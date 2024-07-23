package nuts.muzinut.dto.artist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HotArtistDto {
    private Long id;
    private String nickname;
    private Long follower;
    private String img;
    private Boolean isFollow = null;
}
