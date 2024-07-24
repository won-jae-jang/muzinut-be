package nuts.muzinut.controller.music;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import nuts.muzinut.service.music.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;


import java.io.IOException;
import java.io.InputStream;

@RestController
@RequestMapping("/streaming")
@RequiredArgsConstructor
public class StreamingController {
    @Autowired
    StreamingService streamingService;
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{songId}")
    public ResponseEntity<StreamingResponseBody> streamingMusic(
            @Validated @PathVariable("songId") @NotNull Long songId) throws IOException {
        try {
            Resource resource = streamingService.streamingSong(songId);

            if (resource.exists()) {
                StreamingResponseBody responseBody = outputStream -> {
                    try (InputStream inputStream = resource.getInputStream()) {
                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                };

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline;filename=\"" + resource.getFilename() + "\"");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(responseBody);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
