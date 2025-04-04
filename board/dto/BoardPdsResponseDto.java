package ipat.admin.board.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardPdsResponseDto {
    private String fileOriginalName;
    private String fileUrl;
    private String fileType;
}
