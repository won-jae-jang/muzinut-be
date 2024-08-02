package nuts.muzinut.dto.board.comment;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * nuts.muzinut.dto.board.comment.QReplyDto is a Querydsl Projection type for ReplyDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QReplyDto extends ConstructorExpression<ReplyDto> {

    private static final long serialVersionUID = 229436507L;

    public QReplyDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> content, com.querydsl.core.types.Expression<String> replyWriter, com.querydsl.core.types.Expression<Long> replyWwriterId, com.querydsl.core.types.Expression<Long> commentId, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdDt, com.querydsl.core.types.Expression<String> replyProfileImg) {
        super(ReplyDto.class, new Class<?>[]{long.class, String.class, String.class, long.class, long.class, java.time.LocalDateTime.class, String.class}, id, content, replyWriter, replyWwriterId, commentId, createdDt, replyProfileImg);
    }

}

