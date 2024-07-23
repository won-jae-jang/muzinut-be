package nuts.muzinut.repository.mainpage;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import nuts.muzinut.domain.board.Board;
import nuts.muzinut.dto.mainpage.*;
import org.springframework.stereotype.Repository;

import java.util.List;

import static nuts.muzinut.domain.member.QUser.*;
import static nuts.muzinut.domain.music.QAlbum.*;
import static nuts.muzinut.domain.music.QSong.*;
import static nuts.muzinut.domain.music.QPlayView.*;
import static nuts.muzinut.domain.member.QFollow.*;
import static nuts.muzinut.domain.board.QBoard.*;

@RequiredArgsConstructor
@Repository
public class MainPageRepository {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    EntityManager em;

    // 메인페이지 TOP10 곡을 불러오는 쿼리
    public List<HotSongDto> findTOP10Song() {
        return queryFactory
                .select(Projections.constructor(HotSongDto.class,
                        song.id,
                        album.albumImg,
                        song.title,
                        user.nickname))
                .from(playView)
                .join(playView.song, song)
                .join(song.user, user)
                .join(song.album, album)
                .groupBy(song.id)
                .orderBy(playView.id.count().desc())
                .limit(10)
                .fetch();
    }

    // 메인페이지 TOP5 아티스트를 불러오는 쿼리
    public List<HotArtistDto> findTOP5Artist() {
        return queryFactory
                .select(Projections.constructor(HotArtistDto.class,
                        user.id,
                        user.profileImgFilename,
                        user.nickname))
                .from(user)
                .leftJoin(follow)
                .on(user.id.eq(follow.followingMemberId))
                .having(follow.followingMemberId.count().gt(0))
                .groupBy(user.id)
                .orderBy(follow.followingMemberId.count().desc())
                .limit(5)
                .fetch();
    }

    // 메인페이지 최신음악을 불러오는 쿼리
    public List<NewSongDto> findNewSong() {
        return queryFactory
                .select(Projections.constructor(NewSongDto.class,
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
                .limit(15)
                .fetch();
    }

    // 메인페이지 인기게시판을 불러오는 쿼리
    public List<Object[]> findHotBoard(){
         String sql = "SELECT " +
                "b.board_id, " +
                "b.title, " +
                "u.nickname, " +
                "b.view, " +
                "b.dtype " +
                "FROM board b " +
                "JOIN users u ON b.user_id = u.user_id " +
                 "WHERE b.dtype = 'FreeBoard' OR b.dtype = 'RecruitBoard'" +
                "ORDER BY b.view DESC " +
                "LIMIT 5 ";

        Query nativeQuery = em.createNativeQuery(sql);
        return nativeQuery.getResultList();
    }

    // 메인페이지 최신게시판을 불러오는 쿼리
    public List<Object[]> findNewBoard(){
        String sql = "(SELECT " +
                    "b.board_id, " +
                    "b.title, " +
                    "u.nickname, " +
                    "b.dtype " +
                    "FROM board b " +
                    "JOIN users u ON b.user_id = u.user_id " +
                    "WHERE b.dtype = 'FreeBoard' " +
                    "ORDER BY b.created_dt DESC " +
                    "LIMIT 4) " +
                    "UNION All " +
                    "(SELECT " +
                    "b.board_id, " +
                    "b.title, " +
                    "u.nickname, " +
                    "b.dtype " +
                    "FROM board b " +
                    "JOIN users u ON b.user_id = u.user_id " +
                    "WHERE b.dtype = 'RecruitBoard' " +
                    "ORDER BY b.created_dt DESC " +
                    "LIMIT 4)";
        Query nativeQuery = em.createNativeQuery(sql);
        return nativeQuery.getResultList();
    }

}
