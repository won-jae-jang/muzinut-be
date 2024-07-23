package nuts.muzinut.service.music;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.domain.music.*;
import nuts.muzinut.dto.music.*;
import nuts.muzinut.exception.AlbumCreateFailException;
import nuts.muzinut.exception.NoDataFoundException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.member.UserRepository;
import nuts.muzinut.repository.music.AlbumRepository;
import nuts.muzinut.repository.music.SongGenreRepository;
import nuts.muzinut.repository.music.SongRepository;
import nuts.muzinut.service.encoding.EncodeFiile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class AlbumService {

    private final AlbumRepository albumRepository;

    private final UserRepository userRepository;

    private final SongRepository songRepository;

    private final SongGenreRepository songGenreRepository;

    private final EncodeFiile encodeFiile;
    @PersistenceContext
    EntityManager em;

    @Value("${spring.file.dir}")
    private String fileDir;

    public String saveAlbumImg(MultipartFile albumImg) {
        String randomFileName = makeFileName();

        // 확장자 추출
        String fileType = getFileType(albumImg);

        File storeAlbumImg = new File(fileDir + "/albumImg/" + randomFileName + "." + fileType);

        // 저장 경로가 없으면 저장경로 생성
        File storeDir = new File(fileDir + "/albumImg/");
        if(!storeDir.exists()) storeDir.mkdirs();

        try {
            albumImg.transferTo(storeAlbumImg);
        }catch(IOException e) {
            e.printStackTrace();
        }

        // 랜덤 파일 이름 + 확장명 이름으로 설정
        randomFileName = randomFileName + "." + fileType;

        return randomFileName;
    }

    public AlbumDto saveSongs(List<MultipartFile> songFiles, AlbumDto albumData) {
        // 음원 파일 원본 이름 배열에 저장
        ArrayList<String> originSongFileName = new ArrayList<>();
        List<SongDto> songs = albumData.getSongs();
        for(SongDto song : songs) {
            originSongFileName.add(song.getOriginFilename());
        }

        // 원본 파일과 넘어온 파일 이름을 검증 후 저장
        // songs 에 있는 originFilename 은 저장된 이름으로 변경 처리
        for(int i=0; i<songFiles.size(); i++) {
            String origin = originSongFileName.get(i);
            if(origin.equals(songFiles.get(i).getOriginalFilename())) {
                String randomFileName = makeFileName();
                File storeSong = new File(fileDir + "/songFile/" + randomFileName + ".mp3");
                // 저장 경로가 없으면 저장경로 생성
                File storeDir = new File(fileDir + "/songFile/");
                if(!storeDir.exists()) storeDir.mkdirs();
                try {
                    songFiles.get(i).transferTo(storeSong);
                }catch(IOException e) {
                    e.printStackTrace();
                }

                // 저장될 파일 이름으로 변경
                String fileType = getFileType(songFiles.get(i));
                albumData.getSongs().get(i).setOriginFilename(randomFileName + "." + fileType);
            }
        }

        return albumData;
    }

    public Long saveAlbumData(AlbumDto storeAlbumData, String storeAlbumImg) {
        // userId 토큰에서 꺼내오기
        Long userId = getCurrentUsername();
        if(userId >= 0) {
            // Album Entity 저장
            Album album = new Album(userRepository.findById(userId).get(), storeAlbumData.getAlbumName(), storeAlbumData.getAlbumBio(), storeAlbumImg);
            albumRepository.save(album);

            // Song Entity, 장르 Entity 저장
            for(SongDto s : storeAlbumData.getSongs()) {
                // Song Entity 저장
                Song song = new Song(userRepository.findById(userId).get(), s.getSongName(),s.getLyrics(), s.getLyricist(), s.getComposer(), s.getOriginFilename(), album);
                songRepository.save(song);
                song.createMusic(userRepository.findById(userId).get());

                // 장르 Entity 저장
                List<String> stringGenres = s.getGenres();
                List<SongGenre> songGenres = new ArrayList<>();

                // 문자열로 받은 장르를 Enum 타입 리스트로 변환
                for(String g : stringGenres) {
                    Genre genre = Genre.valueOf(g.toUpperCase());
                    SongGenre songGenre = new SongGenre(genre);
                    songGenre.addMusicGenre(song);
                    songGenreRepository.save(songGenre);
                }
            }
            em.flush();
            return album.getId();
        }
        return null;
    }

    @SneakyThrows
    public void updateAlbumData(Long albumId, AlbumDto updatedAlbumData) {
        // 현재 인증된 사용자의 ID 가져오기
        Long userId = getCurrentUsername();

        // 기존 앨범 정보 가져오기
        Optional<Album> optionalAlbum = albumRepository.findById(albumId);
        if (optionalAlbum.isPresent()) {
            Album album = optionalAlbum.get();

            // 사용자 ID 검증
            if (album.getUser().getId().equals(userId)) {
                // 앨범 정보 업데이트


                // 변경 사항 저장
                albumRepository.save(album);
            } else {
                // 사용자 ID가 일치하지 않는 경우 예외 처리
                throw new IllegalAccessException("You do not have permission to update this album.");
            }
        } else {
            // 앨범이 존재하지 않는 경우 예외 처리
            throw new IllegalArgumentException("Album not found.");
        }
    }

    // 현재 인증된 사용자의 이름을 반환하는 메소드
    private Long getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String Username =  ((UserDetails) principal).getUsername();
            Optional<User> finduser = userRepository.findOneWithAuthoritiesByUsername(Username);
            finduser.orElseThrow(() -> new NotFoundMemberException("로그인을 해주세요"));
            User user = finduser.get();
            return user.getId();
        } else {
            return -1L;
        }
    }

    public String makeFileName() {
        String randomFileName;
        while (true) {
            randomFileName = UUID.randomUUID().toString();
            if (albumRepository.findByAlbumImg(randomFileName).isEmpty() &&
                    songRepository.findByFileName(randomFileName).isEmpty()) break;

        }
        return randomFileName;
    }

    public String getFileType(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    // 앨범 이미지 파일 삭제 메소드
    public void albumImgDelete(String albumImgName) {
        File file = new File(fileDir + "/albumImg/" + albumImgName);
        file.delete();
    }

    // 앨범 수정 메소드
    public void updateAlbum(Long albumId, MultipartFile albumImg, AlbumUpdateDto albumUpdateDto) {
        Long userId = getCurrentUsername();
        Optional<Album> optional = albumRepository.findByUser(albumId, userId);
        optional.orElseThrow(() -> new AccessDeniedException("이 유저는 수정 권한이 없습니다"));

        Album album = optional.get();
        String albumImgName = album.getAlbumImg();
        albumImgDelete(albumImgName);
        String updateAlbumImg = saveAlbumImg(albumImg);
        albumRepository.updateById(albumUpdateDto.getAlbumName(), albumUpdateDto.getAlbumBio(),
                updateAlbumImg, albumId);
    }

    // 앨범 삭제 메소드
    public void albumDelete(Long albumId) {
        Long userId = getCurrentUsername();
        Optional<Album> optional = albumRepository.findByUser(albumId, userId);
        optional.orElseThrow(() -> new AccessDeniedException("이 유저는 삭제 권한이 없습니다"));

        Album album = optional.get();
        String albumImgName = album.getAlbumImg();
        albumImgDelete(albumImgName);
        albumRepository.deleteById(albumId);
    }

    // 앨범상세페이지
    public ResponseEntity<AlbumDetaillResultDto> getAlbumDetail(Long id) {

        List<AlbumDetaillDto> albumDetaillDtos = albumRepository.albumDetail(id);
        List<AlbumSongDetaillDto> albumSongDetaillDtos = albumRepository.albumSongDetail(id);

        // 데이터가 없는 경우 예외 처리
        if (albumDetaillDtos.isEmpty() || albumSongDetaillDtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<AlbumSongDetaillDto> songs = new ArrayList<>();
        for (AlbumSongDetaillDto albumSongDetaillDto : albumSongDetaillDtos) {
            songs.add(albumSongDetaillDto);
        }
        AlbumDetaillDto result = albumDetaillDtos.get(0);
        File file = new File(fileDir + "/albumImg/" + result.getAlbumImg());
        // 파일이 없는 경우 예외 처리
        if (!file.exists() || !file.isFile()) {
            throw new NoDataFoundException("파일이 존재 하지 않습니다");
        }
        try {
            String en = encodeFiile.encodeFileToBase64(file);
            result.setAlbumImg(en);
            AlbumDetaillResultDto totalData = new AlbumDetaillResultDto(
                    result.getName(), result.getAlbumImg(),
                    result.getNickname(), result.getIntro(),
                    songs
            );
            return new ResponseEntity<AlbumDetaillResultDto>(totalData, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 음원 파일 올릴때 각 음원의 파일 이름은 같으면 안된다!
    // 음원 파일 이름이 각 음원파일의 이름이 같다면 Exception
    public void isSongFileSameName(List<MultipartFile> songFiles) {
        for(int i=0; i<songFiles.size(); i++) {
            for(int j=i+1; j<songFiles.size(); j++) {
                if(songFiles.get(i).getOriginalFilename().equals(songFiles.get(j).getOriginalFilename())) throw new AlbumCreateFailException("각 음원 파일의 이름은 같을 수 없습니다.");
            }
        }
    }
}

