package nuts.muzinut.repository.member;

import nuts.muzinut.domain.member.Follow;
import nuts.muzinut.domain.member.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 특정 유저의 팔로잉 수를 계산하는 메서드
    Long countFollowingByUser(User user);

    // 특정 유저의 팔로워 수를 계산하는 메서드
    Long countFollowerByUser(User user);

    /**
     * @param notification: 알림 여부, false:끄기, true:켜기
     * @param user: 알림 설정을 희망하는 회원
     * @param followingMemberId: 알림 설정을 하고 싶은 대상
     */
    // 팔로우 알림 상태를 업데이트하는 메소드
    @Modifying
    @Query("update Follow f set f.notification = :notification where f.user = :user and f.followingMemberId = :followingMemberId")
    void updateNotificationStatus(@Param("notification") Boolean notification, @Param("user") User user, @Param("followingMemberId") Long followingMemberId);

    // 특정 유저가 팔로우한 Follow 객체 리스트를 가져오는 메서드
    List<Follow> findByUser(User user);

    /**
     * @param user: 팔로잉 하는 주체
     * @param followingMemberId: 팔로잉 하는 대상
     * @return 팔로우 여부
     */
    // 특정 유저가 특정 회원을 팔로우하고 있는지 확인하는 메소드
    boolean existsByUserAndFollowingMemberId(User user, Long followingMemberId);

    // 팔로워(특정 유저를 팔로우한 회원의) 리스트를 반환하는 메소드
    @Query("select f from Follow f where f.followingMemberId = :userId")
    List<Follow> findFollowersByUserId(@Param("userId") Long userId);

    /**
     * @param user: 팔로잉 하는 주체
     * @return 팔로잉 ID 리스트
     */
    // 특정 유저가 팔로우한 회원의 ID 리스트를 가져오는 메서드
    @Query("select f.followingMemberId from Follow f where f.user = :user")
    List<Long> findFollowingMemberIdsByUser(@Param("user") User user);

    /**
     * @param user: 팔로잉 취소하는 주체
     * @param followingMemberId: 팔로잉 취소하는 대상
     */
    // 유저 팔로우 취소 메서드
    @Modifying
    @Query("delete from Follow f where f.user = :user and f.followingMemberId = :followingMemberId")
    void unfollow(@Param("user") User user, @Param("followingMemberId") Long followingMemberId);

    // 팔로우 관계를 확인하는 메서드
    Optional<Follow> findByUserAndFollowingMemberId(User user, Long followingMemberId);

    /**
     * @param userId: 유저 ID
     * @return 맞팔 되어 있는 사용자 리스트
     */
    // 맞팔 관계를 확인하는 메서드
    @Query("select f1.followingMemberId from Follow f1 join Follow f2 " +
            "on f1.user.id = f2.followingMemberId and f1.followingMemberId = f2.user.id " +
            "where f1.user.id = :userId")
    List<Long> findMutualFollowIds(@Param("userId") Long userId);
}
