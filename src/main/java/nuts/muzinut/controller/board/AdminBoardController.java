package nuts.muzinut.controller.board;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.AdminBoard;
import nuts.muzinut.domain.board.AdminUploadFile;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.MessageDto;
import nuts.muzinut.dto.board.admin.AdminBoardsDto;
import nuts.muzinut.dto.board.admin.DetailAdminBoardDto;
import nuts.muzinut.dto.board.admin.AdminBoardForm;
import nuts.muzinut.exception.*;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.service.board.AdminBoardService;
import nuts.muzinut.service.board.FileStore;
import nuts.muzinut.service.member.UserService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;


@Slf4j
@Controller
@RequestMapping("/community")
@RequiredArgsConstructor
public class AdminBoardController {

    private final FileStore fileStore;
    private final AdminBoardService adminBoardService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @GetMapping("/admin-boards/new")
    public String newAdminBoard(@ModelAttribute AdminBoardForm form) {
        return "/board/admin-board-form";
    }

    /**
     *
     * @param form: 제목이 담긴 json
     * @param quillFile: 리액트 퀼 파일
     * @param attachedFiles: 첨부 파일
     * @return: 어드민 게시판 경로로 리다이렉트
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin-boards")
    public ResponseEntity<MessageDto> saveAdminBoard(
            @RequestPart @Valid AdminBoardForm form, @RequestPart @NotNull MultipartFile quillFile,
            @RequestPart @Nullable List<MultipartFile> attachedFiles) throws IOException {

        Optional<User> findUser = userService.getUserWithUsername(); //NotFoundMemberException 발생할 수 있음
        User user = findUser.get();

        AdminBoard adminBoard = new AdminBoard(form.getTitle());
        adminBoardFileStore(quillFile, attachedFiles, adminBoard);

        adminBoardService.createAdminBoard(adminBoard, user);

        HttpHeaders header = new HttpHeaders();
        header.setLocation(URI.create("/community/admin-boards/" + adminBoard.getId())); //생성한 게시판으로 리다이렉트

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .headers(header)
                .body(new MessageDto("어드민 게시판이 생성되었습니다"));
    }


    /**
     * 특정 어드민 게시판을 조회
     * @param id: admin board pk
     * @throws: db로 부터 검색되는 엔티티가 없을 경우 NotFoundEntityException 예외 발생 (404 Not found)
     */
    /*@ResponseBody
    @GetMapping(value = "/admin-boards/{id}", produces = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiValueMap<String, Object>> getAdminBoard(@PathVariable Long id) throws JsonProcessingException {

        //회원이 보는 상세페이지 인지, 비회원이 보는 상세페이지인지 구분
        User findUser = userService.getUserWithUsername().orElse(null);

        DetailAdminBoardDto detailAdminBoard = adminBoardService.getDetailAdminBoard(id, findUser);

        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<String, Object>();
        if (detailAdminBoard == null) {
            throw new BoardNotFoundException("해당 게시판이 존재하지 않습니다");
        }

        String jsonString = objectMapper.writeValueAsString(detailAdminBoard);

        // JSON 데이터를 Multipart-form 데이터에 추가
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonEntity = new HttpEntity<>(jsonString, jsonHeaders);
        formData.add("admin_data", jsonEntity);

        //해당 게시판의 quill 파일 추가
        String quillFilename = detailAdminBoard.getQuillFilename();
        String fullPath = fileStore.getFullPath(quillFilename);
        formData.add("quillFile", new FileSystemResource(fullPath));


        return new ResponseEntity<MultiValueMap<String, Object>>(formData, HttpStatus.OK);
    }*/

    @ResponseBody
    @GetMapping(value = "/admin-boards/{id}")
    public ResponseEntity<DetailAdminBoardDto> getAdminBoard(@PathVariable Long id) throws JsonProcessingException {

        //회원이 보는 상세페이지 인지, 비회원이 보는 상세페이지인지 구분
        User findUser = userService.getUserWithUsername().orElse(null);

        DetailAdminBoardDto detailAdminBoard = adminBoardService.getDetailAdminBoard(id, findUser);
        if (detailAdminBoard == null) {
            throw new BoardNotFoundException("해당 게시판이 존재하지 않습니다");
        }

        return new ResponseEntity<DetailAdminBoardDto>(detailAdminBoard, HttpStatus.OK);
    }

    //특정 어드민 게시판 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin-boards/{id}")
    public ResponseEntity<MessageDto> updateAdminBoard(@RequestPart AdminBoardForm form,
                                                       @RequestPart @NotNull MultipartFile quillFile,
                                                       @RequestPart @Nullable List<MultipartFile> attachedFiles,
                                                       @PathVariable Long id) throws IOException {

        AdminBoard adminBoard = adminBoardService.getAdminBoardWithUploadFiles(id);
        adminBoard.changeTitle(form.getTitle());
        fileStore.updateAdminAttachedFile(adminBoard); //기존에 저장해놓았던 첨부파일 및 퀼 파일 삭제
        adminBoardFileStore(quillFile, attachedFiles, adminBoard); //수정된 파일 저장

        HttpHeaders header = new HttpHeaders();
        header.setLocation(URI.create("/community/admin-boards/" + adminBoard.getId())); //수정한 게시판으로 리다이렉트

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .headers(header)
                .body(new MessageDto("어드민 게시판이 수정되었습니다"));

    }


    //어드민 게시판 삭제
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin-boards/{id}")
    public ResponseEntity<MessageDto> deleteAdminBoard(@PathVariable Long id) {

        AdminBoard adminBoard = adminBoardService.getAdminBoard(id);
        fileStore.deleteAdminAttachedFile(adminBoard);
        adminBoardService.deleteAdminBoard(id);
        HttpHeaders header = new HttpHeaders();
        header.setLocation(URI.create("/community/admin-boards")); //어드민 게시판으로 리다이렉트

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .headers(header)
                .body(new MessageDto("어드민 게시판이 삭제되었습니다"));
    }

    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    //공지 사항 게시판 첨부 파일 다운로드 (회원들만 가능)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/attach/{id}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long id) throws MalformedURLException {

        try {
            AdminUploadFile uploadFile = adminBoardService.getAttachFile(id);
            String storeFilename = uploadFile.getStoreFilename();
            String originFilename = uploadFile.getOriginFilename();

            UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFilename));
            log.info("originFilename={}", originFilename);

            String encodedOriginFilename = UriUtils.encode(originFilename, StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedOriginFilename + "\"";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);

        } catch (NotFoundFileException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }

    /**
     * 모든 어드민 게시판 조회
     * @param page: 페이지의 수
     * @throws: 아무런 게시판이 없는 경우에 BoardNotExistException 발생 (204 no content)
     */
    @ResponseBody
    @GetMapping("/admin-boards")
    public ResponseEntity<AdminBoardsDto> getAdminBoards(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "sort", defaultValue = "DATE") SortType sort) {

        try {
            AdminBoardsDto adminBoards = adminBoardService.getAdminBoards(page, sort);
            return ResponseEntity.ok()
                    .body(adminBoards);
        } catch (BoardNotExistException e) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(null);
        }
    }

    private void adminBoardFileStore(MultipartFile quillFile, List<MultipartFile> attachedFiles, AdminBoard adminBoard) throws IOException {
        if (attachedFiles == null) {
            fileStore.storeFile(quillFile, adminBoard); //퀼 파일 저장
        } else {
            fileStore.storeFiles(attachedFiles, adminBoard);//첨부 파일들을 저장
            fileStore.storeFile(quillFile, adminBoard); //퀼 파일 저장
            log.info("adminBoard's attachedFile: {}", adminBoard.getAdminUploadFiles().getFirst().getOriginFilename());
        }
    }
}
