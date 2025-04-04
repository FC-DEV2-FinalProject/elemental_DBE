package ipat.admin.board.dto;

import ipat.admin.board.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BoardSelectResponseDto {
    private Long board_num;
    private String subject;
    private String userName;
    private Character postType;
    private LocalDateTime createdAt;

    public static BoardSelectResponseDto fromEntity(Board board) {
        return new BoardSelectResponseDto(
                board.getBoardNum(),
                board.getSubject(),
                board.getUserName(),
                board.getPostType(),
                board.getCreatedAt()
        );
    }
}
