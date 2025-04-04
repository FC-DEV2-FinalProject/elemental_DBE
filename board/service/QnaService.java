package ipat.admin.board.service;

import ipat.admin.board.dto.*;
import ipat.admin.board.entity.Board;
import ipat.admin.board.entity.BoardComment;
import ipat.admin.board.entity.BoardManager;
import ipat.admin.board.entity.BoardPds;
import ipat.admin.board.repository.BoardCommentRepository;
import ipat.admin.board.repository.BoardManagerRepository;
import ipat.admin.board.repository.BoardPdsRepository;
import ipat.admin.board.repository.BoardRepository;
import ipat.admin.entity.Admin;
import ipat.admin.repository.AdminRepository;
import ipat.email.api.SendNhnToastEmailApi;
import ipat.email.dto.NhnToastEmailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QnaService {

    private final BoardRepository boardRepository;

    private final AdminRepository adminRepository;

    private final BoardCommentRepository boardCommentRepository;

    private final BoardManagerRepository boardManagerRepository;

    private final BoardPdsRepository boardPdsRepository;

    private final SendNhnToastEmailApi sendNhnToastEmailApi = new SendNhnToastEmailApi();

    // 전체 or 검색 조회
    public Optional<Page<BoardCommentSelectResponseDto>> SearchBoardByKeyword(String searchType, String keyword, Pageable pageable, Long managerNum) {

        BoardManager boardManager = boardManagerRepository.findByManagerNum(managerNum);

        // Specification 사용하여 검색 조건을 생성
        Specification<Board> searchSpecification = BoardSelectRequestDto.searchNews(searchType, keyword, boardManager);

        // findAll 호출하여 결과를 얻음
        Page<Board> result = boardRepository.findAll(searchSpecification, pageable);

        // Page<Board>를 Page<NewsSelectResponseDto>로 변환
        Page<BoardCommentSelectResponseDto> responsePage = result.map(BoardCommentSelectResponseDto::fromEntity);

        // 결과가 있으면 Optional 반환, 없으면 Optional.empty() 반환
        return responsePage.hasContent() ? Optional.of(responsePage) : Optional.empty();
    }

    // 상세 조회
    public Optional<BoardResponseDto> getBoardById(Long board_num, BoardCommentRequestDto boardCommentRequestDto) {
        Optional<Board> board = boardRepository.findById(board_num);
        if(board.isPresent()){
            if(Objects.equals(boardCommentRequestDto.getPwd(), board.get().getPwd())){
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
                        .boardPds(boardPdsResponseDto)
                        .build();
                // 조회수 증가
                boardEntity.setReadCnt(boardEntity.getReadCnt()+1);
                boardRepository.save(boardEntity);
                return Optional.of(boardResponseDto);
            }
            else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found");
        }
    }

    // 답글 조회
    public Optional<BoardCommentResponseDto> getBoardCommentById(Long boardNum) {
        Optional<BoardComment> board = boardCommentRepository.findByBoard(boardRepository.findById(boardNum));
        Board checkBoard = boardRepository.findById(boardNum).get();

        if(board.isPresent()){
            BoardComment boardComment = board.get();
            List<BoardPds> boardPds = boardPdsRepository.findByBoard(checkBoard);
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
            BoardCommentResponseDto boardCommentResponseDto = BoardCommentResponseDto.builder()
                    .commentNum(boardComment.getCommentNum())
                    .comments(boardComment.getComments())
                    .userName(boardComment.getUserName())
                    .writeDate(boardComment.getWriteDate())
                    .build();
            // 조회수 증가
            checkBoard.setReadCnt(checkBoard.getReadCnt()+1);
            boardRepository.save(checkBoard);
            return Optional.of(boardCommentResponseDto);
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Board not found");
        }
    }

    // 관리자 전송 이메일
    private void sendEmailToAdmin(Board board) {
        String adminEmail = "admin@example.com";
        String adminName = "관리자";

        try {
            NhnToastEmailVO emailVO = new NhnToastEmailVO();
            emailVO.setSenderAddress(board.getEmail()); // 발신자 이메일 주소
            emailVO.setSenderName("사용자");
            emailVO.setTitle("새로운 질문이 등록되었습니다.");
            emailVO.setBody("관리자님,\n\n" +
                    "새로운 질문이 등록되었습니다.\n\n" +
                    "작성자: " + board.getUserName() + "\n" +
                    "제목: " + board.getSubject() + "\n" +
                    "내용: " + board.getContents() + "\n\n" +
                    "웹사이트에서 확인해 주세요.");
            emailVO.setReceiveMailAddr(adminEmail);
            emailVO.setReceiveName(adminName);
            emailVO.setReceiveType("M");

            int result = sendNhnToastEmailApi.sendNhnToastEmailApi(emailVO);
            System.out.println("Admin email send result: " + result);
        } catch (Exception e) {
            System.out.println("Error sending email to admin");
            e.printStackTrace();
        }
    }

    // 답변 생성
    public BoardCommentResponseDto createAnswer(Long boardId, BoardCommentRequestDto boardRequestCommentDto, HttpSession session, HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");

        // 1. 게시판 ID로 해당 게시판을 찾기
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("게시판을 찾을 수 없습니다."));

        // 2. 관리자 확인
        String userid = (String) session.getAttribute("userid");
        Admin admin = adminRepository.findByManagerId(userid);

        // 2. Board_Comment 엔티티 생성
        if(admin != null) {
            BoardComment newComment = BoardComment.builder()
                    .board(board)
                    .comments(boardRequestCommentDto.getComment())
                    .userName(admin.getManagerName())
                    .userId(admin.getManagerId())
                    .userIp(ip)
                    .pwd(boardRequestCommentDto.getPwd())
                    .writeDate(LocalDateTime.now())
                    .build();

            BoardComment savedComment = boardCommentRepository.save(newComment);
            sendEmailToQuestioner(board, savedComment);

            board.setSubject("[답변 완료] " + board.getSubject());
            boardRepository.save(board);

            return BoardCommentResponseDto.fromEntity(savedComment);
        }

        else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication failed");

    }

    // 질문자에게 답변 이메일 보내기
    private void sendEmailToQuestioner(Board board, BoardComment answer) {
        String userEmail = board.getEmail();
        String questionerName = board.getUserName();
        String adminName = answer.getUserName(); // 답변을 작성한 관리자 이름
        String questionTitle = board.getSubject(); // 질문 제목

        if (userEmail != null && !userEmail.isEmpty()) {
            try {
                // 이메일 발송 정보를 설정
                NhnToastEmailVO emailVO = new NhnToastEmailVO();
                emailVO.setSenderAddress("your-email@example.com"); // 발신자 이메일 주소 (적절하게 변경)
                emailVO.setSenderName("관리자");
                emailVO.setTitle("질문에 대한 답변이 등록되었습니다.");
                emailVO.setBody("안녕하세요 " + questionerName + "님!\n\n" +
                        "회원님이 작성하신 질문(제목: " + questionTitle + ")에 " +
                        "관리자(" + adminName + ")님이 답변을 등록하였습니다.\n\n" +
                        "웹사이트에서 답변을 확인해 주세요.");

                emailVO.setReceiveMailAddr(userEmail);
                emailVO.setReceiveName(questionerName);
                emailVO.setReceiveType("M");

                // 이메일 전송 API 호출
                sendNhnToastEmailApi.sendNhnToastEmailApi(emailVO);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
