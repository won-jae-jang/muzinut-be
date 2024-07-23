package nuts.muzinut.dto.board.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nuts.muzinut.dto.board.DetailBaseDto;
import nuts.muzinut.dto.board.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//특정 어드민 게시판을 반환할때 사용하는 dto
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetailEventBoardDto extends DetailBaseDto {

    private Long id; //게시판 pk
    private Long writerId; //작성자 pk
    private String title;
    private String writer; //저장된 게시판 작성자의 프로필 이미지 경로
    private int view;
    private String quillFilename;
    private String profileImg;
    private int likeCount;
    private LocalDateTime createdDt;

    private List<CommentDto> comments = new ArrayList<>();

    public DetailEventBoardDto(String title, String writer, int view) {
        this.title = title;
        this.writer = writer;
        this.view = view;
    }

    public DetailEventBoardDto(Long id, Long writerId,String title, String writer, int view, String quillFilename,
                               String profileImg, LocalDateTime createdDt) {
        this.id = id;
        this.writerId = writerId;
        this.title = title;
        this.writer = writer;
        this.view = view;
        this.quillFilename = quillFilename;
        this.profileImg = profileImg;
        this.createdDt = createdDt;
    }
}
