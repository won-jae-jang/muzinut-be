package nuts.muzinut.domain.music;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nuts.muzinut.domain.baseEntity.BaseTimeEntity;
import nuts.muzinut.domain.member.User;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class Song extends BaseTimeEntity {

    @Id @GeneratedValue
    @Column(name = "song_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 10000)
    private String title;
    @Column(length = 10000)
    private String lyrics;
    private String lyricist; // 작사가
    private String composer; // 작곡가

    private String fileName;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    private List<SongGenre> songGenres = new ArrayList<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    private List<PlayView> playViews = new ArrayList<>();

    // 추가
    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    private List<SongLike> songLikes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id")
    private Album album;

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    private List<PlaylistMusic> playlistMusics = new ArrayList<>();

    @OneToMany(mappedBy = "song", cascade = CascadeType.ALL)
    private List<PlayNutMusic> playNutMusics = new ArrayList<>();

    //연관 관계 편의 메서드
    public void createMusic(User user) {
        this.user = user;
        user.getSongList().add(this);
    }

    public Song(User user, String title, String lyrics, String lyricist, String composer, String Filename, Album album) {
        this.user = user;
        this.title = title;
        this.lyrics = lyrics;
        this.lyricist = lyricist;
        this.composer = composer;
        this.fileName = Filename;
        this.album = album;
    }
}