package nuts.muzinut.controller.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.FreeBoard;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.MessageDto;
import nuts.muzinut.dto.board.free.DetailFreeBoardDto;
import nuts.muzinut.dto.board.free.FreeBoardForm;
import nuts.muzinut.dto.board.free.FreeBoardsDto;
import nuts.muzinut.exception.*;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.exception.token.ExpiredTokenException;
import nuts.muzinut.service.board.FileStore;
import nuts.muzinut.service.board.FreeBoardService;
import nuts.muzinut.service.member.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import static nuts.muzinut.controller.board.FileType.*;

@Slf4j
@Controller
@RequestMapping("/community/free-boards")
@RequiredArgsConstructor
public class FreeBoardController {

    private final UserService userService;
    private final FileStore fileStore;
    private final FreeBoardService freeBoardService;
    private final ObjectMapper objectMapper;

    /**
     * 자유 게시판 생성
     * @param freeBoardForm: react quill file & title
     * @throws NoUploadFileException: 업로드 할 파일이 없는 경우
     * @throws IOException
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping
    public ResponseEntity<MessageDto> createBoard(
            @RequestPart("quillFile") MultipartFile quillFile, @RequestPart("freeBoardForm") FreeBoardForm freeBoardForm) throws IOException {
        User user = userService.getUserWithUsername()
                .orElseThrow(() -> new NotFoundMemberException("회원이 아닙니다."));
        FreeBoard freeBoard = new FreeBoard(freeBoardForm.getTitle());
        freeBoard.addBoard(user);

        Map<FileType, String> filename = fileStore.storeFile(quillFile);//자유 게시판 파일 저장
        freeBoard.setFilename(filename.get(STORE_FILENAME)); //저장 파일 이름 설정
        freeBoardService.save(freeBoard); //자유 게시판 저장

        HttpHeaders header = new HttpHeaders();
        // header.setLocation(URI.create("/community/free-boards/" + freeBoard.getId())); //수정한 게시판으로 리다이렉트

        return ResponseEntity.status(HttpStatus.OK)
                .headers(header)
                .body(new MessageDto("자유 게시판이 생성되었습니다"));
    }

    //특정 게시판 조회
    @GetMapping(value = "/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> getDetailFreeBoard(@PathVariable(name = "id") Long id) throws JsonProcessingException {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        User findUser;

        //회원이 보는 상세페이지 인지, 비회원이 보는 상세페이지인지 구분
        try {
            findUser = userService.getUserWithUsername().orElse(null);
        } catch (ExpiredTokenException e) {
            log.info("잡힘");
            findUser = null;
        }
        DetailFreeBoardDto detailFreeBoardDto = freeBoardService.detailFreeBoard(id, findUser);

        if (detailFreeBoardDto == null) {
            throw new BoardNotFoundException("해당 게시판이 존재하지 않습니다");
        }

        String jsonString = objectMapper.writeValueAsString(detailFreeBoardDto);

        // JSON 데이터를 Multipart-form 데이터에 추가
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonEntity = new HttpEntity<>(jsonString, jsonHeaders);
        formData.add("json_data", jsonEntity);

        //해당 게시판의 quill 파일 추가
        String quillFilename = detailFreeBoardDto.getQuillFilename();
        String fullPath = fileStore.getFullPath(quillFilename);
        formData.add("quillFile", new FileSystemResource(fullPath));

        return new ResponseEntity<MultiValueMap<String, Object>>(formData, HttpStatus.OK);
    }

    //모든 자유 게시판 조회
    @GetMapping()
    public ResponseEntity<FreeBoardsDto> getFreeBoards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sort", defaultValue = "DATE") SortType sort) {
        try {
            FreeBoardsDto freeBoards = freeBoardService.getFreeBoards(page, sort);
            return ResponseEntity.ok()
                    .body(freeBoards);
        } catch (BoardNotExistException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(null);
        }
    }
    
    //자유 게시판 수정
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<MessageDto> updateFreeBoard(
            @RequestPart MultipartFile quillFile, @RequestPart FreeBoardForm freeBoardForm, @PathVariable Long id) throws IOException {
        User user = userService.getUserWithUsername()
                .orElseThrow(() -> new NotFoundMemberException("닉네임을 설정해주세요"));
        boolean isAuthorized = freeBoardService.checkAuth(id, user);
        if (isAuthorized) {
            FreeBoard freeBoard = freeBoardService.getFreeBoard(id);
            //자유 게시판 파일 저장 및 기존 퀼 파일 삭제
            String changeFilename = fileStore.updateFile(quillFile, freeBoard.getFilename());
            freeBoardService.updateFreeBoard(freeBoard.getId(), freeBoardForm.getTitle(), changeFilename); //자유 게시판 저장
            HttpHeaders header = new HttpHeaders();
            header.setLocation(URI.create("/community/free-boards/" + freeBoard.getId())); //수정한 게시판으로 리다이렉트

            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .headers(header)
                    .body(new MessageDto("자유 게시판이 수정되었습니다"));

        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(null);
    }


    //자유 게시판 삭제
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageDto> deleteFreeBoard(@PathVariable Long id) throws IOException {
        User user = userService.getUserWithUsername()
                .orElseThrow(() -> new NotFoundMemberException("닉네임을 설정해주세요"));
        boolean isAuthorized = freeBoardService.checkAuth(id, user);
        if (isAuthorized) {
            FreeBoard freeBoard = freeBoardService.getFreeBoard(id); //게시판 조회
            fileStore.deleteFile(freeBoard.getFilename()); //자유 게시판의 파일 삭제
            freeBoardService.deleteFreeBoard(id); //자유 게시판 삭제

            HttpHeaders header = new HttpHeaders();
            header.setLocation(URI.create("/community/free-boards")); //자유 게시판 홈페이지로 리다이렉트

            return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                    .headers(header)
                    .body(new MessageDto("자유 게시판이 삭제되었습니다"));

        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(null);
    }


    //for test
    @GetMapping(value = "/multipartdata", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> gerMultipartData() {
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        formData.add("first_name",  "ganesh");
        formData.add("last_name", "patil");
        formData.add("file-data_1", new FileSystemResource("C:\\Users\\dnjswo\\study\\project\\muzinut\\file\\sample1.png"));
        return new ResponseEntity<MultiValueMap<String, Object>>(formData, HttpStatus.OK);
    }

}
