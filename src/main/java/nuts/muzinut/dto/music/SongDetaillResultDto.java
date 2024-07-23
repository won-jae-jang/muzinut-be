package nuts.muzinut.dto.music;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nuts.muzinut.domain.music.Genre;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SongDetaillResultDto {

    private String albumImg;
    private String title;
    private String nickname;
    private Long likeCount;
    private String lyrics;
    private String composer;
    private String lyricist;
    private Long albumId;
    private List<Genre> genres;
    private boolean like;
}
