package ipat.admin.board.repository;

import ipat.admin.board.entity.Board;
import ipat.admin.board.entity.BoardComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BoardCommentRepository extends JpaRepository<BoardComment, Long> {



    Optional<BoardComment> findByBoard(Optional<Board> byId);
}