package nuts.muzinut.dto.member.profile.Lounge;

import lombok.Data;
import nuts.muzinut.dto.member.profile.ProfileDto;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProfileLoungeDto extends ProfileDto {

    private List<ProfileLoungesForm> loungesForms = new ArrayList<>();
    private int currentPage;
    private int totalPage;
    private Long totalContent; //조회된 총 데이터 수

    public void setPaging(int currentPage, int totalPage, Long totalContent) {
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.totalContent = totalContent;
    }

    // 기존 생성자
    public ProfileLoungeDto(Long userId, String profileBannerImgName, String profileImgName, String nickname, String intro, Long followingCount, Long followersCount, boolean followStatus, List<ProfileLoungesForm> loungesForms, int currentPage, int totalPage, Long totalContent) {
        super(userId, profileBannerImgName, profileImgName, nickname, intro, followingCount, followersCount, followStatus);
        this.loungesForms = loungesForms;
        this.currentPage = currentPage;
        this.totalPage = totalPage;
        this.totalContent = totalContent;
    }

    // 새로운 생성자: 라운지 정보 없이 프로필 정보만 반환
    public ProfileLoungeDto(Long userId, String profileBannerImgName, String profileImgName, String nickname, String intro, Long followingCount, Long followersCount, boolean followStatus) {
        super(userId, profileBannerImgName, profileImgName, nickname, intro, followingCount, followersCount, followStatus);
    }
}
