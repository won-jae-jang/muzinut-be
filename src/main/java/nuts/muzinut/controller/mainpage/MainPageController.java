package nuts.muzinut.controller.mainpage;

import lombok.RequiredArgsConstructor;
import nuts.muzinut.dto.mainpage.*;
import nuts.muzinut.service.mainpage.MainPageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/muzinut")
public class MainPageController {

    private final MainPageService mainPageService;

    /**
     * 메인 페이지 데이터를 조회하는 엔드포인트입니다.
     *
     * @return 메인 페이지 데이터와 HTTP 상태 코드
     */
//    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<MainTotalDto> findMainData(){
//        return mainPageService.findMainTotalData();
//    }

    @GetMapping(value = "/hotsong",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<HotSongDto>>> getHotSongs(){
        return mainPageService.findTOP10Songs();
    }
    @GetMapping(value = "/newsong",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<NewSongDto>>> getNewSongs(){
        return mainPageService.findNewSongs();
    }
    @GetMapping(value = "/hotartist",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<HotArtistDto>>> getHotArtist(){
        return mainPageService.findTOP5Artists();
    }
    @GetMapping(value = "/hotboard",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, List<HotBoardDto>>> getHotBoards(){
        return mainPageService.findHotBoards();
    }
    @GetMapping(value = "/newboard",produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, NewBoardDto>> getNewBoards(){
        return mainPageService.findNewBoard();
    }
}
