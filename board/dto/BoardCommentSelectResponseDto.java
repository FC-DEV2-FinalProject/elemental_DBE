package ipat.admin.board.dto;

import ipat.admin.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BoardCommentSelectResponseDto {
    private Long board_num;
    private String subject;
    private String userName;
    private LocalDateTime createdAt;
    private Boolean isSecret;

    public static BoardCommentSelectResponseDto fromEntity(Board board) {
        return new BoardCommentSelectResponseDto(
                board.getBoardNum(),
                board.getSubject(),
                board.getUserName(),
                board.getCreatedAt(),
                board.getIsSecret()
        );
    }
}
