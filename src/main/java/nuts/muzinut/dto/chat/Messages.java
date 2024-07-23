package nuts.muzinut.dto.chat;

import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Builder
public class Messages {
    private Long id;
    private String nickname;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime sendTime;
    private String message;
    private String profileImg;
}
