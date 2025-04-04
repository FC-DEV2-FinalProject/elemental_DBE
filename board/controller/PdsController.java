package ipat.admin.board.controller;


import ipat.admin.board.dto.BoardPageResponseDto;
import ipat.admin.board.dto.BoardInsertRequestDto;
import ipat.admin.board.dto.BoardResponseDto;
import ipat.admin.board.dto.BoardSelectResponseDto;
import ipat.admin.board.entity.BoardPds;
import ipat.admin.board.service.BoardService;
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
@RequestMapping("/tiger/board/pds")
@RequiredArgsConstructor
public class PdsController {

    private final BoardService boardService;

    private final FileUploadService fileUploadService;

    @Value("${file.upload.path}")
    private String fileUploadPath = "/upload/bbs/pds";

    private final Long managerNum = 2L;

    // . 전체 조회 및 검색
    @GetMapping
    public BoardPageResponseDto searchPds(@RequestParam("searchType") String type,
                                           @RequestParam("keyword") String keyword,
                                           @PageableDefault(sort = "board_num", direction = Sort.Direction.DESC) Pageable pageable) {

        // Optional 처리 후, 결과가 있으면 페이지를 가져오고, 없으면 빈 리스트를 반환
        Optional<Page<BoardSelectResponseDto>> optionalPdsPage = boardService.SearchBoardByKeyword(type, keyword, pageable, managerNum);
        Page<BoardSelectResponseDto> pdsPage = optionalPdsPage.orElse(Page.empty());

        // pdsPageResponseDto로 변환 후 반환
        return new BoardPageResponseDto(
                pdsPage.getContent(), // 콘텐츠 리스트
                pdsPage.getTotalElements(), // 전체 데이터 개수
                pdsPage.getTotalPages(), // 전체 페이지 수
                pdsPage.getNumber() // 현재 페이지 번호
        );
    }

    // . 상세 조회
    @GetMapping("/{id}")
    public BoardResponseDto getPdsById(@PathVariable("id") Long boardNum) {
        return boardService.getBoardById(boardNum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "pds not found"));
    }

    // . 올리기
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> createPds(HttpSession session ,
                           HttpServletRequest request ,
                           @RequestBody BoardInsertRequestDto boardInsertRequestDto,
                           @RequestPart(value = "miniMap", required = false) MultipartFile file) throws IOException {

        boardService.createBoard(session ,request  , boardInsertRequestDto, managerNum, file, fileUploadPath);

        return ResponseEntity.ok().build();
    }

    // . 수정
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePds(@PathVariable("id") Long boardNum ,
                                          HttpSession session,
                                          HttpServletRequest request ,
                                          @RequestBody BoardInsertRequestDto boardInsertRequestDto,
                                          @RequestPart(value = "miniMap", required = false) MultipartFile file) throws IOException {

        boardService.updateBoard(boardNum, session ,request  , boardInsertRequestDto, managerNum, file, fileUploadPath);

        return ResponseEntity.ok().build();
    }
}
