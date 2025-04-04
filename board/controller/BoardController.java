package ipat.admin.board.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Slf4j
@Controller
@RequestMapping("/tiger/board")
@RequiredArgsConstructor
public class BoardController {
    @GetMapping("/boardList")
    public String boardList() {
        return "board/boardList.ipat";
    }
}
