package nuts.muzinut.repository.board.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.*;
import nuts.muzinut.domain.member.QUser;
import nuts.muzinut.dto.board.comment.CommentDto;
import nuts.muzinut.dto.board.comment.ReplyDto;
import org.springframework.stereotype.Repository;

import java.util.List;

import static nuts.muzinut.domain.board.QAdminBoard.*;
import static nuts.muzinut.domain.board.QBoard.*;
import static nuts.muzinut.domain.board.QComment.*;
import static nuts.muzinut.domain.board.QLike.*;
import static nuts.muzinut.domain.board.QReply.*;
import static nuts.muzinut.domain.member.QUser.*;

@Slf4j
@RequiredArgsConstructor
@Repository
public class AdminBoardQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<Tuple> getDetailAdminBoard(Long boardId) {

        return queryFactory
                .select(board,
                        Projections.fields(CommentDto.class, comment.id, comment.content,
                                comment.user.nickname.as("commentWriter"), comment.createdDt),
                        Projections.fields(ReplyDto.class, reply.id, reply.content, reply.comment.id.as("commentId"),
                                reply.user.nickname.as("replyWriter"), reply.createdDt),
                        JPAExpressions
                                .select(like.count())
                                .from(like)
                                .where(like.board.id.eq(boardId))
                )
                .from(comment)
                .join(comment.board, board)
                .leftJoin(comment.replies, reply)
                .leftJoin(reply.user, user)
                .where(board.id.eq(boardId))
                .fetch();
    }
}
