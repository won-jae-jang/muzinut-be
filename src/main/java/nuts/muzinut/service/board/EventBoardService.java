package nuts.muzinut.service.board;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.controller.board.SortType;
import nuts.muzinut.domain.board.Board;
import nuts.muzinut.domain.board.EventBoard;
import nuts.muzinut.domain.board.QEventBoard;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.board.DetailBaseDto;
import nuts.muzinut.dto.board.event.DetailEventBoardDto;
import nuts.muzinut.dto.board.event.EventBoardsDto;
import nuts.muzinut.dto.board.event.EventBoardsForm;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.repository.board.EventBoardRepository;
import nuts.muzinut.repository.board.query.EventBoardQueryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static nuts.muzinut.controller.board.SortType.LIKE;
import static nuts.muzinut.controller.board.SortType.VIEW;
import static nuts.muzinut.domain.board.QBoard.board;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventBoardService extends DetailCommon{

    private final EventBoardRepository eventBoardRepository;
    private final EventBoardQueryRepository queryRepository;

    public EventBoard save(EventBoard eventBoard) {
        return eventBoardRepository.save(eventBoard);
    }

    public EventBoard getEventBoard(Long id) {
        return eventBoardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("찾고자하는 이벤트 게시판이 없습니다"));
    }

    public void updateEventBoard(String filename, String title, String img, Long BoardId) {
        eventBoardRepository.updateEventBoard(filename, title, img, BoardId);
    }

    public void deleteEventBoard(Long id) {
        eventBoardRepository.deleteById(id);
    }

    /**
     * 특정 이벤트 게시판 조회
     * tuple (board, eventBoard, like.count)
     */
    public DetailEventBoardDto getDetailEventBoard(Long boardId, User user) {
        List<Tuple> result = queryRepository.getDetailEventBoard(boardId, user);

        if (result.isEmpty()) {
            return null;
        }

        Tuple first = result.getFirst();
        Board findBoard = first.get(board);
        EventBoard eventBoard = first.get(QEventBoard.eventBoard);
        int view = eventBoard.addView();

        if (findBoard == null) {
            return null;
        }

        DetailEventBoardDto detailEventBoardDto =
                new DetailEventBoardDto(eventBoard.getId(), eventBoard.getUser().getId(), eventBoard.getTitle(), eventBoard.getUser().getNickname(), view,
                        eventBoard.getFilename(), encodeFileToBase64(eventBoard.getUser().getProfileImgFilename()));

        DetailBaseDto detailBaseDto = first.get(2, DetailBaseDto.class);
        detailEventBoardDto.setLikeCount(findBoard.getLikeCount()); //좋아요 수 셋팅
        detailEventBoardDto.setBoardLikeStatus(detailBaseDto.getBoardLikeStatus()); //사용자가 특정 게시판의 좋아요를 눌렀는지 여부
        detailEventBoardDto.setIsBookmark(detailBaseDto.getIsBookmark()); //사용자가 특정 게시판을 북마크했는지 여부

        //게시판 댓글 & 대댓글 셋팅
        detailEventBoardDto.setComments(setCommentsAndReplies(user, findBoard));
        return detailEventBoardDto;
    }

    //모든 게시판 조회
    public EventBoardsDto getEventBoards(int startPage, SortType sortType){

        String sortColumn = "createdDt"; //default 값
        if (sortType == LIKE) {
            sortColumn = "likeCount";
        } else if (sortType == VIEW) {
            sortColumn = "view";
        }

        //Todo 페이지 수 결정하기
        PageRequest pageRequest = PageRequest.of(startPage, 10, Sort.by(Sort.Direction.DESC, sortColumn)); //Todo 한 페이지에 가져올 게시판 수를 정하기
        Page<EventBoard> page = eventBoardRepository.findAll(pageRequest);
        List<EventBoard> eventBoards = page.getContent();

        if (eventBoards.isEmpty()) {
            throw new BoardNotExistException("기재된 이벤트 게시판이 없습니다.");
        }

        EventBoardsDto boardsDto = new EventBoardsDto();
        boardsDto.setPaging(page.getNumber(), page.getTotalPages(), page.getTotalElements()); //paging 처리
        for (EventBoard e : eventBoards) {
            boardsDto.getFreeBoardsForms().add(new EventBoardsForm(e.getId() ,e.getTitle(), e.getUser().getNickname(),
                    e.getCreatedDt(), e.getLikeCount(), e.getView()));
        }
        return boardsDto;
    }

    public boolean checkAuth(Long boardId, User user) {
        Optional<EventBoard> eventBoard = eventBoardRepository.findEventBoardWithUser(boardId);
        EventBoard findEventBoard = eventBoard.orElseThrow(() -> new BoardNotFoundException("게시판이 존재하지 않습니다."));
        return findEventBoard.getUser() == user;
    }
}
