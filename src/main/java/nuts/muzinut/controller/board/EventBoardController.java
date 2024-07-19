package nuts.muzinut.controller.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.EventBoard;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.MessageDto;
import nuts.muzinut.dto.board.event.DetailEventBoardDto;
import nuts.muzinut.dto.board.event.EventBoardForm;
import nuts.muzinut.dto.board.event.EventBoardsDto;
import nuts.muzinut.dto.board.lounge.DetailLoungeDto;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.exception.NoUploadFileException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.service.board.EventBoardService;
import nuts.muzinut.service.board.FileStore;
import nuts.muzinut.service.member.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static nuts.muzinut.controller.board.FileType.STORE_FILENAME;

@Slf4j
@Controller
@RequestMapping("/community/event-boards")
@RequiredArgsConstructor
public class EventBoardController {

    private final UserService userService;
    private final FileStore fileStore;
    private final EventBoardService eventBoardService;
    private final ObjectMapper objectMapper;

    /**
     * 이벤트 게시판 생성
     * @param quillFile: 리액트 퀼 파일
     * @param img: 섬네일 이미지
     * @param form: 제목
     * @throws NoUploadFileException: 업로드 할 파일이 없는 경우
     * @throws IOException
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping
    public ResponseEntity<MessageDto> createBoard(
            @RequestPart MultipartFile quillFile, @RequestPart MultipartFile img,
            @Validated @RequestPart EventBoardForm form) throws IOException {
        User user = userService.getUserWithUsername()
                .orElseThrow(() -> new NotFoundMemberException("회원이 아닙니다."));

        EventBoard eventBoard = new EventBoard(form.getTitle());
        eventBoard.addBoard(user);

        Map<FileType, String> filename = fileStore.storeFile(quillFile); //이벤트 게시판 퀼 파일 저장
        eventBoard.setFilename(filename.get(STORE_FILENAME)); //저장 파일 이름 설정
        Map<FileType, String> imgFilename = fileStore.storeFile(img); //이벤트 게시판 썸네일 이미지 파일 저장
        eventBoard.setImg(imgFilename.get(STORE_FILENAME)); //저장 파일 이름 설정

        eventBoardService.save(eventBoard); //이벤트 게시판 저장

        HttpHeaders header = new HttpHeaders();
        header.setLocation(URI.create("/community/event-boards/" + eventBoard.getId())); //생성한 게시판으로 리다이렉트

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .headers(header)
                .body(new MessageDto("이벤트 게시판이 생성되었습니다"));
    }

    //특정 게시판 조회
    @GetMapping(value = "/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> getDetailFreeBoard(@PathVariable Long id) throws JsonProcessingException {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();

        User findUser = userService.getUserWithUsername().orElse(null);
        DetailEventBoardDto detailEventBoard = eventBoardService.getDetailEventBoard(id, findUser);

        if (detailEventBoard == null) {
            throw new BoardNotFoundException("해당 게시판이 존재하지 않습니다");
        }

        String jsonString = objectMapper.writeValueAsString(detailEventBoard);

        // JSON 데이터를 Multipart-form 데이터에 추가
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonEntity = new HttpEntity<>(jsonString, jsonHeaders);
        formData.add("json_data", jsonEntity);

        //해당 게시판의 quill 파일 추가
        String quillFilename = detailEventBoard.getQuillFilename();
        String fullPath = fileStore.getFullPath(quillFilename);
        formData.add("quillFile", new FileSystemResource(fullPath));

        return new ResponseEntity<MultiValueMap<String, Object>>(formData, HttpStatus.OK);
    }
//특정 라운지 조회 및 댓글과 대댓글 불러오기
@GetMapping(value = "/{id}")
public ResponseEntity<DetailEventBoardDto> getDetailLounge(@PathVariable Long id) throws JsonProcessingException {

    User findUser = userService.getUserWithUsername().orElse(null);
    DetailEventBoardDto detailEventBoard = eventBoardService.getDetailEventBoard(id, findUser);

    if (detailEventBoard == null) {
        throw new BoardNotFoundException("해당 라운지가 존재하지 않습니다");
    }

    return new ResponseEntity<DetailEventBoardDto>(detailEventBoard, HttpStatus.OK);
}


    //모든 이벤트 게시판 조회
    @GetMapping()
    public ResponseEntity<EventBoardsDto> getFreeBoards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sort", defaultValue = "DATE") SortType sort) {
        try {
            EventBoardsDto eventBoards = eventBoardService.getEventBoards(page, sort);
            return ResponseEntity.ok()
                    .body(eventBoards);
        } catch (BoardNotExistException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(null);
        }
    }
    
    //이벤트 게시판 수정
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MessageDto> updateFreeBoard(
            @RequestPart MultipartFile quillFile, @RequestPart MultipartFile img,
            @Validated @RequestPart EventBoardForm form, @PathVariable Long id) throws IOException {
        User user = userService.getUserWithUsername()
                .orElseThrow(() -> new NotFoundMemberException("닉네임을 설정해주세요"));
        boolean isAuthorized = eventBoardService.checkAuth(id, user);

        if (isAuthorized) {
            log.info("수정할 게시판: {}", id);
            EventBoard eventBoard = eventBoardService.getEventBoard(id); //이벤트 게시판 가져오기
            //자유 게시판 파일 저장 및 기존 퀼 파일 삭제
            String changeQuillFilename = fileStore.updateFile(quillFile, eventBoard.getFilename());
            String changeImgFilename = fileStore.updateFile(img, eventBoard.getImg());
            eventBoardService.updateEventBoard(changeQuillFilename, form.getTitle(), changeImgFilename, id); //디비 정보 수정
            HttpHeaders header = new HttpHeaders();
            header.setLocation(URI.create("/community/event-boards/" + eventBoard.getId())); //수정한 게시판으로 리다이렉트

            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .headers(header)
                    .body(new MessageDto("이벤트 게시판이 수정되었습니다"));

        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(null);
    }


    //이벤트 게시판 삭제
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> deleteFreeBoard(@PathVariable Long id) throws IOException {
        User user = userService.getUserWithUsername()
                .orElseThrow(() -> new NotFoundMemberException("닉네임을 설정해주세요"));

        boolean isAuthorized = eventBoardService.checkAuth(id, user);
        if (isAuthorized) {
            EventBoard eventBoard = eventBoardService.getEventBoard(id);
            fileStore.deleteFile(eventBoard.getFilename()); //이벤트 게시판의 퀼 파일 삭제
            fileStore.deleteFile(eventBoard.getImg()); //이벤트 게시판의 이미지 삭제
            eventBoardService.deleteEventBoard(id); //이벤트 게시판 삭제

            HttpHeaders header = new HttpHeaders();
            header.setLocation(URI.create("/community/event-boards")); //이벤트 게시판 홈페이지로 리다이렉트

            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .headers(header)
                    .body(new MessageDto("이벤트 게시판이 삭제되었습니다"));

        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(null);
    }
}
