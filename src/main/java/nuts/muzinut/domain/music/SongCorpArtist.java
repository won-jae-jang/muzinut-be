package nuts.muzinut.domain.music;

import jakarta.persistence.*;
import lombok.Getter;
import nuts.muzinut.domain.member.Member;

@Entity
@Getter
@Table(name = "music_corp_artist")
public class SongCorpArtist {

    @Id @GeneratedValue
    @Column(name = "music_corp_artist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Song song;

    @Column(name = "member_id")
    private Long memberId;

    public void addCorpArtist(Song song, Member member) {
        this.song = song;
        this.memberId = member.getId();
        song.getSongCorpArtists().add(this);
    }
}
