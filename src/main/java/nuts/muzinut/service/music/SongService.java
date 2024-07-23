package nuts.muzinut.service.music;

import lombok.RequiredArgsConstructor;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.domain.music.Genre;
import nuts.muzinut.domain.music.Song;
import nuts.muzinut.domain.music.SongGenre;
import nuts.muzinut.domain.music.SongLike;
import nuts.muzinut.dto.music.*;
import nuts.muzinut.dto.page.PageDto;
import nuts.muzinut.exception.NoDataFoundException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.member.UserRepository;
import nuts.muzinut.repository.music.SongGenreRepository;
import nuts.muzinut.repository.music.SongLikeRepository;
import nuts.muzinut.repository.music.SongRepository;
import nuts.muzinut.service.encoding.EncodeFiile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

@RequiredArgsConstructor
@Service
public class SongService {

    @Value("${spring.file.dir}")
    private String fileDir;

    private final SongRepository songRepository;
    private final SongGenreRepository songGenreRepository;
    private final AlbumService albumService;
    private final EncodeFiile encodeFiile;
    private final UserRepository userRepository;
    private final SongLikeRepository songLikeRepository;

    //최신음악페이지
    public ResponseEntity<PageDto<SongPageDto>> getNewSongs(int pageable) {
        // 페이지 넘버가 0이하 인 경우 0으로 저장, 1이상인 경우 1을 뺌 값을 저장
        if (pageable < 1 ) {
            pageable = 0;
        } else {
            pageable = pageable - 1;
        }

        PageRequest pageRequest = PageRequest.of(pageable,20);
        Page<SongPageDto> page = songRepository.new100Song(pageRequest);

        // 데이터가 없는 경우 예외 처리
        if (!page.hasContent()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<SongPageDto> content = page.getContent();
        try {
            List<SongPageDto> songList = getAlbumImg(content);
            PageDto<SongPageDto> totalData = (PageDto<SongPageDto>) new PageDto<>(
                    songList,
                    page.getNumber(),
                    page.getContent().size(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
            return new ResponseEntity<PageDto<SongPageDto>>(totalData, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    //인기TOP100페이지
    public ResponseEntity<PageDto<SongPageDto>> getHotTOP100Songs(int pageable) {
        // 페이지 넘버가 0이하 인 경우 0으로 저장, 1이상인 경우 1을 뺌 값을 저장
        if (pageable < 1 ) {
            pageable = 0;
        } else {
            pageable = pageable - 1;
        }

        PageRequest pageRequest = PageRequest.of(pageable, 20);
        Page<SongPageDto> page = songRepository.hotTOP100Song(pageRequest);

        // 데이터가 없는 경우 예외 처리
        if (!page.hasContent()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<SongPageDto> content = page.getContent();
        try {
            List<SongPageDto> songList = getAlbumImg(content);
            PageDto<SongPageDto> totalData = (PageDto<SongPageDto>) new PageDto<>(
                    songList,
                    page.getNumber(),
                    page.getContent().size(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
            return new ResponseEntity<PageDto<SongPageDto>>(totalData, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //장르별음악페이지
    public ResponseEntity<PageDto<SongPageDto>> getGenreSongs(int pageable, String genre) {
        // 페이지 넘버가 0이하 인 경우 0으로 저장, 1이상인 경우 1을 뺌 값을 저장
        if (pageable < 1 ) {
            pageable = 0;
        } else {
            pageable = pageable - 1;
        }

        PageRequest pageRequest = PageRequest.of(pageable, 20);
        Page<SongPageDto> page = songRepository.genreSong(genre,pageRequest);

        // 데이터가 없는 경우 예외 처리
        if (!page.hasContent()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<SongPageDto> content = page.getContent();
        try {
            List<SongPageDto> songList = getAlbumImg(content);
            PageDto<SongPageDto> totalData = (PageDto<SongPageDto>) new PageDto<>(
                    songList,
                    page.getNumber(),
                    page.getContent().size(),
                    page.getTotalElements(),
                    page.getTotalPages()
            );
            return new ResponseEntity<PageDto<SongPageDto>>(totalData, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 이미지 파일 인코딩 및 DTO 저장 메소드
    public List<SongPageDto> getAlbumImg(List<SongPageDto> content) throws IOException {
        List<SongPageDto> songList = new ArrayList<>();
        for (SongPageDto songPageDto : content) {
            String encodedFile = encodeFiile.encoding(songPageDto.getAlbumImg(),"album");
            songPageDto.setAlbumImg(encodedFile);
            songList.add(songPageDto);
        }
        return songList;
    }

    //음악상세페이지
    public ResponseEntity<SongDetaillResultDto> getSongDetail(Long id){
        Long userId = getCurrentUsername();
        Optional<Song> songby = songRepository.findById(id);
        Song song = songby.get();
        Optional<SongLike> bySongLike = songLikeRepository.findBySongLikeUser(song, userId);
        boolean songLikeTarget = false; // 종아요 확인 여부 false 디폴드
        if(bySongLike.isPresent()){ // 사용자가 좋아요를 눌렀는지 확인 하고 값이 있으면 true 저장
            songLikeTarget = true;
        }
        List<SongDetailDto> songDetailDtos = songRepository.songDetail(id);
        List<SongGenreDto> songGenreDtos = songRepository.songDetailGenre(id);

        // 데이터가 없는 경우 예외 처리
        if (songDetailDtos.isEmpty() || songGenreDtos.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<Genre> genres = new ArrayList<>();
        for (SongGenreDto songGenreDto : songGenreDtos) {
            genres.add(songGenreDto.getGenre());
        }

        SongDetailDto result = songDetailDtos.get(0);
        try {
            String encodedFile = encodeFiile.encoding(result.getAlbumImg(),"album");
            result.setAlbumImg(encodedFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        SongDetaillResultDto totalData = new SongDetaillResultDto(
                result.getAlbumImg(), result.getTitle(),
                result.getNickname(), result.getLikeCount(),
                result.getLyrics(), result.getComposer(),
                result.getLyricist(), result.getAlbumId(),
                genres, songLikeTarget
        );

        return new ResponseEntity<>(totalData, HttpStatus.OK);
    }

    // 곡 파일 삭제 메소드
    public void songFileDelete(String songFileName) {
        File file = new File(fileDir + "/songFile/" + songFileName);
        file.delete();
    }

    // 곡 파일 로컬 저장 메소드
    public String saveSongFile(MultipartFile songFile) {
        String randomFileName = albumService.makeFileName();
        File storeSong = new File(fileDir + "/songFile/" + randomFileName + ".mp3");
        // 저장 경로가 없으면 저장경로 생성
        File storeDir = new File(fileDir + "/songFile/");
        if(!storeDir.exists()) storeDir.mkdirs();
        try {
            songFile.transferTo(storeSong);
        }catch(IOException e) {
            e.printStackTrace();
        }

        // 저장될 파일 이름으로 변경
        return randomFileName + ".mp3";
    }

    // 곡 수정 메소드
    @Transactional
    public void updateSong(Long songId, SongUpdateDto songUpdateDto, MultipartFile songFile) {
        Long userId = getCurrentUsername();
        Optional<Song> optional = songRepository.findByUser(songId, userId);
        optional.orElseThrow(() -> new AccessDeniedException("이 유저는 수정 권한이 없습니다"));

        Song song = optional.get();
        songFileDelete(song.getFileName());
        String songFileName = saveSongFile(songFile);
        songRepository.updateById(songUpdateDto.getSongName(), songUpdateDto.getLyricist(), songUpdateDto.getComposer(),
                songUpdateDto.getLyrics(), songFileName, songId);
        List<SongGenre> findBySong = songGenreRepository.findBySong(song);
        List<Genre> genres = new ArrayList<>();
        for (int i = 0; i < songUpdateDto.getGenres().size(); i++) {
            List<String> genresList = songUpdateDto.getGenres();
            String g = genresList.get(i);
            Genre genre = Genre.valueOf(g.toUpperCase());
            Long id = findBySong.get(i).getId();
            songGenreRepository.updateById(genre, id);
        }

    }

    // 곡 삭제 메소드
    @Transactional
    public void songDelete(Long songId) {
        Long userId = getCurrentUsername();
        Optional<Song> optional = songRepository.findByUser(songId, userId);
        optional.orElseThrow(() -> new AccessDeniedException("이 유저는 수정 권한이 없습니다"));

        Song song = optional.get();
        songFileDelete(song.getFileName());
        songRepository.delete(song);
    }

    // 현재 인증된 사용자의 이름을 반환하는 메소드
    private Long getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String Username =  ((UserDetails) principal).getUsername();
            Optional<User> finduser = userRepository.findOneWithAuthoritiesByUsername(Username);
            User user = finduser.get();
            return user.getId();
        } else {
            return -1L;
        }
    }
}
