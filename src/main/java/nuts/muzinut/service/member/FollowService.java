package nuts.muzinut.service.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.member.Follow;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.MessageDto;
import nuts.muzinut.dto.member.follow.FollowListDto;
import nuts.muzinut.repository.member.FollowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserService userService;

    // 팔로우 및 언팔로우 토글 메소드
    @Transactional
    public ResponseEntity<MessageDto> toggleFollow(User user, Long followingUserId) {
        validateUser(user);
        validateUserId(followingUserId);

        // 자기 자신을 팔로우하려는 경우 예외 처리
        if (user.getId().equals(followingUserId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageDto("자기 자신을 팔로우할 수 없습니다."));
        }

        User followingUser = userService.findUserById(followingUserId);
        validateUser(followingUser);

        if (!(isFollowing(user, followingUser))) {
            // 언팔로잉 상태면(기본) 팔로잉
            Follow follow = new Follow();
            follow.createFollowing(user, followingUser);
            followRepository.save(follow);
            return ResponseEntity.ok().body(new MessageDto("팔로잉 되었습니다."));
        } else {
            followRepository.unfollow(user, followingUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(new MessageDto("언팔로잉 되었습니다."));
        }
    }

    // 특정 유저의 `팔로잉` 수를 반환하는 메소드
    public Long countFollowing(User user) {
        validateUser(user);
        return followRepository.countFollowingByUser(user);
    }

    // 특정 유저의 `팔로워` 수를 반환하는 메소드
    public Long countFollowers(User user) {
        validateUser(user);
        return followRepository.countFollowerByUser(user);
    }

    // 팔로우 알림 토글 메서드
    @Transactional
    public ResponseEntity<MessageDto> toggleNotification(User user, Long userId) {
        validateUser(user);
        validateUserId(userId);
        Follow follow = followRepository.findByUserAndFollowingMemberId(user, userId)
                .orElseThrow(() -> new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));

        if (follow.getNotification()) {
            follow.turnOffNotification();
            followRepository.save(follow);
            return ResponseEntity.ok().body(new MessageDto("알림이 꺼졌습니다."));
        } else {
            follow.turnOnNotification();
            followRepository.save(follow);
            return ResponseEntity.ok().body(new MessageDto("알림이 켜졌습니다."));
        }
    }

    // 특정 유저가 특정 회원을 팔로우하고 있는지 확인하는 메소드
    public boolean isFollowing(User user, User followingMember) {
        validateUser(user);
        validateUser(followingMember);
        Long followingMemberid = followingMember.getId();
        return followRepository.existsByUserAndFollowingMemberId(user, followingMemberid);
    }

    // 팔로잉(특정 유저가 팔로우한 회원의) 리스트를 반환하는 메소드
    public List<FollowListDto> getFollowingList(User user) {
        validateUser(user);
        List<Follow> followings = followRepository.findByUser(user);
        return followings.stream()
                .map(follow -> {
                    User followingUser = userService.findUserById(follow.getFollowingMemberId());
                    return new FollowListDto(followingUser);
                })
                .collect(Collectors.toList());
    }

    // 팔로워(특정 유저를 팔로우한 회원의) 리스트를 반환하는 메소드
    public List<FollowListDto> getFollowerList(Long userId) {
        validateUserId(userId);
        List<Follow> followers = followRepository.findFollowersByUserId(userId);
        return followers.stream()
                .map(follow -> new FollowListDto(follow.getUser()))
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("유효하지 않은 유저 정보입니다.");
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 유저 ID입니다.");
        }
    }
}
