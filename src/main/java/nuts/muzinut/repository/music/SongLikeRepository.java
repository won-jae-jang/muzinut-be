package nuts.muzinut.repository.music;

import nuts.muzinut.domain.member.User;
import nuts.muzinut.domain.music.Song;
import nuts.muzinut.domain.music.SongLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SongLikeRepository extends JpaRepository<SongLike, Long> {

    @Query("select count(sl) from SongLike sl where sl.song.id = :songId")
    Long countLikesBySongId(@Param("songId") Long songId);

    @Query("select sl from SongLike sl join sl.user u where sl.song = :song and u = :user")
    Optional<SongLike> findBySongLike(@Param("song")Song song, @Param("user")User user);

    @Query("select sl from SongLike sl join sl.user u where sl.song = :song and u.id = :user")
    Optional<SongLike> findBySongLikeUser(@Param("song")Song song, @Param("user")Long userId);
}
