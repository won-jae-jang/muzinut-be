package nuts.muzinut.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.chat.Chat;
import nuts.muzinut.domain.chat.ChatMember;
import nuts.muzinut.domain.chat.Message;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.exception.NotFoundEntityException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.chat.ChatMemberRepository;
import nuts.muzinut.repository.chat.ChatRepository;
import nuts.muzinut.repository.chat.MessageRepository;
import nuts.muzinut.repository.chat.ReadMessageRepository;
import nuts.muzinut.repository.member.UserRepository;
import nuts.muzinut.service.member.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    /**
     * @param user1: 채팅방 첫번째 참가 유저 (닉네임)
     * @param user2: 채팅방 두번째 참가 유저 (닉네임)
     * @return
     */
    public Chat createRoom(String user1, String user2) {
        List<User> users = userRepository.findUsersByNickname(user1, user2);

        if (users.size() != 2) {
            throw new NotFoundMemberException("채팅은 두명의 회원과 이루어져야 합니다");
        }

        Chat chat = chatRepository.save(new Chat()); //새로운 채팅방 생성
        for (User user : users) {
            ChatMember chatMember = new ChatMember(user, chat); //user 채팅방 참가
            chatMemberRepository.save(chatMember);
        }
        return chat;
    }

    public void connectChatRoom(String chatRoomNumber, String username) {
//        log.info("chatService connectChatRoom");
        List<String> redisData = redisUtil.getMultiData(chatRoomNumber);

        //채팅방에 접속한 유저가 없는 경우
        if (redisData.isEmpty()) {
            log.info("채팅방 1명 접속");
            redisUtil.setMultiData(chatRoomNumber, username);
        } else if (redisData.size() == 1 && !redisData.getFirst().equals(username)){
            log.info("채팅방 2명 접속");
            log.info("이미 접속중인 사용자: {}", redisData.getFirst());
            redisUtil.setMultiData(chatRoomNumber, username);
        }
    }

    public void disconnectChatRoom(String chatRoomNumber, String username) {
        log.info("chatService disconnectChatRoom");
        List<String> redisData = redisUtil.getMultiData(chatRoomNumber);

        //기존에 두명의 채팅방 참가자가 있는 경우
        if (redisData.size() == 2 && redisData.contains(username)) {
            log.info("채팅방 2명중 한명 퇴장");
            String user = redisData.stream().filter(Predicate.not(r -> r.equals(username))).toString(); //채팅방을 나가지 않은 사용자
            redisUtil.setMultiData(chatRoomNumber, user); //덮어쓰기

        } else if (redisData.size() == 1 && redisData.getFirst().equals(username)) {
            log.info("채팅방 모두 퇴장");
            redisUtil.deleteData(chatRoomNumber);
        } else {
            log.info("채팅방에 접속중인 사용자가 없었음");
        }
    }
}