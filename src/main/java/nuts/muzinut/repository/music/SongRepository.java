package nuts.muzinut.repository.music;

import nuts.muzinut.domain.music.Album;
import nuts.muzinut.domain.music.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long>, SongRepositoryCustom {

    List<Song> findAllByAlbum(Album album);

    Optional<Song> findByFileName(String fileName);

    @Query("SELECT s FROM Song s LEFT JOIN s.songLikes sl WHERE s.user.id = :userId GROUP BY s.id ORDER BY COUNT(sl.id) DESC, s.id ASC")
    List<Song> findSongsByUserIdOrderByLikesAndId(Long userId); // 좋아요 수와 등록 순으로 곡 리스트를 가져오는 메소드 추가

    @Modifying
    @Transactional
    @Query("update Song s set s.title = :title, s.lyricist = :lyricist, "
    + "s.composer = :composer, s.lyrics = :lyrics, s.fileName = :fileName "
    + "where s.id = :id")
    void updateById(@Param("title") String title, @Param("lyricist") String lyricist,
                    @Param("composer") String composer, @Param("lyrics") String lyrics,
                    @Param("fileName") String fileName, @Param("id") Long id);

    @Query("select s from Song s join s.user u where s.id = :songId and u.id = :userId")
    Optional<Song> findByUser(@Param("songId")Long albumId, @Param("userId")Long userId);
}
