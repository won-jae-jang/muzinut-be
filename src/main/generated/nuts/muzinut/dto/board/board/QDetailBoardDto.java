package nuts.muzinut.dto.board.board;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * nuts.muzinut.dto.board.board.QDetailBoardDto is a Querydsl Projection type for DetailBoardDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QDetailBoardDto extends ConstructorExpression<DetailBoardDto> {

    private static final long serialVersionUID = -795774935L;

    public QDetailBoardDto(com.querydsl.core.types.Expression<Integer> likeCount, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> writer, com.querydsl.core.types.Expression<String> quillFilename, com.querydsl.core.types.Expression<String> profileImg, com.querydsl.core.types.Expression<Integer> view, com.querydsl.core.types.Expression<Boolean> boardLikeStatus, com.querydsl.core.types.Expression<Boolean> isBookmark) {
        super(DetailBoardDto.class, new Class<?>[]{int.class, String.class, String.class, String.class, String.class, int.class, boolean.class, boolean.class}, likeCount, title, writer, quillFilename, profileImg, view, boardLikeStatus, isBookmark);
    }

}

