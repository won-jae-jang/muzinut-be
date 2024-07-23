package nuts.muzinut.domain.board;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAdminBoard is a Querydsl query type for AdminBoard
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdminBoard extends EntityPathBase<AdminBoard> {

    private static final long serialVersionUID = 928402665L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAdminBoard adminBoard = new QAdminBoard("adminBoard");

    public final QBoard _super;

    public final ListPath<AdminUploadFile, QAdminUploadFile> adminUploadFiles = this.<AdminUploadFile, QAdminUploadFile>createList("adminUploadFiles", AdminUploadFile.class, QAdminUploadFile.class, PathInits.DIRECT2);

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

    public QAdminBoard(String variable) {
        this(AdminBoard.class, forVariable(variable), INITS);
    }

    public QAdminBoard(Path<? extends AdminBoard> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAdminBoard(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAdminBoard(PathMetadata metadata, PathInits inits) {
        this(AdminBoard.class, metadata, inits);
    }

    public QAdminBoard(Class<? extends AdminBoard> type, PathMetadata metadata, PathInits inits) {
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

