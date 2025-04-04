package ipat.admin.board.dto;

import ipat.admin.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class BoardResponseDto {
    private Long boardNum;
    private String subject;
    private String content;
    private String userName;
    private Character postType;
    private LocalDateTime createdAt;
    private List<BoardPdsResponseDto> boardPds;


    public static BoardResponseDto fromEntity(Board board, List<BoardPdsResponseDto> boardPds) {
        return new BoardResponseDto(
                board.getBoardNum(),
                board.getSubject(),
                board.getContents(),
                board.getUserName(),
                board.getPostType(),
                board.getCreatedAt(),
                boardPds
        );
    }
}
