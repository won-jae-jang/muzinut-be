package nuts.muzinut.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLounge is a Querydsl query type for Lounge
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLounge extends EntityPathBase<Lounge> {

    private static final long serialVersionUID = 1563352652L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLounge lounge = new QLounge("lounge");

    public final QBoard _super;

    //inherited
    public final ListPath<Bookmark, QBookmark> bookmarks;

    //inherited
    public final ListPath<Comment, QComment> comments;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDt;

    //inherited
    public final StringPath filename;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final NumberPath<Integer> likeCount;

    //inherited
    public final ListPath<Like, QLike> likes;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDt;

    //inherited
    public final StringPath title;

    // inherited
    public final nuts.muzinut.domain.member.QUser user;

    //inherited
    public final NumberPath<Integer> view;

    public QLounge(String variable) {
        this(Lounge.class, forVariable(variable), INITS);
    }

    public QLounge(Path<? extends Lounge> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLounge(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLounge(PathMetadata metadata, PathInits inits) {
        this(Lounge.class, metadata, inits);
    }

    public QLounge(Class<? extends Lounge> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QBoard(type, metadata, inits);
        this.bookmarks = _super.bookmarks;
        this.comments = _super.comments;
        this.createdDt = _super.createdDt;
        this.filename = _super.filename;
        this.id = _super.id;
        this.likeCount = _super.likeCount;
        this.likes = _super.likes;
        this.modifiedDt = _super.modifiedDt;
        this.title = _super.title;
        this.user = _super.user;
        this.view = _super.view;
    }

}

