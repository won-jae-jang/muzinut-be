package nuts.muzinut.dto.board.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nuts.muzinut.domain.board.AdminBoard;
import nuts.muzinut.domain.board.AdminUploadFile;
import nuts.muzinut.domain.board.Comment;
import nuts.muzinut.dto.board.DetailBaseDto;
import nuts.muzinut.dto.board.comment.CommentDto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//특정 어드민 게시판을 반환할때 사용하는 dto
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DetailAdminBoardDto extends DetailBaseDto {

    private Long id; //게시판 pk
    private Long writerId; //작성자 pk
    private int likeCount;
    private String title;
    private String writer = "muzi";
    private String quillFilename;
    private String profileImg; //저장된 게시판 작성자의 프로필 이미지 경로
    private int view;
    private LocalDateTime createdDt;

    private List<CommentDto> comments = new ArrayList<>();
    private List<AdminFilename> adminFilenames = new ArrayList<>();

    public DetailAdminBoardDto(String title, int view, List<AdminUploadFile> files, String quillFilename, String profileImg
            ,LocalDateTime createdDt) {
        this.title = title;
        this.view = view;
        this.quillFilename = quillFilename;
        this.profileImg = profileImg;
        this.createdDt = createdDt;
        for (AdminUploadFile file : files) {
            this.adminFilenames.add(new AdminFilename(file.getStoreFilename(), file.getOriginFilename(), file.getId()));
        }
    }

    public DetailAdminBoardDto(String title, int view, String quillFilename, String profileImg, LocalDateTime createdDt) {
        this.title = title;
        this.view = view;
        this.quillFilename = quillFilename;
        this.profileImg = profileImg;
        this.createdDt = createdDt;
    }


    public DetailAdminBoardDto(String title, String writer, int view, List<AdminUploadFile> files) {
        this.title = title;
        this.writer = writer;
        this.view = view;
        for (AdminUploadFile file : files) {
            this.adminFilenames.add(new AdminFilename(file.getStoreFilename(), file.getOriginFilename(), file.getId()));
        }
    }

    public void setAdminBoard(AdminBoard adminBoard) {
        title = adminBoard.getTitle();
        view = adminBoard.getView();

        List<AdminUploadFile> adminUploadFiles = adminBoard.getAdminUploadFiles();
        List<AdminFilename> adminFilenames = new ArrayList<>();

        for (AdminUploadFile adminUploadFile : adminUploadFiles) {
            adminFilenames.add(new AdminFilename(adminUploadFile.getStoreFilename(),
                    adminUploadFile.getOriginFilename(),adminUploadFile.getId()));
        }
    }
}
