package nuts.muzinut.repository.chat.query;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.chat.Message;
import nuts.muzinut.domain.chat.QChat;
import nuts.muzinut.domain.chat.QChatMember;
import nuts.muzinut.domain.chat.QMessage;
import nuts.muzinut.domain.member.QUser;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.board.board.QBoardsForm;
import nuts.muzinut.dto.chat.ChatRoomListDto;
import nuts.muzinut.dto.chat.QChatRoomListDto;
import org.springframework.stereotype.Repository;

import java.util.List;

import static nuts.muzinut.domain.board.QBoard.board;
import static nuts.muzinut.domain.chat.QChat.*;
import static nuts.muzinut.domain.chat.QChatMember.*;
import static nuts.muzinut.domain.chat.QMessage.*;
import static nuts.muzinut.domain.member.QUser.*;

@RequiredArgsConstructor
@Repository
public class ChatQueryRepository {

    private final JPAQueryFactory queryFactory;

    //user는
    public List<ChatRoomListDto> getChatRoomList(User entryUser) {

        return queryFactory
                .select(new QChatRoomListDto(chat.id, message.isRead.eq(false).count(), user.profileImgFilename,
                        user.nickname, user.id))
                .from(chatMember)
                .join(chat).on(chatMember.chat.eq(chat).and(chatMember.user.eq(entryUser)))
                .join(message).on(message.chat.eq(chat).and(message.user.ne(entryUser)))
                .join(user).on(user.eq(message.user))
                .groupBy(chat.id)
                .orderBy(message.sendTime.max().desc())
                .fetch();
    }
}
