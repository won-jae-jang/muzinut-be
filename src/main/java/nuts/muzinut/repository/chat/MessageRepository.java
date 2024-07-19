package nuts.muzinut.repository.chat;

import nuts.muzinut.domain.chat.Chat;
import nuts.muzinut.domain.chat.Message;
import nuts.muzinut.domain.member.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    //채팅방에 두명이 접속했을 때 모든 메시지를 읽음 상태로 처리한다.
    @Modifying
    @Query("update Message m set m.isRead = true where m.chat = :chat")
    void updateAllRead(@Param("chat") Chat chat);

    /**
     * 채팅창에 한명이 접속하는 경우 (다른 한명은 접속을 안한 상태일 때)
     * 한 채팅방에 a 와 b 라는 유저가 있다고 할때 a 가 채팅방에 입장하면 b 가 보낸 기존의 메시지를 읽음 처리한다.
     * @param chat: a, b의 채팅방
     * @param user: 채팅방에 입장한 유저
     */
    @Modifying
    @Query("update Message m set m.isRead = true where m.chat = :chat and m.user != :user")
    void updateOnePersonRead(@Param("chat") Chat chat, @Param("user") User user);

    @Query("select m from Message m join fetch m.user where m.chat = :chat order by m.sendTime desc")
    List<Message> findChatRoomMessages(@Param("chat") Chat chat);
}
