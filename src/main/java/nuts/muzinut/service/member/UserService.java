package nuts.muzinut.service.member;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.common.Base64Encoding;
import nuts.muzinut.domain.member.Authority;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.domain.music.Playlist;
import nuts.muzinut.dto.member.UserDto;
import nuts.muzinut.exception.DuplicateMemberException;
import nuts.muzinut.exception.InvalidPasswordException;
import nuts.muzinut.exception.NotFoundEntityException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.member.AuthorityRepository;
import nuts.muzinut.repository.member.UserRepository;
import nuts.muzinut.repository.music.PlaylistRepository;
import nuts.muzinut.service.security.SecurityRole;
import nuts.muzinut.service.security.SecurityUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class UserService extends Base64Encoding {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PlaylistRepository playlistRepository;
    private final PasswordEncoder passwordEncoder;

    // 프로필 이미지 설정
    public void setProfileName(String filename, User user) {
        userRepository.updateFilename(filename, user);
    }

    //username 으로 회원 찾기
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new NotFoundEntityException("없는 회원입니다"));
    }

    // 프로필 닉네임, 자기소개 설정
    @Transactional
    public void updateNicknameAndIntro(Long userId, String nickname, String intro) {
//        userRepository.updateNicknameAndIntro(userId, nickname, intro);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundMemberException("회원이 아닙니다."));
        user.updateNicknameAndIntro(nickname, intro);
    }

    // 중복 닉네임 확인
    public boolean isDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean checkDuplicateNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean checkAlreadyExistUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    // 프로필 배너 이미지 설정
    public void setProfileBannerName(String filename, User user){
        userRepository.updateProfileBannerImg(filename, user);
    }

    //닉네임으로 유저 찾기
    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public String findProfileImage(String profileImg) {
        return encodeFileToBase64(profileImg);
    }

    /**
     *
     * @param user: 비밀번호를 바꾸고 싶어하는 유저
     * @param currentPassword: 현재 비밀번호
     * @param newPassword: 바꿀 비밀번호
     */
    public void updatePassword(User user, String currentPassword, String newPassword) {
        if (validatePassword(user, currentPassword)) {
            log.info("correct password");
            user.updatePassword(passwordEncoder.encode(newPassword));
        }
        throw new InvalidPasswordException("비밀번호가 맞지 않습니다");
    }

    //비밀번호 바꾸기 (비밀번호 찾기에서 활용)
    public void updatePassword(User user, String newPassword) {
        user.updatePassword(passwordEncoder.encode(newPassword));
    }


    //비밀번호 유효성 검사
    public Boolean validatePassword(User user, String currentPassword) {
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return false;
        }
        return true;
    }

    //회원인지 검증 (비밀번호 찾기에 사용)
    public boolean isUser(String username) {
        Optional<User> findUser = userRepository.findByUsername(username);
        if (findUser.isEmpty()) {
            return false;
        }
        return true;
    }

    public User findByEmail(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundMemberException("없는 회원입니다"));
    }

    //랜덤 닉네임을 생성해주는 메서드
    public String randomNickname() {
        return UUID.randomUUID().toString();
    }

    /**
     * 일반 회원가입
     * @throws : 이미 가입된 회원이 동일한 username 으로 회원가입 한 경우 DuplicateMemberException 발생
     */
    public UserDto signup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new DuplicateMemberException("이미 가입되어 있는 유저입니다.");
        }

        //권한 설정
        Authority authority = getAuthority(SecurityRole.ROLE_USER);

        //유저 생성
        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        makePlayList(user); //플레이 리스트 만들기

        return UserDto.from(userRepository.save(user));
    }


    /**
     * 관리자 회원가입
     * @throws : 이미 가입된 회원이 동일한 username 으로 회원가입 한 경우 DuplicateMemberException 발생
     */
    public UserDto adminSignup(UserDto userDto) {
        if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
            throw new DuplicateMemberException("이미 가입되어 있는 유저입니다.");
        }

        //권한 설정
        Authority authority = getAuthority(SecurityRole.ROLE_ADMIN);

        //유저 생성
        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .authorities(Collections.singleton(authority))
                .activated(true)
                .build();

        makePlayList(user); //플레이 리스트 만들기

        return UserDto.from(userRepository.save(user));
    }

    private void makePlayList(User user) {
        Playlist playlist = new Playlist();
        playlist.createPlaylist(user);
        playlistRepository.save(playlist);
    }

    private Authority getAuthority(SecurityRole role) {
        Authority authority = Authority.builder()
                .authorityName(role.toString())
                .build();
        authorityRepository.save(authority);
        return authority;
    }

    @Transactional(readOnly = true)
    public UserDto getUserWithAuthorities(String username) {
        return UserDto.from(userRepository.findOneWithAuthoritiesByUsername(username).orElse(null));
    }

    @Transactional(readOnly = true)
    public UserDto getMyUserWithAuthorities() {
        return UserDto.from(
                SecurityUtil.getCurrentUsername()
                        .flatMap(userRepository::findOneWithAuthoritiesByUsername)
                        .orElseThrow(() -> new NotFoundMemberException("Member not found"))
        );
    }

    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * 토큰을 토대로 로그인 한 사용자인지 검증
     * @throws NotFoundMemberException: 스프링 시큐리티가 관리하지 않는 회원일 경우 exception 발생
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithUsername() {
        return userRepository.findByUsername(
                SecurityUtil.getCurrentUsername()
                        .orElseThrow(() -> new NotFoundMemberException("로그인 하지 않았거나 없는 회원입니다")));
    }

    // 토큰의 사용자 이름 반환
    @Transactional(readOnly = true)
    public User findUserByUsername(String username) {
        return userRepository.findOneWithAuthoritiesByUsername(username)
                .orElseThrow(() -> new NotFoundMemberException("Member not found"));
    }

}
