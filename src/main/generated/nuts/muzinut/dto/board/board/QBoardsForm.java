package nuts.muzinut.dto.board.board;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * nuts.muzinut.dto.board.board.QBoardsForm is a Querydsl Projection type for BoardsForm
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QBoardsForm extends ConstructorExpression<BoardsForm> {

    private static final long serialVersionUID = -1641869360L;

    public QBoardsForm(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> writer, com.querydsl.core.types.Expression<Integer> view, com.querydsl.core.types.Expression<Integer> like, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdDt) {
        super(BoardsForm.class, new Class<?>[]{long.class, String.class, String.class, int.class, int.class, java.time.LocalDateTime.class}, id, title, writer, view, like, createdDt);
    }

}

