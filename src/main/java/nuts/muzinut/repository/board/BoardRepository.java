package nuts.muzinut.repository.board;

import nuts.muzinut.domain.board.AdminBoard;
import nuts.muzinut.domain.board.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

    void deleteById(Long id);

    @Query(value = "SELECT DTYPE FROM BOARD WHERE board_id = :id", nativeQuery = true)
    String findBoardTypeById(@Param("id") Long id);

    Optional<Board> findByFilename(String filename);

    List<Board> findByUserId(Long userId);

    @Query("SELECT b FROM Board b JOIN b.bookmarks bm WHERE bm.user.id = :userId")
    List<Board> findBookmarkedBoardsByUserId(@Param("userId") Long userId);
}
