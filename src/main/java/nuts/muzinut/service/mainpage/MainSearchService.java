package nuts.muzinut.service.mainpage;

import lombok.RequiredArgsConstructor;
import nuts.muzinut.dto.mainpage.*;
import nuts.muzinut.dto.page.PageDto;
import nuts.muzinut.exception.NoDataFoundException;
import nuts.muzinut.repository.mainpage.MainSearchRepository;
import nuts.muzinut.service.encoding.EncodeFiile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainSearchService {

    private final MainSearchRepository mainSearchRepository;
    private final EncodeFiile encodeFiile;

    @Value("${spring.file.dir}")
    private String fileDir;

    // 메인 디폴트 검색(아티스트, 곡)
    public ResponseEntity<SearchTotalDto> getTotalSearch(String searchWord, int artistPageable, int songPageable) {
        // 페이지 넘버가 0이하 인 경우 0으로 저장, 1이상인 경우 1을 뺌 값을 저장
        if (artistPageable < 1) {
            artistPageable = 0;
        }else {
            artistPageable = artistPageable - 1;
        }
        if (songPageable < 1) {
            songPageable = 0;
        }else {
            songPageable = songPageable - 1;
        }
        PageRequest artistPageRequest = PageRequest.of(artistPageable,10);
        PageRequest songPageRequest = PageRequest.of(songPageable,20);
        Page<SearchArtistDto> artistPage = mainSearchRepository.artistSearch(searchWord, artistPageRequest);
        Page<SearchSongDto> songPage = mainSearchRepository.songSearch(searchWord, songPageRequest);
        // 검색 결과가 하나라도 있으면 데이터 반환
        if(artistPage.hasContent() || songPage.hasContent()) {
            List<SearchArtistDto> artistContent = artistPage.getContent();
            List<SearchSongDto> songContent = songPage.getContent();
            try {
                List<SearchArtistDto> artistDtoList = getProfileImg(artistContent);
                List<SearchSongDto> songDtoList = getAlbumImg(songContent);
                PageDto<SearchArtistDto> artistData = getArtistData(artistPage, artistDtoList);
                PageDto<SearchSongDto> songData = getSongData(songPage, songDtoList);
                SearchTotalDto totalData = new SearchTotalDto(artistData, songData);
                return new ResponseEntity<SearchTotalDto>(totalData, HttpStatus.OK);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else { // 검색 결과가 하나도 없으면 예외 처리
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    // 메인 특정 검색(아티스트)
    public ResponseEntity<?> getArtistSearch(String searchWord, int artistPageable) {
        // 페이지 넘버가 0이하 인 경우 0으로 저장, 1이상인 경우 1을 뺌 값을 저장
        if (artistPageable < 1) {
            artistPageable = 0;
        }else {
            artistPageable = artistPageable - 1;
        }
        PageRequest artistPageRequest = PageRequest.of(artistPageable,10);
        Page<SearchArtistDto> artistPage = mainSearchRepository.artistSearch(searchWord, artistPageRequest);
        if (!artistPage.hasContent()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<SearchArtistDto> artistContent = artistPage.getContent();
        try {
            List<SearchArtistDto> artistDtoList = getProfileImg(artistContent);
            PageDto<SearchArtistDto> artistData = getArtistData(artistPage, artistDtoList);
            TapSearchDto<SearchArtistDto> data = new TapSearchDto<>(artistData);
            return new ResponseEntity<TapSearchDto<SearchArtistDto>>(data, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 메인 특정 검색(곡)
    public ResponseEntity<?> getSongSearch(String searchWord, int songPageable) {
        // 페이지 넘버가 0이하 인 경우 0으로 저장, 1이상인 경우 1을 뺌 값을 저장
        if (songPageable < 1) {
            songPageable = 0;
        }else {
            songPageable = songPageable - 1;
        }
        PageRequest songPageRequest = PageRequest.of(songPageable, 20);
        Page<SearchSongDto> songPage = mainSearchRepository.songSearch(searchWord, songPageRequest);
        if (!songPage.hasContent()){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        List<SearchSongDto> songContent = songPage.getContent();
        try {
            List<SearchSongDto> songDtoList = getAlbumImg(songContent);
            PageDto<SearchSongDto> songData = getSongData(songPage, songDtoList);
            TapSearchDto<SearchSongDto> data = new TapSearchDto<>(songData);
            return new ResponseEntity<TapSearchDto<SearchSongDto>>(data, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // ProfileImg 인코딩 및 DTO 저장 메소드
    public List<SearchArtistDto> getProfileImg(List<SearchArtistDto> artistContent) throws IOException {
        List<SearchArtistDto> searchArtistDtos = new ArrayList<>();

        for (SearchArtistDto searchArtistDto : artistContent) {
            File file = new File(fileDir + searchArtistDto.getProfileImg());
            // 파일이 없는 경우 예외 처리
            if (!file.exists() || !file.isFile()) {
                file = new File(fileDir + "/base/profile.png");
            }
            String encodedFile = encodeFiile.encodeFileToBase64(file);
            searchArtistDto.setProfileImg(encodedFile);
            searchArtistDtos.add(searchArtistDto);
        }
        return searchArtistDtos;
    }

    // AlbumImg 인코딩 및 DTO 저장 메소드
    public List<SearchSongDto> getAlbumImg(List<SearchSongDto> songContent) throws IOException {
        List<SearchSongDto> searchSongDtos = new ArrayList<>();

        for (SearchSongDto searchSongDto : songContent) {
            File file = new File(fileDir + "/albumImg/" + searchSongDto.getAlbumImg());
            // 파일이 없는 경우 예외 처리
            if (!file.exists() || !file.isFile()) {
                throw new NoDataFoundException("파일이 존재 하지 않습니다");
            }
            String encodedFile = encodeFiile.encodeFileToBase64(file);
            searchSongDto.setAlbumImg(encodedFile);
            searchSongDtos.add(searchSongDto);
        }
        return searchSongDtos;
    }

    // artist 페이징 처리 메소드
    public PageDto<SearchArtistDto> getArtistData(Page<SearchArtistDto> artistPage, List<SearchArtistDto> artistDtoList) {
        return (PageDto<SearchArtistDto>) new PageDto<>(
                artistDtoList,
                artistPage.getNumber(),
                artistPage.getContent().size(),
                artistPage.getTotalElements(),
                artistPage.getTotalPages()
        );

    }

    // song 페이징 처리 메소드
    public PageDto<SearchSongDto> getSongData(Page<SearchSongDto> songPage, List<SearchSongDto> songDtoList) {
        return (PageDto<SearchSongDto>)  new PageDto<>(
                songDtoList,
                songPage.getNumber(),
                songPage.getContent().size(),
                songPage.getTotalElements(),
                songPage.getTotalPages()
        );
    }

}
