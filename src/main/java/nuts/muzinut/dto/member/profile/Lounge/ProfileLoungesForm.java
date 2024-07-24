package nuts.muzinut.dto.member.profile.Lounge;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import nuts.muzinut.dto.board.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ProfileLoungesForm {
    private Long id;
    private String writer;
    private String filename;
    private LocalDateTime createdDt;
    private int like;

    private int commentSize;
    private List<CommentDto> comments = new ArrayList<>();

    private Boolean boardLikeStatus = false;
    private Boolean isBookmark = false;

    @QueryProjection
    public ProfileLoungesForm(Boolean boardLikeStatus, Boolean isBookmark) {
        this.boardLikeStatus = boardLikeStatus;
        this.isBookmark = isBookmark;
    }
    // comments 리스트를 설정 & 댓글 갯수 설정하는 메서드
    public void setComments(List<CommentDto> comments) {
        this.comments = comments;
        this.commentSize = comments != null ? comments.size() : 0;
    }

    public ProfileLoungesForm(Long id, String writer, String filename, LocalDateTime createdDt, int like) {
        this.id = id;
        this.writer = writer;
        this.filename = filename;
        this.createdDt = createdDt;
        this.like = like;
    }
}
