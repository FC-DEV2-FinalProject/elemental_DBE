package ipat.admin.board.repository;

import ipat.admin.board.entity.BoardManager;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardManagerRepository extends JpaRepository<BoardManager, Integer> {
    BoardManager findByManagerNum(Long managerId);
}
