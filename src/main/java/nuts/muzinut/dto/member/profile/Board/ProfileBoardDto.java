package nuts.muzinut.dto.member.profile.Board;

import lombok.Data;
import nuts.muzinut.dto.member.profile.ProfileDto;

import java.util.List;

@Data
public class ProfileBoardDto extends ProfileDto {

    List<BoardsTitle> boards;
    List<BookmarkTitle> bookmarks;

    // 기본 생성자
    public ProfileBoardDto(Long userId, String profileBannerImgName, String profileImgName, String nickname, String intro, Long followingCount, Long followersCount, boolean followStatus, List<BoardsTitle> boards, List<BookmarkTitle> bookmarks) {
        super(userId, profileBannerImgName, profileImgName, nickname, intro, followingCount, followersCount, followStatus);
        this.boards = boards;
        this.bookmarks = bookmarks;
    }

    // 새로운 생성자: 보드 정보 없이 프로필 정보만 반환
    public ProfileBoardDto(Long userId, String profileBannerImgName, String profileImgName, String nickname, String intro, Long followingCount, Long followersCount, boolean followStatus) {
        super(userId, profileBannerImgName, profileImgName, nickname, intro, followingCount, followersCount, followStatus);
    }
}
