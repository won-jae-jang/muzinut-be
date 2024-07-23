package nuts.muzinut.service.scheduler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.repository.member.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@AllArgsConstructor
public class VoteResetService {
    final private UserRepository userRepository;

    @Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul") // 매 월 1일 실행(한국 시간 기준)
    public void voteReset() {

        try{
            userRepository.resetUserReceiveVote(); // 득표 초기화
            log.info("user receive vote reset success");
        }catch (Exception e) {
            log.error("receive vote reset error", e);
        }

        try{
            userRepository.resetUserVote(); // 투표권 갯수 10개 이하 > 10개로 초기화
            log.info("user remain vote reset success");
        }catch (Exception e){
            log.error("remain vote reset error", e);
        }

    }
}
