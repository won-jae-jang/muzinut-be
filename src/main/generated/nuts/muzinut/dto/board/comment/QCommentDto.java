package nuts.muzinut.dto.board.comment;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * nuts.muzinut.dto.board.comment.QCommentDto is a Querydsl Projection type for CommentDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QCommentDto extends ConstructorExpression<CommentDto> {

    private static final long serialVersionUID = -1376581818L;

    public QCommentDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<String> commentWriter, com.querydsl.core.types.Expression<Long> commentWriterId, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdDt, com.querydsl.core.types.Expression<String> commentProfileImg, com.querydsl.core.types.Expression<Integer> likeCount) {
        super(CommentDto.class, new Class<?>[]{long.class, String.class, String.class, long.class, java.time.LocalDateTime.class, String.class, int.class}, id, content, commentWriter, commentWriterId, createdDt, commentProfileImg, likeCount);
    }

}

