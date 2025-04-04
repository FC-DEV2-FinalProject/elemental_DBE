package ipat.admin.board.controller;


import ipat.admin.board.dto.*;
import ipat.admin.board.repository.BoardPdsRepository;
import ipat.admin.board.service.BoardService;
import ipat.admin.board.service.QnaService;
import ipat.admin.entity.Admin;
import ipat.admin.repository.AdminRepository;
import ipat.file.dto.FileVO;
import ipat.file.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/tiger/board/qna")
@RequiredArgsConstructor
public class QnaController {

    private final QnaService qnaService;

    private final BoardService boardService;

    private final FileUploadService fileUploadService;

    private final BoardPdsRepository boardPdsRepository;

    @Value("${file.upload.path}")
    private String fileUploadPath = "/upload/bbs/qna";

    private final Long managerNum = 2L;

    // 전체 조회 및 검색
    @GetMapping
    public BoardCommentPageResponseDto searchQna(@RequestParam("searchType") String type,
                                           @RequestParam("keyword") String keyword,
                                           @PageableDefault(sort = "board_num", direction = Sort.Direction.DESC) Pageable pageable) {

        // Optional 처리 후, 결과가 있으면 페이지를 가져오고, 없으면 빈 리스트를 반환
        Optional<Page<BoardCommentSelectResponseDto>> optionalQnaPage = qnaService.SearchBoardByKeyword(type, keyword, pageable, managerNum);
        Page<BoardCommentSelectResponseDto> qnaPage = optionalQnaPage.orElse(Page.empty());

        // qnaPageResponseDto로 변환 후 반환
        return new BoardCommentPageResponseDto(
                qnaPage.getContent(), // 콘텐츠 리스트
                qnaPage.getTotalElements(), // 전체 데이터 개수
                qnaPage.getTotalPages(), // 전체 페이지 수
                qnaPage.getNumber() // 현재 페이지 번호
        );
    }

    // 상세 조회
    @GetMapping("/{id}")
    public BoardResponseDto getQnaById(@PathVariable("id") Long boardNum,
                                       @RequestBody BoardCommentRequestDto boardCommentRequestDto) {
        return qnaService.getBoardById(boardNum, boardCommentRequestDto)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "qna not found"));
    }

    // 질문 업로드
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createPds(HttpSession session ,
                          HttpServletRequest request ,
                          @RequestBody BoardInsertRequestDto boardInsertRequestDto,
                          @RequestPart(value = "miniMap", required = false) MultipartFile file) throws IOException {

        boardService.createBoard(session ,request  , boardInsertRequestDto, managerNum, file, fileUploadPath);

        return ResponseEntity.ok().build();
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateQna(@PathVariable("id") Long boardNum ,
                           HttpSession session,
                           HttpServletRequest request ,
                           @RequestBody BoardInsertRequestDto boardInsertRequestDto,
                           @RequestPart(value = "miniMap", required = false) MultipartFile file) throws IOException {

        boardService.updateBoard(boardNum, session ,request  , boardInsertRequestDto, managerNum, file, fileUploadPath);

        return ResponseEntity.ok().build();
    }

    // 답글 작성
    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createComment(@PathVariable("id") Long boardNum,
                          HttpSession session ,
                          HttpServletRequest request ,
                          @RequestBody BoardCommentRequestDto boardCommentRequestDto) {

        qnaService.createAnswer(boardNum, boardCommentRequestDto, session, request);

        return ResponseEntity.ok().build();
    }

}
