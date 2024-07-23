package nuts.muzinut.controller.chat;

import lombok.RequiredArgsConstructor;
import nuts.muzinut.domain.chat.Chat;
import nuts.muzinut.domain.chat.Message;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.MessageDto;
import nuts.muzinut.dto.chat.*;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.service.chat.ChatService;
import nuts.muzinut.service.chat.MessageService;
import nuts.muzinut.service.member.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final MessageService messageService;

    @MessageMapping("/{roomId}")
    @SendTo("/room/{roomId}") //메시지 전송 (목적지)
    public ChatMessage chat(@DestinationVariable Long roomId, @Validated ChatMessage message) {
        Message createMessage = messageService.createMessage(roomId, message.getSender(), message.getSendTo(), message.getMessage());
        return ChatMessage.builder()
                .id(roomId)
                .sender(createMessage.getUser().getNickname())
                .message(createMessage.getContent())
                .build();
    }

    //채팅방 생성
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/room")
    public CreateChatDto createChatRoom(@RequestBody @Validated CreateChatForm form) {

        User user = userService.getUserWithUsername().orElseThrow(() -> new NotFoundMemberException("채팅방을 만들 수 없습니다"));
        Chat chat = chatService.createRoom(user.getNickname(), form.getSendTo());
        messageService.createMessage(chat.getId(), user.getNickname(), form.getSendTo(), form.getMessage()); //메시지 생성

        return CreateChatDto.builder()
                    .user1(user.getNickname())
                    .user2(form.getSendTo())
                    .chatRoomId(chat.getId()).build();
    }

    @ResponseBody
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/room-out")
    public MessageDto outRoom(@RequestParam("roomId") String id) {
        User user = userService.getUserWithUsername().orElseThrow(() -> new NotFoundMemberException("채팅방을 만들 수 없습니다"));
        chatService.disconnectChatRoom(id, user.getUsername());
        return new MessageDto(user.getNickname() + "님이 " + id + "채팅방을 퇴장하였습니다");
    }

    // 맞팔된 사용자 목록을 가져오는 메서드
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/mutual-follows")
    public ResponseEntity<?> getMutualFollows() {
        User user = userService.getUserWithUsername().orElseThrow(() -> new NotFoundMemberException("사용자를 찾을 수 없습니다"));
        List<MutualFollow> mutualFollows = chatService.getMutualFollowUsers(user.getId());

        if (mutualFollows.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.ok(mutualFollows);
        }
    }

    //채팅방 리스트를 가져오는 메서드
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/my-chat")
    public ResponseEntity<?> getChatRooms() {
        User user = userService.getUserWithUsername().orElseThrow(() -> new NotFoundMemberException("사용자를 찾을 수 없습니다"));
        List<ChatRoomListDto> myChatRooms = chatService.getMyChatRooms(user);
        if (myChatRooms.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new MessageDto("진행중인 채팅목록이 없습니다"));
        }
        return ResponseEntity.ok(myChatRooms);
    }

}
