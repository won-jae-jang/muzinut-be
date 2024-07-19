package nuts.muzinut.domain.baseEntity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
public abstract class BaseTimeEntity {
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDt;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedDt;

    @PrePersist // 엔티티가 처음 생성될 때의 시간을 자동으로 기록
    public void prePersist() {
        this.createdDt = LocalDateTime.now();
        this.modifiedDt = LocalDateTime.now();
    }

    @PreUpdate  // 엔티티가 수정될 때마다 수정 시간을 자동으로 기록
    public void preUpdate() {
        this.modifiedDt = LocalDateTime.now();
    }
}


