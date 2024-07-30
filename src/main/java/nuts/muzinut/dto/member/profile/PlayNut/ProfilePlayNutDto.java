package nuts.muzinut.dto.member.profile.PlayNut;

import lombok.Data;
import nuts.muzinut.dto.member.profile.ProfileDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProfilePlayNutDto extends ProfileDto {

    private List<ProfilePlayNutForm> playNuts = new ArrayList<>();

    // 기본 생성자
    public ProfilePlayNutDto(Long userId, String profileBannerImgName, String profileImgName, String nickname, String intro, Long followingCount, Long followersCount, boolean followStatus, List<ProfilePlayNutForm> playNuts) {
        super(userId, profileBannerImgName, profileImgName, nickname, intro, followingCount, followersCount, followStatus);
        this.playNuts = playNuts;
    }

    // 새로운 생성자: 플리넛 없을 경우 프로필 정보만 반환
    public ProfilePlayNutDto(Long userId, String profileBannerImgName, String profileImgName, String nickname, String intro, Long followingCount, Long followersCount, boolean followStatus) {
        super(userId, profileBannerImgName, profileImgName, nickname, intro, followingCount, followersCount, followStatus);
    }
}
