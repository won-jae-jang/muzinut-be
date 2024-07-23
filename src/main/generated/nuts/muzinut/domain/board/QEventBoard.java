package nuts.muzinut.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventBoard is a Querydsl query type for EventBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventBoard extends EntityPathBase<EventBoard> {

    private static final long serialVersionUID = 1224651902L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventBoard eventBoard = new QEventBoard("eventBoard");

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

    public final StringPath img = createString("img");

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

    public QEventBoard(String variable) {
        this(EventBoard.class, forVariable(variable), INITS);
    }

    public QEventBoard(Path<? extends EventBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventBoard(PathMetadata metadata, PathInits inits) {
        this(EventBoard.class, metadata, inits);
    }

    public QEventBoard(Class<? extends EventBoard> type, PathMetadata metadata, PathInits inits) {
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

