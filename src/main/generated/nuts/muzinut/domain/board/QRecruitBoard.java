package nuts.muzinut.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QRecruitBoard is a Querydsl query type for RecruitBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRecruitBoard extends EntityPathBase<RecruitBoard> {

    private static final long serialVersionUID = 1799236058L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QRecruitBoard recruitBoard = new QRecruitBoard("recruitBoard");

    public final QBoard _super;

    //inherited
    public final ListPath<Bookmark, QBookmark> bookmarks;

    //inherited
    public final ListPath<Comment, QComment> comments;

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDt;

    public final DateTimePath<java.time.LocalDateTime> endDuration = createDateTime("endDuration", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endWorkDuration = createDateTime("endWorkDuration", java.time.LocalDateTime.class);

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

    public final ListPath<RecruitBoardGenre, QRecruitBoardGenre> recruitBoardGenres = this.<RecruitBoardGenre, QRecruitBoardGenre>createList("recruitBoardGenres", RecruitBoardGenre.class, QRecruitBoardGenre.class, PathInits.DIRECT2);

    public final NumberPath<Integer> recruitMember = createNumber("recruitMember", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> startDuration = createDateTime("startDuration", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> startWorkDuration = createDateTime("startWorkDuration", java.time.LocalDateTime.class);

    //inherited
    public final StringPath title;

    // inherited
    public final nuts.muzinut.domain.member.QUser user;

    //inherited
    public final NumberPath<Integer> view;

    public QRecruitBoard(String variable) {
        this(RecruitBoard.class, forVariable(variable), INITS);
    }

    public QRecruitBoard(Path<? extends RecruitBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QRecruitBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QRecruitBoard(PathMetadata metadata, PathInits inits) {
        this(RecruitBoard.class, metadata, inits);
    }

    public QRecruitBoard(Class<? extends RecruitBoard> type, PathMetadata metadata, PathInits inits) {
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

