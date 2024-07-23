package nuts.muzinut.controller.music;


import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.dto.music.AlbumDetaillResultDto;
import nuts.muzinut.dto.music.AlbumDto;
import nuts.muzinut.dto.music.AlbumUpdateDto;
import nuts.muzinut.exception.AlbumCreateFailException;
import nuts.muzinut.service.music.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/album")
public class AlbumController {
    @Autowired
    AlbumService albumService;

    // 앨범 업로드
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Long>> uploadAlbum(
            @RequestPart("albumImg") MultipartFile albumImg,
            @RequestPart("songFiles") List<MultipartFile> songFiles,
            @RequestPart("albumData") AlbumDto albumData
    ) throws IOException {
        // 곡 파일의 갯수와 곡 정보의 갯수가 다를때 Exception
        if (songFiles.size() != albumData.getSongs().size()) throw new AlbumCreateFailException("등록하려는 음원 파일의 갯수와 음원 파일 정보의 갯수가 다릅니다.");
        // 각 곡 파일의 이름이 서로 같은지 확인 후 Exception
        albumService.isSongFileSameName(songFiles);

        // 앨범 이미지 저장
        String storeAlbumImg = albumService.saveAlbumImg(albumImg);
        // 각 곡들 저장
        AlbumDto storeAlbumData = albumService.saveSongs(songFiles, albumData);
        // 엔티티 저장
        Long albumId = albumService.saveAlbumData(storeAlbumData, storeAlbumImg);
        if(albumId == null) throw new AlbumCreateFailException("앨범 등록에 실패하였습니다. (Entity Create Error)");
        Map<String, Long> body = new HashMap<>();
        body.put("albumId", albumId);

        log.info("upload information : {}", body);

        return ResponseEntity.status(HttpStatus.OK)
                .body(body);
    }

    /**
     * 앨범 수정을 가져오는 엔드포인트
     *
     * @param albumId 앨범의 ID
     * @param albumImg 앨범 이미지 파일
     * @param albumData 앨범의 정보
     * @return 앨범 수정 완료 메시지와 HTTP 상태 코드
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<String> updateAlbum(
            @PathVariable("id") Long albumId,
            @RequestPart("albumImg") MultipartFile albumImg,
            @RequestPart("albumData") AlbumUpdateDto albumData
    ) {
        albumService.updateAlbum(albumId, albumImg, albumData);

        return new ResponseEntity<String>("앨범 수정 완료 되었습니다",HttpStatus.OK);
    }

    /**
     * 앨범 상세 정보를 가져오는 엔드포인트
     *
     * @param albumId 앨범의 ID
     * @return 앨범 상세 정보와 HTTP 상태 코드
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AlbumDetaillResultDto> getSongDetail
    (@Validated @PathVariable("id") @NotNull Long albumId) {
        return albumService.getAlbumDetail(albumId);
    }

    /**
     * 앨범 삭제를 가져오는 엔드포인트
     *
     * @param albumId 앨범의 ID
     * @return 앨범 삭제 완료 메시지와 HTTP 상태 코드
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<String> deleteAlbum(@Validated @PathVariable("id") @NotNull Long albumId){
        albumService.albumDelete(albumId);
        return new ResponseEntity<String>("앨범 삭제가 완료 되었습니다", HttpStatus.OK);
    }

}