package nuts.muzinut.dto.chat;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class ChatRoomListDto {
    private Long id; //채팅방 pk
    private Long notReadMessages = 0L; //읽지 않은 메시지
    private String profileImg; //메시지를 보낸 상대방의 프로필 이미지
    private String nickname; //메시지를 보낸 상대방의 닉네임
    private Long userId; //메시지를 보낸 상대방의 pk

    @QueryProjection
    public ChatRoomListDto(Long id, Long notReadMessages, String profileImg, String nickname, Long userId) {
        this.id = id;
        this.notReadMessages = notReadMessages;
        this.profileImg = profileImg;
        this.nickname = nickname;
        this.userId = userId;
    }
}
