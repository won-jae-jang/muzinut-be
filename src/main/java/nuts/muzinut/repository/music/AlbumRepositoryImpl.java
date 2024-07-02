package nuts.muzinut.repository.music;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import nuts.muzinut.dto.music.AlbumDetaillDto;
import nuts.muzinut.dto.music.AlbumSongDetaillDto;

import java.util.List;

import static nuts.muzinut.domain.member.QUser.user;
import static nuts.muzinut.domain.music.QAlbum.album;
import static nuts.muzinut.domain.music.QSong.song;

@RequiredArgsConstructor
public class AlbumRepositoryImpl implements AlbumRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<AlbumDetaillDto> albumDetaill(Long id) {
        return queryFactory
                .select(Projections.constructor(AlbumDetaillDto.class,
                        album.name,
                        album.albumImg,
                        user.nickname,
                        album.intro
                ))
                .from(album)
                .where(album.id.eq(id))
                .groupBy(album.id)
                .fetch();
    }

    @Override
    public List<AlbumSongDetaillDto> albumSongDetaill(Long id) {
        return queryFactory
                .select(Projections.constructor(AlbumSongDetaillDto.class,
                        song.id,
                        song.title,
                        user.nickname
                ))
                .from(song)
                .where(song.album.id.eq(id))
                .groupBy(song.id)
                .fetch();
    }
}