package nuts.muzinut.domain.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Chat {

    @Id @GeneratedValue
    @Column(name = "chat_id")
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime created_dt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private ChatStatus chatStatus = ChatStatus.VALID;

    //연관 관계
    @OneToMany(mappedBy = "chat")
    List<ChatMember> chatMembers = new ArrayList<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL) //채팅방이 없어지면 해당 채팅방의 메시지들 모두 삭제
    List<Message> messages = new ArrayList<>();

    //비지니스 로직
    public Chat freezeChatRoom() {
        this.chatStatus = ChatStatus.INVALID;
        return this;
    }

    public Chat meltChatRoom() {
        this.chatStatus = ChatStatus.VALID;
        return this;
    }
}
