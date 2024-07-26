package nuts.muzinut.dto.member.profile.Lounge;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * nuts.muzinut.dto.member.profile.Lounge.QProfileLoungesForm is a Querydsl Projection type for ProfileLoungesForm
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QProfileLoungesForm extends ConstructorExpression<ProfileLoungesForm> {

    private static final long serialVersionUID = -1440141442L;

    public QProfileLoungesForm(com.querydsl.core.types.Expression<Boolean> boardLikeStatus, com.querydsl.core.types.Expression<Boolean> isBookmark) {
        super(ProfileLoungesForm.class, new Class<?>[]{boolean.class, boolean.class}, boardLikeStatus, isBookmark);
    }

}

