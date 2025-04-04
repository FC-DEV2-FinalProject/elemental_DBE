package ipat.admin.board.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BoardPageResponseDto {
    private List<BoardSelectResponseDto> content;
    private PageInfo page;

    public BoardPageResponseDto(List<BoardSelectResponseDto> content, long totalItems, int totalPages, int currentPage) {
        this.content = content;
        this.page = new PageInfo(totalItems, totalPages, currentPage);
    }

    @Getter
    @Setter
    static class PageInfo {
        private long totalItems;
        private int totalPages;
        private int currentPage;

        public PageInfo(long totalElements, int totalPages, int currentPage) {
            this.totalItems = totalElements;
            this.totalPages = totalPages;
            this.currentPage = currentPage;
        }
    }
}