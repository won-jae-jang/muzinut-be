package nuts.muzinut.service.mainpage;


import lombok.RequiredArgsConstructor;

import nuts.muzinut.dto.mainpage.*;
import nuts.muzinut.exception.NoDataFoundException;
import nuts.muzinut.repository.mainpage.MainPageRepository;
import nuts.muzinut.service.encoding.EncodeFiile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RequiredArgsConstructor
@Service
public class MainPageService {

    private final MainPageRepository mainPageRepository;
    private final EncodeFiile encodeFiile;

    @Value("${spring.file.dir}")
    private String fileDir;

    // 인기 TOP10 음악
    public ResponseEntity<Map<String ,List<HotSongDto>>> findTOP10Songs(){
        List<HotSongDto> top10Songs = mainPageRepository.findTOP10Song();
        if (top10Songs.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        try {
            List<HotSongDto> hotSongList = top10Img(top10Songs);
            Map<String ,List<HotSongDto>> body = new HashMap<>();
            body.put("top10Songs", hotSongList);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    // 최신 음악
    public ResponseEntity<Map<String ,List<NewSongDto>>> findNewSongs(){
        List<NewSongDto> newSongs = mainPageRepository.findNewSong();
        if (newSongs.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        try {
            List<NewSongDto> newSongList = newSongImg(newSongs);
            Map<String ,List<NewSongDto>> body = new HashMap<>();
            body.put("newSongs", newSongs);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 인기 TOP5 아티스트
    public ResponseEntity<Map<String ,List<HotArtistDto>>> findTOP5Artists(){
        List<HotArtistDto> top5Artists = mainPageRepository.findTOP5Artist();
        if (top5Artists.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        try {
            List<HotArtistDto> hotArtistList = top5ArtistImg(top5Artists);
            Map<String ,List<HotArtistDto>> body = new HashMap<>();
            body.put("top5Artists", hotArtistList);
            return new ResponseEntity<>(body, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 인기 게시판
    public ResponseEntity<Map<String ,List<HotBoardDto>>> findHotBoards(){
        List<HotBoardDto> hotBoards = findMainHotBoard();
        if (hotBoards.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        Map<String ,List<HotBoardDto>> body = new HashMap<>();
        body.put("hotBoards", hotBoards);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }

    // 최신 게시판
    public ResponseEntity<Map<String ,NewBoardDto>> findNewBoard(){
        NewBoardDto newBoard = findMainNewBoard();
        if (newBoard.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        Map<String ,NewBoardDto> body = new HashMap<>();
        body.put("newBoard", newBoard);
        return new ResponseEntity<>(body, HttpStatus.OK);
    }


    // top10Img 인코딩 및 DTO 저장 메소드
    public List<HotSongDto> top10Img(List<HotSongDto> top10Songs) throws IOException {
        List<HotSongDto> top10List = new ArrayList<>();
        for (HotSongDto top10Song : top10Songs) {
            File file = new File(fileDir + "/albumImg/" + top10Song.getAlbumImg());
            // 파일이 없는 경우 예외 처리
            if (!file.exists() || !file.isFile()) {
                throw new NoDataFoundException("파일이 존재 하지 않습니다");
            }
            String encodedFile = encodeFiile.encodeFileToBase64(file);
            top10Song.setAlbumImg(encodedFile);
            top10List.add(top10Song);
        }
        return top10List;
    }
    // newSongImg 인코딩 및 DTO 저장 메소드
    public List<NewSongDto> newSongImg(List<NewSongDto> newSongs) throws IOException {
        List<NewSongDto> newSongList = new ArrayList<>();
        for (NewSongDto newSong : newSongs) {
            File file = new File(fileDir + "/albumImg/" + newSong.getAlbumImg());
            // 파일이 없는 경우 예외 처리
            if (!file.exists() || !file.isFile()) {
                throw new NoDataFoundException("파일이 존재 하지 않습니다");
            }
            String encodedFile = encodeFiile.encodeFileToBase64(file);
            newSong.setAlbumImg(encodedFile);
            newSongList.add(newSong);
        }
        return newSongList;
    }
    // top5ArtistImg 인코딩 및 DTO 저장 메소드
    public List<HotArtistDto> top5ArtistImg(List<HotArtistDto> top5Artists) throws IOException {
        List<HotArtistDto> top5ArtistList = new ArrayList<>();
        for (HotArtistDto top5Artist : top5Artists) {
            File file = new File(fileDir + top5Artist.getProfileImg());
            // 파일이 없는 경우 예외 처리
            if (!file.exists() || !file.isFile()) {
                throw new NoDataFoundException("파일이 존재 하지 않습니다");
            }
            String encodedFile = encodeFiile.encodeFileToBase64(file);
            top5Artist.setProfileImg(encodedFile);
            top5ArtistList.add(top5Artist);
        }

        return top5ArtistList;
    }

    // 인기 게시판 검색 및 DTO 변환 메소드
    public List<HotBoardDto> findMainHotBoard(){
        List<Object[]> resultList = mainPageRepository.findHotBoard();

        List<HotBoardDto> hotBoardDtos = new ArrayList<>();
        for (Object[] objects : resultList) {
            Long boardId = (Long) objects[0];
            String title = (String) objects[1];
            String nickname = (String) objects[2];
            int view = (int) objects[3];
            String dtype = (String) objects[4];
            HotBoardDto hotBoardDto = new HotBoardDto(boardId, title, nickname, view, dtype);
            hotBoardDtos.add(hotBoardDto);
        }
        return hotBoardDtos;
    }

    // 최신 게시판 검색 및 DTO 변환 메소드
    public NewBoardDto findMainNewBoard() {
        List<Object[]> results = mainPageRepository.findNewBoard();

        List<NewFreeBoardDto> freeBoardDtos = new ArrayList<>();
        List<NewRecruitBoardDto> recruitBoardDtos = new ArrayList<>();

        for (Object[] objects : results) {
            Long boardId = (Long) objects[0];
            String title = (String) objects[1];
            String nickname = (String) objects[2];
            String dtype = (String) objects[3];
            if (dtype.equalsIgnoreCase("FreeBoard")){
                NewFreeBoardDto newFreeBoardDto = new NewFreeBoardDto(boardId, title, nickname);
                freeBoardDtos.add(newFreeBoardDto);
            } else if (dtype.equalsIgnoreCase("RecruitBoard")) {
                NewRecruitBoardDto newRecruitBoardDto = new NewRecruitBoardDto(boardId, title, nickname);
                recruitBoardDtos.add(newRecruitBoardDto);
            }
        }
        return new NewBoardDto(freeBoardDtos,recruitBoardDtos);
    }

}
