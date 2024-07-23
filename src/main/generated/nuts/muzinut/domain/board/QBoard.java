package nuts.muzinut.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = 1703744404L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBoard board = new QBoard("board");

    public final nuts.muzinut.domain.baseEntity.QBaseBoardEntity _super = new nuts.muzinut.domain.baseEntity.QBaseBoardEntity(this);

    public final ListPath<Bookmark, QBookmark> bookmarks = this.<Bookmark, QBookmark>createList("bookmarks", Bookmark.class, QBookmark.class, PathInits.DIRECT2);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDt = _super.createdDt;

    public final StringPath filename = createString("filename");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final NumberPath<Integer> likeCount = _super.likeCount;

    public final ListPath<Like, QLike> likes = this.<Like, QLike>createList("likes", Like.class, QLike.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> modifiedDt = _super.modifiedDt;

    //inherited
    public final StringPath title = _super.title;

    public final nuts.muzinut.domain.member.QUser user;

    //inherited
    public final NumberPath<Integer> view = _super.view;

    public QBoard(String variable) {
        this(Board.class, forVariable(variable), INITS);
    }

    public QBoard(Path<? extends Board> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBoard(PathMetadata metadata, PathInits inits) {
        this(Board.class, metadata, inits);
    }

    public QBoard(Class<? extends Board> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new nuts.muzinut.domain.member.QUser(forProperty("user"), inits.get("user")) : null;
    }

}

