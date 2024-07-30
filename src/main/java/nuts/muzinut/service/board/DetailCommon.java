package nuts.muzinut.service.board;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.*;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.board.board.BoardsDto;
import nuts.muzinut.dto.board.board.BoardsForm;
import nuts.muzinut.dto.board.comment.CommentDto;
import nuts.muzinut.dto.board.comment.ReplyDto;
import nuts.muzinut.repository.board.query.BoardQueryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

//모든 상세 게시판 페이지에서 공통으로 쓰는 기능
@Slf4j
public class DetailCommon {


    @Value("${spring.file.dir}")
    private String fileDir;

    @Value("${spring.file.profile-base-img}")
    private String profileBaseImg;

    @Value("${spring.file.banner-base-img}")
    private String bannerBaseImg;

    /**
     * 게시판에 사용자가 좋아요를 눌렀는지 확인
     * @param user: 좋아요를 눌렀는지 확인하고 싶은 유저
     * @param board: 좋아요 대상이 되는 게시판
     */
    public boolean isLike(User user, Board board) {
        for (Like l : board.getLikes()) {
            if (l.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 댓글에 사용자가 좋아요를 눌렀는지 확인
     * @param user: 좋아요를 눌렀는지 확인하고 싶은 유저
     * @param comment: 좋아요 대상이 되는 댓글
     */
    public boolean isLike(User user, Comment comment) {
        for (CommentLike l : comment.getCommentLikes()) {
            if (l.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 사용자가 게시판을 북마크 했는지 여부를 파악하기 위한 메서드
     * @param user: 북마크를 했는지 확인하고 싶은 유저
     * @param board: 북마크 대상이 되는 게시판
     */
    public boolean isBookmark(User user, Board board) {
        for (Bookmark b : board.getBookmarks()) {
            if (b.getUser().equals(user)) {
                return true;
            }
        }
        return false;
    }

    //사용자 프로필 이미지명을 가져와서 base64로 인코딩 하여 반환하는 메서드
    public String encodeFileToBase64(String filename){
        File file;
        try {
            if (StringUtils.hasText(filename)) {
                file = new File(fileDir + filename); //설정된 프로필 이미지
            } else {
                file = new File(profileBaseImg); //기본 프로필 이미지
            }
            byte[] fileContent = Files.readAllBytes(file.toPath());
            return Base64.getEncoder().encodeToString(fileContent);

        } catch (IOException e) {
            log.info("{} 파일 없음", filename);
            return null;
        }
    }

    // 이미지 파일명을 가져와서 base64로 인코딩 하여 반환하는 메서드(프로필, 배너 기본이미지 코드 추가)
    public String encodeFileToBase64(String filename, boolean isBanner) {
        log.info("파일명: {}", filename);
        try {
            File file;
            if (StringUtils.hasText(filename)) {
                file = new File(fileDir + filename);
                if (!file.exists()) {
                    // 파일이 없을 경우 기본 이미지 사용
                    file = new File(isBanner ? bannerBaseImg : profileBaseImg);
                }
            } else {
                // 파일명이 null이거나 공백일 경우 기본 이미지 사용
                file = new File(isBanner ? bannerBaseImg : profileBaseImg);
            }
            byte[] fileContent = Files.readAllBytes(file.toPath());
            log.info("인코딩 된 파일명 : {}", Base64.getEncoder().encodeToString(fileContent));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            log.info("{} 파일 없음", filename);
            return null;
        }
    }


    //이미지 파일명을 가져와서 base64로 인코딩 하여 반환하는 메서드
    public String encodeAlbumFileToBase64(String filename){
        log.info("파일명: {}", filename);
        try {
            if (StringUtils.hasText(filename)) {
                File file = new File(fileDir + "/albumImg/" + filename);
                byte[] fileContent = Files.readAllBytes(file.toPath());
                return Base64.getEncoder().encodeToString(fileContent);
            }
            return null;

        } catch (IOException e) {
            log.info("{} 파일 없음", filename);
            return null;
        }
    }


    public List<CommentDto> setCommentsAndReplies(User user, Board board) {
        List<CommentDto> comments = new ArrayList<>();

        for (Comment c : board.getComments()) {
            CommentDto commentDto;
            //사용자의 프로필 경로 추출

            if (user != null) {
                // 회원인 경우
                commentDto = new CommentDto(
                        c.getId(), c.getContent(), c.getUser().getNickname(),c.getUser().getId(),
                        c.getCreatedDt(), encodeFileToBase64(c.getUser().getProfileImgFilename(), false),
                        isLike(user, c), c.getCommentLikes().size()); //isLike 추가 (댓글에 대한 좋아요를 했는지 확인)
            } else {
                // 비회원인 경우
                commentDto = new CommentDto(
                        c.getId(), c.getContent(), c.getUser().getNickname(), c.getUser().getId(),
                        c.getCreatedDt(), encodeFileToBase64(c.getUser().getProfileImgFilename(), false),
                        c.getCommentLikes().size());
            }

            List<ReplyDto> replies = new ArrayList<>();
            for (Reply r : c.getReplies()) {
                replies.add(new ReplyDto(
                        r.getId(), r.getContent(), r.getUser().getNickname(), r.getUser().getId(),
                        r.getCreatedDt(), encodeFileToBase64(r.getUser().getProfileImgFilename(), false)));
            }
            commentDto.setReplies(replies);
            comments.add(commentDto);
        }
        return comments;
    }

    public Set<String> getProfileImages(String profileImg, List<CommentDto> comments) {

        Set<String> profileImages = new HashSet<>();
        addWriterProfile(profileImages, profileImg); //게시판 작성자의 프로필 추가

        for (CommentDto c : comments) {
            addWriterProfile(profileImages, c.getCommentProfileImg()); //댓글 작성자의 프로필 추가

            for (ReplyDto r : c.getReplies()) {
                addWriterProfile(profileImages, r.getReplyProfileImg()); //대댓글 작성자의 프로필 추가
            }
        }
        return profileImages;
    }

    private void addWriterProfile(Set<String> profileImages, String profileImg) {
        if (StringUtils.hasText(profileImg)) {
            profileImages.add(profileImg);
        }
    }

}
