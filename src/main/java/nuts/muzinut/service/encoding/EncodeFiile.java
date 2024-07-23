package nuts.muzinut.service.encoding;

import lombok.RequiredArgsConstructor;
import nuts.muzinut.exception.NoDataFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EncodeFiile {

    @Value("${spring.file.dir}")
    private String fileDir;

    //파일 Base64 인코딩 메소드
    public String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = Files.readAllBytes(file.toPath());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    public String encoding(String img, String comparison) throws IOException{
        File file;
        if (comparison.equalsIgnoreCase("album")){
            file = new File(fileDir + "/albumImg/" + img);
        } else {
            file = new File(fileDir + img);
        }
        // 파일이 없는 경우 예외 처리
        if (!file.exists() || !file.isFile()) {
            throw new NoDataFoundException("파일이 존재 하지 않습니다");
        }
            return encodeFileToBase64(file);
    }
}
