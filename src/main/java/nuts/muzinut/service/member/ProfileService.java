package nuts.muzinut.service.member;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.Board;
import nuts.muzinut.domain.board.Lounge;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.domain.music.Album;
import nuts.muzinut.domain.music.PlayNut;
import nuts.muzinut.domain.music.Song;
import nuts.muzinut.dto.board.DetailBaseDto;
import nuts.muzinut.dto.member.profile.Album.ProfileSongDto;
import nuts.muzinut.dto.member.profile.Album.ProfileAlbumDto;
import nuts.muzinut.dto.member.profile.Board.BoardsTitle;
import nuts.muzinut.dto.member.profile.Board.BookmarkTitle;
import nuts.muzinut.dto.member.profile.Board.ProfileBoardDto;
import nuts.muzinut.dto.member.profile.Lounge.ProfileLoungeDto;
import nuts.muzinut.dto.member.profile.Lounge.ProfileLoungesForm;
import nuts.muzinut.dto.member.profile.PlayNut.ProfilePlayNutDto;
import nuts.muzinut.dto.member.profile.PlayNut.ProfilePlayNutForm;
import nuts.muzinut.dto.member.profile.PlayNut.ProfilePlayNutSongDto;
import nuts.muzinut.dto.member.profile.PlayNut.ProfilePlayNutSongsForm;
import nuts.muzinut.dto.member.profile.ProfileDto;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.NotFoundEntityException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.board.BoardRepository;
import nuts.muzinut.repository.board.LoungeRepository;
import nuts.muzinut.repository.board.query.LoungeQueryRepository;
import nuts.muzinut.repository.member.FollowRepository;
import nuts.muzinut.repository.member.UserRepository;
import nuts.muzinut.repository.music.AlbumRepository;
import nuts.muzinut.repository.music.PlayNutRepository;
import nuts.muzinut.repository.music.SongRepository;
import nuts.muzinut.service.board.DetailCommon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService extends DetailCommon {

    private final UserRepository userRepository;
    private final AlbumRepository albumRepository;
    private final SongRepository songRepository;
    private final BoardRepository boardRepository;
    private final FollowRepository followRepository;
    private final LoungeRepository loungeRepository;
    private final LoungeQueryRepository queryRepository;
    private final PlayNutRepository playNutRepository;

    // 기존 userId를 통해 프로필 정보를 가져오는 메소드
    public ProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다."));
        return buildProfileDto(user);
    }

    // 닉네임을 통해 프로필 정보를 가져오는 메소드
    public ProfileDto getUserProfileByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다."));
        return buildProfileDto(user);
    }

    // 프로필 페이지 값 설정
    public ProfileDto buildProfileDto(User user) {
        validateUser(user);
        // 팔로잉 수, 팔로워 수 가져오기
        Long followingCount = followRepository.countFollowingByUser(user);
        Long followersCount = followRepository.countFollowerByFollowingMemberId(user.getId());
//        Long followersCount = followRepository.countFollowerByUser(user);

        // 현재 로그인한 사용자 확인
        String currentUsername = getCurrentUsername();
        log.info("currentUsername = {}", currentUsername);
        User currentUser = null;
        if (currentUsername.equals("anonymousUser")){
            currentUser = null;
        }else {
            currentUser = findUserByUsername(currentUsername);
        }

        // 팔로잉 여부
        boolean isFollowing = false;
        if (currentUser != null) {
            isFollowing = followRepository.existsByUserAndFollowingMemberId(currentUser, user.getId());
            log.info("isFollowing = {}", isFollowing);
        }

        ProfileDto profileDto = new ProfileDto(
                user.getId(),
                user.getProfileBannerImgFilename(),
                user.getProfileImgFilename(),
                user.getNickname(),
                user.getIntro(),
                followingCount,
                followersCount,
                isFollowing
        );

        return profileDto;
    }

    // 현재 인증된 사용자의 이름을 반환하는 메소드
    public String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // 토큰의 사용자 반환
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username)
                .orElseThrow(() -> new NotFoundMemberException("접근 권한이 없습니다."));
    }

    // 앨범 탭(기본), userId를 통해 프로필 이미지 클릭했을 때
    public ProfileSongDto getAlbumTab(Long userId) {
        return buildAlbumTab(userRepository.findById(userId).orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다.")));
    }

    // 앨범 탭(기본), nickname을 통해 마이페이지 버튼 클릭했을 때
    public ProfileSongDto getAlbumTabByNickname(String nickname) {
        return buildAlbumTab(userRepository.findByNickname(nickname).orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다.")));
    }

    // 앨범 탭(기본) 값 설정하는 메소드
    public ProfileSongDto buildAlbumTab(User user) {
        Long userId = user.getId();
        List<Song> songs = songRepository.findSongsByUserIdOrderByLikesAndId(userId);
        ProfileDto profileDto = getUserProfile(userId);

        if (songs.isEmpty()) {
            return new ProfileSongDto(
                    profileDto.getUserId(),
                    encodeFileToBase64(profileDto.getProfileBannerImgName(), true),
                    encodeFileToBase64(profileDto.getProfileImgName(), false),
                    profileDto.getNickname(),
                    profileDto.getIntro(),
                    profileDto.getFollowingCount(),
                    profileDto.getFollowersCount(),
                    profileDto.isFollowStatus()
            );
        }

        Song mainSong = songs.get(0);
        String songGenre = mainSong.getSongGenres().stream()
                .map(genre -> genre.getGenre().toString())
                .collect(Collectors.joining(", "));

        Album album = mainSong.getAlbum();
        String albumImg = (album != null) ? album.getAlbumImg() : null;

        List<ProfileAlbumDto> allAlbums = albumRepository.findAllByUserIdOrderByLatest(userId).stream()
                .map(a -> new ProfileAlbumDto(
                        encodeAlbumFileToBase64(a.getAlbumImg()),
                        a.getName()
                ))
                .collect(Collectors.toList());

        return new ProfileSongDto(
                profileDto.getUserId(),
                encodeFileToBase64(profileDto.getProfileBannerImgName(), true),
                encodeFileToBase64(profileDto.getProfileImgName(), false),
                profileDto.getNickname(),
                profileDto.getIntro(),
                profileDto.getFollowingCount(),
                profileDto.getFollowersCount(),
                profileDto.isFollowStatus(),
                mainSong.getTitle(),
                songGenre,
                mainSong.getLyricist(),
                mainSong.getComposer(),
                mainSong.getSongLikes().size(),
                encodeAlbumFileToBase64(albumImg),
                allAlbums
        );
    }

    // 라운지 탭을 보여주는 메소드
    public ProfileLoungeDto getLoungeTab(String nickname, int startPage) throws BoardNotExistException {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다."));

        PageRequest pageRequest = PageRequest.of(startPage, 10, Sort.by(Sort.Direction.DESC, "createdDt"));
        Page<Lounge> page = loungeRepository.findAllByNickname(nickname, pageRequest);
        List<Lounge> lounges = page.getContent();

        ProfileDto profileDto = getUserProfileByNickname(nickname);

        if (lounges.isEmpty()){
            return new ProfileLoungeDto(
                    profileDto.getUserId(),
                    encodeFileToBase64(profileDto.getProfileBannerImgName(), true),
                    encodeFileToBase64(profileDto.getProfileImgName(), false),
                    profileDto.getNickname(),
                    profileDto.getIntro(),
                    profileDto.getFollowingCount(),
                    profileDto.getFollowersCount(),
                    profileDto.isFollowStatus()
            );
        }
        Long userId = user.getId();
        List<ProfileLoungesForm> loungesForms = lounges.stream()
                .map(lounge -> {
                    // 각 라운지에 대한 세부 정보 가져오기
                    List<Tuple> details = queryRepository.getLoungeTab(userId);
                    DetailBaseDto detailBaseDto = details.getFirst().get(2, DetailBaseDto.class);

                    ProfileLoungesForm form = new ProfileLoungesForm(
                            lounge.getId(),
                            lounge.getUser().getNickname(),
                            lounge.getFilename(),
                            lounge.getCreatedDt(),
                            lounge.getLikes().size()
                    );
                    form.setComments(setCommentsAndReplies(user, lounge));          // 라운지 댓글 & 대댓글 셋팅
                    form.setBoardLikeStatus(detailBaseDto.getBoardLikeStatus());    // 사용자가 특정 게시판의 좋아요를 눌렀는지 여부
                    form.setIsBookmark(detailBaseDto.getIsBookmark());              // 사용자가 특정 게시판을 북마크했는지 여부
                    return form;
                })
                .collect(Collectors.toList());

        return new ProfileLoungeDto(
                profileDto.getUserId(),
                encodeFileToBase64(profileDto.getProfileBannerImgName(), true),
                encodeFileToBase64(profileDto.getProfileImgName(), false),
                profileDto.getNickname(),
                profileDto.getIntro(),
                profileDto.getFollowingCount(),
                profileDto.getFollowersCount(),
                profileDto.isFollowStatus(),
                loungesForms,
                startPage,
                page.getTotalPages(),
                page.getTotalElements()
        );
    }

    // 게시글 탭을 보여주는 메소드
    public ProfileBoardDto getBoardTab(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다."));

        // 유저가 작성한 게시글 조회
        List<Board> boards = boardRepository.findByNickname(nickname);

        // 게시글의 타입을 조회하여 BoardsTitle로 변환
        List<BoardsTitle> boardsTitles = boards.stream()
                .map(board -> new BoardsTitle(board.getId(), board.getTitle(), boardRepository.findBoardTypeById(board.getId())))
                .collect(Collectors.toList());

        // 북마크된 게시글 조회
        Long userId = user.getId();
        List<Board> bookmarkedBoards = boardRepository.findBookmarkedBoardsByUserId(userId);

        // 북마크의 타입을 조회하여 BookmarkTitle로 변환
        List<BookmarkTitle> bookmarks = bookmarkedBoards.stream()
                .map(board -> new BookmarkTitle(board.getId(), board.getTitle(), boardRepository.findBoardTypeById(board.getId())))
                .collect(Collectors.toList());

        ProfileDto profileDto = getUserProfile(userId);

        return new ProfileBoardDto(
                profileDto.getUserId(),
                encodeFileToBase64(profileDto.getProfileBannerImgName(), true),
                encodeFileToBase64(profileDto.getProfileImgName(), false),
                profileDto.getNickname(),
                profileDto.getIntro(),
                profileDto.getFollowingCount(),
                profileDto.getFollowersCount(),
                profileDto.isFollowStatus(),
                boardsTitles,
                bookmarks
        );
    }

    // 게시물 상세 정보를 가져오는 메소드
    public Map<String, Object> getBoardDetails(Long id) {
        Map<String, Object> postDetails = new HashMap<>();

        // 게시판 유형 조회
        Optional<Board> board = boardRepository.findById(id);
        if (board.isEmpty()) {
            throw new IllegalArgumentException("Invalid board ID: " + id);
        }

        String boardType = boardRepository.findBoardTypeById(id);
        log.info("boardType = {}", boardType);

        postDetails.put("boardType", boardType);
        return postDetails;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("유효하지 않은 유저 정보입니다.");
        }
    }

    // 플리넛 탭을 보여주는 메서드
    public ProfilePlayNutDto getPlayNutTab(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다."));
        Long userId = user.getId();
        List<PlayNut> playNuts = playNutRepository.findByUserId(userId);
        ProfileDto profileDto = getUserProfile(userId);

        if (playNuts.isEmpty()) {
            return new ProfilePlayNutDto(
                    profileDto.getUserId(),
                    profileDto.getProfileBannerImgName(),
                    profileDto.getProfileImgName(),
                    profileDto.getNickname(),
                    profileDto.getIntro(),
                    profileDto.getFollowingCount(),
                    profileDto.getFollowersCount(),
                    profileDto.isFollowStatus()
            );
        } else {
            List<ProfilePlayNutForm> playNutForms = playNuts.stream()
                    .map(playNut -> new ProfilePlayNutForm(playNut.getId(), playNut.getTitle()))
                    .collect(Collectors.toList());

            return new ProfilePlayNutDto(
                    profileDto.getUserId(),
                    profileDto.getProfileBannerImgName(),
                    profileDto.getProfileImgName(),
                    profileDto.getNickname(),
                    profileDto.getIntro(),
                    profileDto.getFollowingCount(),
                    profileDto.getFollowersCount(),
                    profileDto.isFollowStatus(),
                    playNutForms
            );
        }
    }

    // 플리넛 제목 클릭 시 해당 플리넛에 담긴 곡을 보여주는 메서드
    public ProfilePlayNutSongDto getPlayNutSongs(Long playNutId) {
        PlayNut playNut = playNutRepository.findById(playNutId)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 플리넛입니다."));

        User user = playNut.getUser();
        Long userId = user.getId();

        ProfileDto profileDto = getUserProfile(userId);

        List<ProfilePlayNutSongsForm> songDtos = playNut.getPlayNutMusics().stream()
                .map(playNutMusic -> {
                    Song song = songRepository.findById(playNutMusic.getSong().getId())
                            .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 곡입니다."));
                    return new ProfilePlayNutSongsForm(
                            song.getAlbum().getName(),
                            song.getUser().getNickname(),
                            song.getTitle()
                    );
                })
                .collect(Collectors.toList());

        return new ProfilePlayNutSongDto(
                new ProfileDto(
                        user.getId(),
                        encodeFileToBase64(profileDto.getProfileBannerImgName(), true),
                        encodeFileToBase64(profileDto.getProfileImgName(), false),
                        profileDto.getNickname(),
                        profileDto.getIntro(),
                        profileDto.getFollowingCount(),
                        profileDto.getFollowersCount(),
                        profileDto.isFollowStatus()
                ),
                songDtos
        );
    }
}
