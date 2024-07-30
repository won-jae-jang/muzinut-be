package nuts.muzinut.dto.member.profile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileDto {

    private Long userId; // 회원 pk
    private String profileBannerImgName;
    private String profileImgName;
    private String nickname;
    private String intro;
    private Long followingCount;
    private Long followersCount;
    private boolean FollowStatus;
}
