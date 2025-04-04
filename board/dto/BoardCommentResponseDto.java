package ipat.admin.board.dto;

import ipat.admin.board.entity.Board;
import ipat.admin.board.entity.BoardComment;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BoardCommentResponseDto {

    private Long commentNum;    // 답글 번호
    private String comments;    // 답글 내용
    private String userName;    // 회원 이름
    private LocalDateTime writeDate; // 등록 일시

    public static BoardCommentResponseDto fromEntity(BoardComment boardComment) {
        return BoardCommentResponseDto.builder()
                .commentNum(boardComment.getCommentNum())
                .comments(boardComment.getComments())
                .userName(boardComment.getUserName())
                .writeDate(boardComment.getWriteDate())
                .build();
    }
}