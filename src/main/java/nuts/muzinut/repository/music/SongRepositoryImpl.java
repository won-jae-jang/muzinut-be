package nuts.muzinut.repository.music;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nuts.muzinut.domain.music.Genre;
import nuts.muzinut.dto.music.SongDetailDto;
import nuts.muzinut.dto.music.SongGenreDto;
import nuts.muzinut.dto.music.SongPageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.springframework.data.domain.Pageable;

import java.util.List;

import static nuts.muzinut.domain.member.QUser.user;
import static nuts.muzinut.domain.music.QAlbum.album;
import static nuts.muzinut.domain.music.QPlayView.playView;
import static nuts.muzinut.domain.music.QSong.song;
import static nuts.muzinut.domain.music.QSongGenre.songGenre;
import static nuts.muzinut.domain.music.QSongLike.songLike;

@RequiredArgsConstructor
public class SongRepositoryImpl implements SongRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    // 인기 TOP100 곡 불러오는 쿼리
    @Override
    public Page<SongPageDto> hotTOP100Song(Pageable pageable) {
        List<SongPageDto> content = queryFactory
                .select(Projections.constructor(SongPageDto.class,
                        song.id,
                        album.albumImg,
                        song.title,
                        user.nickname))
                .from(song)
                .join(song.album, album)
                .join(song.user, user)
                .where(song.id.in(JPAExpressions
                        .select(playView.song.id)
                        .from(playView)
                        .groupBy(playView.song.id)
                        .orderBy(playView.id.count().desc())
                ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 음원 수는 100으로 고정
        List<Long> songIds = queryFactory
                .select(playView.song.id)
                .from(playView)
                .groupBy(playView.song.id)
                .orderBy(playView.id.count().desc())
                .limit(100)
                .fetch();
        long total = songIds.size();
        return new PageImpl<>(content, pageable, total);
    }

    // 최신음악 불러오는 쿼리
    @Override
    public Page<SongPageDto> new100Song(Pageable pageable) {

        List<SongPageDto> content = queryFactory
                .select(Projections.constructor(SongPageDto.class,
                        song.id,
                        album.albumImg,
                        song.title,
                        user.nickname))
                .from(song)
                .join(song.album, album)
                .join(song.user, user)
                .where(song.id.in(
                        JPAExpressions.select(song.id)
                                .from(song)
                                .orderBy(song.createdDt.desc())
                ))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 총 음원 수는 100으로 고정
        List<Long> songIds = queryFactory
                .select(song.id)
                .from(song)
                .orderBy(song.createdDt.desc())
                .limit(100)
                .fetch();
        long total = songIds.size();
        return new PageImpl<>(content, pageable, total);
    }

    // 장르별음악 불러오는 쿼리
    @Override
    public Page<SongPageDto> genreSong(String genre, Pageable pageable) {
        List<SongPageDto> content = queryFactory
                .select(Projections.constructor(SongPageDto.class,
                        song.id,
                        album.albumImg,
                        song.title,
                        user.nickname))
                .from(song)
                .join(song.album, album)
                .join(song.user, user)
                .join(song.playViews, playView)
                .where(song.id.in(
                        JPAExpressions
                                .select(songGenre.song.id)
                                .from(songGenre)
                                .where(songGenre.genre.eq(Genre.valueOf(genre.toUpperCase())))
                ))
                .groupBy(song.id)
                .orderBy(playView.id.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        // 총 음원 수는 100으로 고정
        List<Long> songIds = queryFactory
                .select(song.id)
                .from(song)
                .join(song.playViews, playView)
                .where(song.id.in(
                        JPAExpressions
                                .select(songGenre.song.id)
                                .from(songGenre)
                                .where(songGenre.genre.eq(Genre.valueOf(genre.toUpperCase())))
                ))
                .groupBy(song.id)
                .orderBy(playView.id.count().desc())
                .limit(100)
                .fetch();

        long total = songIds.size();

        return new PageImpl<>(content, pageable, total);
    }
    // Genre( KPOP, BALLAD, POP, HIPHOP, RNB, INDIE, TROT, VIRTUBER, ETC )

    // 곡 상세 페이지 불러오는 쿼리
    public List<SongDetailDto> songDetail(Long id){


        return queryFactory
                .select(Projections.constructor(SongDetailDto.class,
                        song.album.albumImg,
                        song.title,
                        song.user.nickname,
                        songLike.id.count().as("likeCount"),
                        song.lyrics,
                        song.composer,
                        song.lyricist,
                        song.album.id))
                .from(song)
                .leftJoin(song.songLikes, songLike)
                .where(song.id.eq(id))
                .groupBy(song.id)
                .fetch();
    }

    // 곡 상세페이지에 장르를 불러오는 쿼리
    public List<SongGenreDto> songDetailGenre(Long id){

        return queryFactory
                .select(Projections.constructor(SongGenreDto.class,
                        songGenre.genre
                ))
                .from(songGenre)
                .where(songGenre.song.id.eq(id))
                .fetch();
    }
}
