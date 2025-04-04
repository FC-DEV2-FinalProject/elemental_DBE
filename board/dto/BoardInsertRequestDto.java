package ipat.admin.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardInsertRequestDto {

    private Long boardNum;

    private String subject;

    private String userName;

    private String content;

    private Boolean isSecret = false;

    private String pwd;

}
