package nuts.muzinut.dto.chat;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * nuts.muzinut.dto.chat.QChatRoomListDto is a Querydsl Projection type for ChatRoomListDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QChatRoomListDto extends ConstructorExpression<ChatRoomListDto> {

    private static final long serialVersionUID = 1534423687L;

    public QChatRoomListDto(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<Long> notReadMessages, com.querydsl.core.types.Expression<String> profileImg, com.querydsl.core.types.Expression<String> nickname, com.querydsl.core.types.Expression<Long> userId) {
        super(ChatRoomListDto.class, new Class<?>[]{long.class, long.class, String.class, String.class, long.class}, id, notReadMessages, profileImg, nickname, userId);
    }

}

