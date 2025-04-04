package ipat.admin.board.dto;

import ipat.admin.board.entity.Board;
import ipat.admin.board.entity.BoardManager;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Getter
@Builder
@RequiredArgsConstructor
public class BoardSelectRequestDto {

    // searchNews를 static 메서드로 변경
    public static Specification<Board> searchNews(String searchType, String keyword, BoardManager manager) {

        return (Root<Board> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            Predicate predicate;
            // 검색어가 없으면 전체 조회 (단, manager_num은 항상 5)
            if (keyword == null || keyword.isEmpty()) {
                predicate = cb.conjunction();
            } else {
                String likeKeyword = "%" + keyword + "%";
                switch (searchType) {
                    case "제목":
                        predicate = cb.like(root.get("subject"), likeKeyword);
                        break;
                    case "내용":
                        predicate = cb.like(root.get("contents"), likeKeyword);
                        break;
                    case "제목 + 내용":
                        Predicate title = cb.like(root.get("subject"), likeKeyword);
                        Predicate content = cb.like(root.get("contents"), likeKeyword);
                        predicate = cb.or(title, content);
                        break;
                    case "작성자":
                        predicate = cb.like(root.get("user_name"), likeKeyword);
                        break;
                    default:
                        Predicate titlePredicateAll = cb.like(root.get("subject"), likeKeyword);
                        Predicate contentPredicateAll = cb.like(root.get("contents"), likeKeyword);
                        Predicate writerPredicateAll = cb.like(root.get("user_name"), likeKeyword);
                        predicate = cb.or(titlePredicateAll, contentPredicateAll, writerPredicateAll);
                        break;
                }
            }
            // 보도자료 조건을 추가
            Predicate managerPredicate = cb.equal(root.get("board_manager"), manager);
            return cb.and(predicate, managerPredicate);
        };
    }
}
