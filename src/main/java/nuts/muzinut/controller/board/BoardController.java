package nuts.muzinut.controller.board;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.AdminUploadFile;
import nuts.muzinut.dto.board.board.BoardSearchForm;
import nuts.muzinut.dto.board.board.BoardsDto;
import nuts.muzinut.exception.NotFoundFileException;
import nuts.muzinut.service.board.BoardService;
import nuts.muzinut.service.board.FileStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequestMapping("/boards")
@RequiredArgsConstructor
@RestController
public class BoardController {

    private final BoardService boardService;
    private final FileStore fileStore;

    /**
     * 게시판 검색기능
     * @param form: 제목을 담는 dto
     * @param searchCond: 검색 조건 (이벤트, 자유, 어드민...)
     * @param page: 시작 페이지
     */
    @PostMapping
    public BoardsDto search(@RequestBody @Validated BoardSearchForm form,
                            @Validated @NotBlank @RequestParam String searchCond,
                            @RequestParam(value = "page", defaultValue = "0") int page) {
        return boardService.searchResult(form.getTitle(), searchCond, page);
    }

    //특정 퀼파일 가져오기
    @GetMapping("/get-file")
    public ResponseEntity<Resource> getQuillFile(
            @RequestParam
            String filename) throws MalformedURLException {

        try {
            String storeFilename = boardService.getQuillFilename(filename);

            log.info("파일 경로: {}", fileStore.getFullPath(storeFilename));
            UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFilename));

            String encodedOriginFilename = UriUtils.encode("퀼파일", StandardCharsets.UTF_8);
            String contentDisposition = "attachment; filename=\"" + encodedOriginFilename + "\"";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                    .body(resource);

        } catch (NotFoundFileException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
}
