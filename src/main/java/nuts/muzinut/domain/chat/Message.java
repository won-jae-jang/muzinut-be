package nuts.muzinut.domain.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import nuts.muzinut.domain.member.User;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "message")
public class Message {

    @Id @GeneratedValue
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String content;
    private Boolean isRead;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    @OneToMany(mappedBy = "message")
    List<ReadMessage> readMessages = new ArrayList<>();

    //연관 관계 메서드

    /**
     * @param user:    채팅 작성자
     * @param chat:    채팅방
     * @param content: 메시지
     */
    public Message(User user, Chat chat, String content) {
        this.user = user;
        this.chat = chat;
        this.content = content;
        this.sendTime = LocalDateTime.now();
        user.getMessages().add(this);
        chat.getMessages().add(this);
    }

    //비지니스 로직
    public Message createNotReadMessage(User user, Chat chat, String message) {
        this.user = user;
        this.chat = chat;
        this.content = message;
        this.sendTime = LocalDateTime.now();
        this.isRead = false; //메시지를 상대방이 읽지 않음
        user.getMessages().add(this);
        chat.getMessages().add(this);
        return this;
    }

    public Message createReadMessage(User user, Chat chat, String message) {
        this.user = user;
        this.chat = chat;
        this.content = message;
        this.sendTime = LocalDateTime.now();
        this.isRead = true; //메시지를 상대방이 읽음
        user.getMessages().add(this);
        chat.getMessages().add(this);
        return this;
    }
}
