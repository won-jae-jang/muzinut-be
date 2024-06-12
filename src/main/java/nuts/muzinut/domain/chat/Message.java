package nuts.muzinut.domain.chat;

import jakarta.persistence.*;
import lombok.Getter;
import nuts.muzinut.domain.member.Member;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Message {

    @Id @GeneratedValue
    @Column(name = "message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "chat_id")
    private Chat chat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String message;
    private int read; //읽은 사람 수

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime send_time;

    @OneToMany(mappedBy = "message")
    List<ReadMessage> readMessages = new ArrayList<>();

    //연관 관계 메서드

    /**
     * @param member:  채팅 작성자
     * @param chat:    채팅방
     * @param message: 메시지
     */
    public void createMessage(Member member, Chat chat, String message) {
        this.member = member;
        this.chat = chat;
        this.message = message;
        this.send_time = LocalDateTime.now();
        member.getMessages().add(this);
        chat.getMessages().add(this);
    }

    //비지니스 로직

}
