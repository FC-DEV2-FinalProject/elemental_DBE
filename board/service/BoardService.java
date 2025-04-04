package ipat.admin.board.service;

import ipat.admin.board.dto.*;
import ipat.admin.board.entity.Board;
import ipat.admin.board.entity.BoardComment;
import ipat.admin.board.entity.BoardManager;
import ipat.admin.board.entity.BoardPds;
import ipat.admin.board.repository.BoardCommentRepository;
import ipat.admin.board.repository.BoardRepository;
import ipat.admin.board.repository.BoardManagerRepository;
import ipat.admin.board.repository.BoardPdsRepository;
import ipat.admin.entity.Admin;
import ipat.admin.repository.AdminRepository;
import ipat.email.api.SendNhnToastEmailApi;
import ipat.email.dto.NhnToastEmailVO;
import ipat.file.dto.FileVO;
import ipat.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    private final AdminRepository adminRepository;

    private final BoardManagerRepository boardManagerRepository;

    private final BoardPdsRepository boardPdsRepository;

    private final FileUploadService fileUploadService;

    // 파일 업로드
    public FileVO uploadFile(MultipartFile file, String uploadPath) throws IOException {
        if (file.isEmpty()) {
            return null;
        }

        // 1. 현재 년도 가져오기
        String currentYear = String.valueOf(LocalDateTime.now().getYear());

        // 2. 기본 업로드 디렉토리 설정 (년도/directory)
        String yearPath = currentYear;
        String uploadDir = uploadPath + File.separator + yearPath;
        Path dirPath = Paths.get(uploadDir);

        // 3. 디렉토리가 없으면 생성
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 4. 파일 정보 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // 5. 타임스탬프 기반 파일명 생성
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String savedFileName = timestamp + fileExtension;

        // 6. 파일 저장
        Path filePath = dirPath.resolve(savedFileName);
        file.transferTo(filePath.toFile());

        // 7. FileVO 객체 생성 및 반환 (웹 접근 경로로 변경)
        return FileVO.builder()
                .originalFileName(originalFilename)
                .savedFileName(savedFileName)
                .savedFilePath("/uploads/" + yearPath)  // 웹 접근 경로로 변경
                .fileExtension(fileExtension)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    // 전체 조회 및 검색
    public Optional<Page<BoardSelectResponseDto>> SearchBoardByKeyword(String searchType, String keyword, Pageable pageable, Long managerNum) {

        BoardManager boardManager = boardManagerRepository.findByManagerNum(managerNum);

        // Specification 사용하여 검색 조건을 생성
        Specification<Board> searchSpecification = BoardSelectRequestDto.searchNews(searchType, keyword, boardManager);

        // findAll을 호출하여 결과를 얻음
        Page<Board> result = boardRepository.findAll(searchSpecification, pageable);

        // Page<Board>를 Page<NewsSelectResponseDto>로 변환
        Page<BoardSelectResponseDto> responsePage = result.map(BoardSelectResponseDto::fromEntity);

        // 결과가 있으면 Optional로 반환, 없으면 Optional.empty() 반환
        return responsePage.hasContent() ? Optional.of(responsePage) : Optional.empty();
    }

    // 상세 조회
    public Optional<BoardResponseDto> getBoardById(Long board_num){
        Optional<Board> board = boardRepository.findById(board_num);
        if(board.isPresent()){
            Board boardEntity = board.get();
            List<BoardPds> boardPds = boardPdsRepository.findByBoard(boardEntity);
            List<BoardPdsResponseDto> boardPdsResponseDto = new ArrayList<>();
            // 게시판 첨부파일 가져오기
            for(BoardPds boardPd : boardPds){
                BoardPdsResponseDto bpDto = BoardPdsResponseDto.builder()
                        .fileOriginalName(boardPd.getFileOriginalName())
                        .fileUrl(boardPd.getFileSavePath())
                        .fileType(boardPd.getContentType())
                        .build();
                boardPdsResponseDto.add(bpDto);
            }
            // 조회
            BoardResponseDto boardResponseDto = BoardResponseDto.builder()
                    .boardNum(boardEntity.getBoardNum())
                    .subject(boardEntity.getSubject())
                    .content(boardEntity.getContents())
                    .userName(boardEntity.getUserName())
                    .postType(boardEntity.getPostType())
                    .createdAt(boardEntity.getCreatedAt())
                    .boardPds(boardPdsResponseDto)
                    .build();
            // 조회수 증가
            boardEntity.setReadCnt(boardEntity.getReadCnt()+1);
            boardRepository.save(boardEntity);
            return Optional.of(boardResponseDto);
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found");
        }
    }

    @Transactional
    // 업로드
    public void createBoard(HttpSession session , HttpServletRequest request, BoardInsertRequestDto boardDto, Long managerNum, MultipartFile file, String url) throws IOException {
        String ip = request.getHeader("X-Forwarded-For");
        String userid = (String) session.getAttribute("userid");
        Admin admin = adminRepository.findByManagerId(userid);

        if(admin != null) {

            Board board = Board.builder()
                    .managerNum(boardManagerRepository.findByManagerNum(managerNum))
                    .subject(boardDto.getSubject())
                    .thread(0L)
                    .orderNum(0L)
                    .readCnt(0L)
                    .email(admin.getManagerEmail())
                    .userName(boardDto.getUserName())
                    .email(admin.getManagerEmail())
                    .userIp(ip)
                    .build();
            boardRepository.save(board);

            if (file != null && !file.isEmpty()) {
                // 파일 업로드 후 FileVO 객체 받아오기
                FileVO fileVO = uploadFile(file, url);;

                BoardPds boardPds = BoardPds.builder()
                        .board(board)
                        .fileOriginalName(fileVO.getOriginalFileName())
                        .fileSavedName(fileVO.getSavedFileName())
                        .fileSavePath(fileVO.getSavedFilePath())
                        .contentType(fileVO.getContentType())
                        .build();

                boardPdsRepository.save(boardPds);


            }
        }
        else {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");}

    }

    @Transactional
    // 수정
    public void updateBoard(Long board_num, HttpSession session , HttpServletRequest request , BoardInsertRequestDto boardDto, Long managerNum, MultipartFile file, String url) throws IOException{
        String ip = request.getHeader("X-Forwarded-For");
        String userid = (String) session.getAttribute("userid");
        Admin admin = adminRepository.findByManagerId(userid);
        Board boardEntity = boardRepository.findByBoardNum(board_num);


        if(admin != null) {
            try {
                Board board = Board.builder()
                        .managerNum(boardManagerRepository.findByManagerNum(managerNum))
                        .subject(boardDto.getSubject())
                        .thread(0L)
                        .orderNum(0L)
                        .readCnt(boardEntity.getReadCnt())
                        .email(admin.getManagerEmail())
                        .userName(boardDto.getUserName())
                        .email(admin.getManagerEmail())
                        .updateIp(ip)
                        .build();
                boardRepository.save(board);

                if (file != null && !file.isEmpty()) {
                    // 파일 업로드 후 FileVO 객체 받아오기
                    FileVO fileVO = uploadFile(file, url);

                    BoardPds boardPds = BoardPds.builder()
                            .board(boardEntity)
                            .fileOriginalName(fileVO.getOriginalFileName())
                            .fileSavedName(fileVO.getSavedFileName())
                            .fileSavePath(fileVO.getSavedFilePath())
                            .contentType(fileVO.getContentType())
                            .build();
                    boardPdsRepository.save(boardPds);

                }
            } catch (RuntimeException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found");
            }
        }
        else {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found");}

    }

}
