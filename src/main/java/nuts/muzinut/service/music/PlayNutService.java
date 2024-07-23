package nuts.muzinut.service.music;

import lombok.RequiredArgsConstructor;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.domain.music.PlayNut;
import nuts.muzinut.domain.music.PlayNutMusic;
import nuts.muzinut.domain.music.Song;
import nuts.muzinut.dto.music.*;
import nuts.muzinut.exception.LimitPlayNutException;
import nuts.muzinut.exception.NoDataFoundException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.member.UserRepository;
import nuts.muzinut.repository.music.PlayNutMusicRepository;
import nuts.muzinut.repository.music.PlayNutRepository;
import nuts.muzinut.repository.music.SongRepository;
import nuts.muzinut.service.encoding.EncodeFiile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlayNutService {

    private final UserRepository userRepository;
    private final PlayNutRepository playNutRepository;
    private final PlayNutMusicRepository playNutMusicRepository;
    private final SongRepository songRepository;
    private final EncodeFiile encodeFiile;

    @Value("${spring.file.dir}")
    private String fileDir;

    // 플리넛 디렉토리 생성
    public void savePlayNut(PlaynutTitleDto data) {
        User user = getUser();
        List<PlayNutDto> playNutDirCount = playNutRepository.findByPlayNut(user);
        if (playNutDirCount.size() < 10){
            PlayNut playNut = new PlayNut(data.getTitle(), user);
            playNutRepository.save(playNut);
        }else {
            throw new LimitPlayNutException("플리넛 생성은 10개 까지입니다");
        }

    }

    // 플리넛의 곡 추가
    public void saveMusic(Long playNutId, Long songId) {
        User user = getUser();
        Optional<PlayNut> optional = playNutRepository.findById(playNutId);
        optional.orElseThrow(() -> new NoDataFoundException("플리넛이 없습니다"));
        if (!optional.get().getUser().equals(user)) {
            throw new AccessDeniedException("플리넛 곡 추가 권한이 없습니다");
        }
        List<PlayNutMusicDto> findMusicCount = playNutMusicRepository.findPlayNutMusic(playNutId);
        if (findMusicCount.size() < 1000){
            PlayNut playNut = optional.get();
            Optional<Song> bySongId = songRepository.findById(songId);
            bySongId.orElseThrow(() -> new NoDataFoundException("곡이 없습니다"));
            PlayNutMusic playNutMusic = new PlayNutMusic(playNut, bySongId.get());
            playNutMusicRepository.save(playNutMusic);
        }else {
            throw new LimitPlayNutException("플리넛 곡 추가는 1000개 까지입니다");
        }

    }

    // 플리넛 디렉토리 이름 변경
    public void updatePlayNut(Long playNutId, PlaynutTitleDto data) {
        User user = getUser();
        Optional<PlayNut> optional = playNutRepository.findById(playNutId);
        optional.orElseThrow(() -> new NoDataFoundException("플리넛이 없습니다"));
        if (!optional.get().getUser().equals(user)) {
            throw new AccessDeniedException("플리넛 이름 변경 권한이 없습니다");
        }
        playNutRepository.updateByTitle(data.getTitle(), playNutId);
    }

    // 플리넛 디렉토리 삭제
    public void playNutDelete(Long playNutId) {
        User user = getUser();
        Optional<PlayNut> optional = playNutRepository.findById(playNutId);
        optional.orElseThrow(() -> new NoDataFoundException("플리넛이 없습니다"));
        if (!optional.get().getUser().equals(user)) {
            throw new AccessDeniedException("플리넛 삭제 권한이 없습니다");
        }
        playNutRepository.deleteById(playNutId);
    }

    // 플리넛 곡 삭제
    public void playNutMusicDelete(Long playNutId, Long playNutMusicId) {
        User user = getUser();
        Optional<PlayNut> optional = playNutRepository.findById(playNutId);
        optional.orElseThrow(() -> new NoDataFoundException("플리넛이 없습니다"));
        if (!optional.get().getUser().equals(user)) {
            throw new AccessDeniedException("플리넛 곡 삭제에 대한 권한이 없습니다");
        }
        playNutMusicRepository.deleteByPlayNutIdANDPlayNutMusicId(playNutId, playNutMusicId);
    }

    // 플리넛 디렉토리 조회
    @Transactional(readOnly = true)
    public ResponseEntity<PlayNutListDto> findPlayNutDir() {
        User user = getUser();
        List<PlayNutDto> playNutDir = playNutRepository.findByPlayNut(user);
        if (playNutDir.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<PlayNutListDto>(new PlayNutListDto(playNutDir), HttpStatus.OK);
    }

    // 플리넛 곡 조회
    @Transactional(readOnly = true)
    public ResponseEntity<PlayNutMusicListDto> findPlayNutMusic(Long playNutId) {
        Optional<PlayNut> optional = playNutRepository.findById(playNutId);
        optional.orElseThrow(() -> new NoDataFoundException("플리넛이 없습니다"));
        User user = getUser();
        if (!optional.get().getUser().equals(user)){
            throw new AccessDeniedException("해당 플리넛 조회 권한이 없습니다");
        }
        List<PlayNutMusicDto> totalData = playNutMusicRepository.findPlayNutMusic(playNutId);
        if (totalData.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<PlayNutMusicDto> playNutMusicDtos = new ArrayList<>();
        for (PlayNutMusicDto totalDatum : totalData) {
            try {
                String encodedFile = encodeFiile.encoding(totalDatum.getAlbumImg(),"album");
                totalDatum.setAlbumImg(encodedFile);
                playNutMusicDtos.add(totalDatum);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return new ResponseEntity<PlayNutMusicListDto>(new PlayNutMusicListDto(playNutMusicDtos), HttpStatus.OK);
    }

    // 현재 인증된 사용자의 이름을 반환하는 메소드
    private User getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String Username =  ((UserDetails) principal).getUsername();
            Optional<User> finduser = userRepository.findOneWithAuthoritiesByUsername(Username);
            finduser.orElseThrow(() -> new NotFoundMemberException("로그인을 해주세요"));
            User user = finduser.get();
            return user;
        } else {
            return null;
        }
    }
}
