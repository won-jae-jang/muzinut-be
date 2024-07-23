package nuts.muzinut.service.board;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.controller.board.SortType;
import nuts.muzinut.domain.board.*;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.board.DetailBaseDto;
import nuts.muzinut.dto.board.free.DetailFreeBoardDto;
import nuts.muzinut.dto.board.free.FreeBoardsDto;
import nuts.muzinut.dto.board.free.FreeBoardsForm;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.exception.NotFoundEntityException;
import nuts.muzinut.repository.board.BoardRepository;
import nuts.muzinut.repository.board.FreeBoardRepository;
import nuts.muzinut.repository.board.query.FreeBoardQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static nuts.muzinut.controller.board.SortType.LIKE;
import static nuts.muzinut.controller.board.SortType.VIEW;
import static nuts.muzinut.domain.board.QBoard.board;
import static nuts.muzinut.domain.board.QFreeBoard.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FreeBoardService extends DetailCommon{

    private final FreeBoardRepository freeBoardRepository;
    private final BoardRepository boardRepository;
    private final FreeBoardQueryRepository queryRepository;

    public FreeBoard save(FreeBoard freeBoard) {
        return freeBoardRepository.save(freeBoard);
    }

    /**
     * @throws BoardNotFoundException: 찾고자 하는 자유 게시판이 없는 경우 404
     * @return: FreeBoard 엔티티
     */
    public FreeBoard getFreeBoard(Long id) {
        return freeBoardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("찾고자하는 자유게시판이 없습니다"));
    }

    public void deleteFreeBoard(Long id) {
        freeBoardRepository.deleteById(id);
    }

    public void updateFreeBoard(Long id, String title, String filename) {
        freeBoardRepository.updateFreeBoard(filename, title, id);
    }

    //모든 게시판 조회
    public FreeBoardsDto getFreeBoards(int startPage, SortType sortType){

        String sortColumn = "createdDt"; //default 값
        if (sortType == LIKE) {
            sortColumn = "likeCount";
        } else if (sortType == VIEW) {
            sortColumn = "view";
        }

        PageRequest pageRequest = PageRequest.of(startPage, 10, Sort.by(Sort.Direction.DESC, sortColumn)); //Todo 한 페이지에 가져올 게시판 수를 정하기
        Page<FreeBoard> page = freeBoardRepository.findAll(pageRequest);
        List<FreeBoard> freeBoards = page.getContent();

        if (freeBoards.isEmpty()) {
            throw new BoardNotExistException("기재된 어드민 게시판이 없습니다.");
        }

        FreeBoardsDto boardsDto = new FreeBoardsDto();
        boardsDto.setPaging(page.getNumber(), page.getTotalPages(), page.getTotalElements()); //paging 처리
        for (FreeBoard f : freeBoards) {
            boardsDto.getFreeBoardsForms().add(new FreeBoardsForm(f.getId() ,f.getTitle(), f.getUser().getNickname(),
                    f.getCreatedDt(), f.getLikeCount(), f.getView()));
        }
        return boardsDto;
    }

    /**
     * 특정 자유 게시판 조회
     * tuple (board, freeBoard, like.count)
     */
    public DetailFreeBoardDto detailFreeBoard(Long boardId, User user) {
        List<Tuple> result = queryRepository.getDetailFreeBoard(boardId, user);

        log.info("tuple: {}", result);
        if (result.isEmpty()) {
            throw new NotFoundEntityException("찾고자 하는 게시판이 없습니다.");
        }

        Tuple first = result.getFirst();
        Board findBoard = first.get(board);
        FreeBoard findFreeBoard = first.get(freeBoard);
        int view = findFreeBoard.addView();

        if (findBoard == null || findFreeBoard == null) {
            return null;
        }

        DetailFreeBoardDto detailFreeBoardDto =
                new DetailFreeBoardDto(findFreeBoard.getId(), findFreeBoard.getUser().getId(),findFreeBoard.getTitle(),
                        findFreeBoard.getUser().getNickname(), view, findFreeBoard.getFilename(),
                        encodeFileToBase64(findFreeBoard.getUser().getProfileImgFilename()), findFreeBoard.getCreatedDt());

        DetailBaseDto detailBaseDto = first.get(2, DetailBaseDto.class);
        detailFreeBoardDto.setLikeCount(findBoard.getLikeCount()); //좋아요 수 셋팅
        detailFreeBoardDto.setBoardLikeStatus(detailBaseDto.getBoardLikeStatus()); //사용자가 특정 게시판의 좋아요를 눌렀는지 여부
        detailFreeBoardDto.setIsBookmark(detailBaseDto.getIsBookmark()); //사용자가 특정 게시판을 북마크했는지 여부

        //게시판 댓글 & 대댓글 셋팅
        detailFreeBoardDto.setComments(setCommentsAndReplies(user, findBoard));

        return detailFreeBoardDto;
    }

    public boolean checkAuth(Long boardId, User user) {
        Optional<FreeBoard> freeBoard = freeBoardRepository.findFreeBoardWithUser(boardId);
        FreeBoard findFreeBoard = freeBoard.orElseThrow(() -> new BoardNotFoundException("게시판이 존재하지 않습니다."));
        return findFreeBoard.getUser() == user;
    }
}
