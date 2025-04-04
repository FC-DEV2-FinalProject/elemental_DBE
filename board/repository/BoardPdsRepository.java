package ipat.admin.board.repository;

import ipat.admin.board.entity.Board;
import ipat.admin.board.entity.BoardPds;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardPdsRepository extends JpaRepository<BoardPds, Integer> {
    List<BoardPds> findByBoard(Board board);
}
