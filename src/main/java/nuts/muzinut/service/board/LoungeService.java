package nuts.muzinut.service.board;

import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.domain.board.*;
import nuts.muzinut.domain.member.User;
import nuts.muzinut.dto.board.DetailBaseDto;
import nuts.muzinut.dto.board.lounge.DetailLoungeDto;
import nuts.muzinut.dto.board.lounge.LoungeDto;
import nuts.muzinut.dto.board.lounge.LoungesForm;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.exception.NotFoundMemberException;
import nuts.muzinut.repository.board.LoungeRepository;
import nuts.muzinut.repository.board.query.LoungeQueryRepository;
import nuts.muzinut.repository.member.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static nuts.muzinut.domain.board.QBoard.board;
import static nuts.muzinut.domain.board.QLounge.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoungeService extends DetailCommon{

    private final LoungeRepository loungeRepository;
    private final LoungeQueryRepository queryRepository;
    private final UserRepository userRepository;

    public Lounge save(Lounge lounge) {
        return loungeRepository.save(lounge);
    }

    /**
     * @throws BoardNotFoundException: 찾고자 하는 자유 게시판이 없는 경우 404
     * @return: Lounge 엔티티
     */
    public Lounge getLounge(Long id) {
        return loungeRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("찾고자 하는 라운지 게시판이 없습니다"));
    }

    public void deleteLounge(Long id) {
        loungeRepository.deleteById(id);
    }

    public void updateLounge(Long id, String filename) {
        loungeRepository.updateLounge(filename, id);
    }

    //모든 라운지 조회
    public LoungeDto getLounges(int startPage) throws BoardNotExistException {
        return getLoungesByUserId(null, startPage);
    }

    // 특정 사용자의 라운지 조회
    public LoungeDto getLoungesByUserId(Long userId, int startPage) throws BoardNotExistException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundMemberException("존재하지 않는 회원입니다."));

        PageRequest pageRequest = PageRequest.of(startPage, 10, Sort.by(Sort.Direction.DESC, "createdDt"));
        Page<Lounge> page;

        page = loungeRepository.findAllByUserId(userId, pageRequest);

        List<Lounge> lounges = page.getContent();

        LoungeDto loungesDto = new LoungeDto();
        loungesDto.setPaging(page.getNumber(), page.getTotalPages(), page.getTotalElements()); // paging 처리
        for (Lounge l : lounges) {
            loungesDto.getLoungesForms().add(new LoungesForm(l.getId(), l.getUser().getNickname(), l.getFilename(),
                    l.getCreatedDt(), l.getLikes().size(), l.getComments().size()));
        }
        return loungesDto;
    }

    /**
     * 특정 라운지 게시판 조회
     * tuple (board, lounge, detailBaseDto)
     */
    public DetailLoungeDto detailLounge(Long boardId, User user) {
        List<Tuple> result = queryRepository.getDetatilLounge(boardId, user);

        log.info("tuple: {}", result);
        if (result.isEmpty()) {
            throw new BoardNotFoundException("라운지 게시판이 존재하지 않습니다.");
        }

        Tuple first = result.getFirst();
        Board findBoard = first.get(board);
        Lounge findLounge = first.get(lounge);
        int view = findLounge.addView();

        if (findBoard == null || findLounge == null) {
            return null;
        }

        DetailLoungeDto detailLoungeDto = new DetailLoungeDto(findLounge.getId(), findLounge.getUser().getNickname(),
                view ,findLounge.getFilename(), encodeFileToBase64(findLounge.getUser().getProfileImgFilename()), findLounge.getCreatedDt());

        DetailBaseDto detailBaseDto = first.get(2, DetailBaseDto.class);
        detailLoungeDto.setLikeCount(findBoard.getLikeCount()); //좋아요 수 셋팅
        detailLoungeDto.setBoardLikeStatus(detailBaseDto.getBoardLikeStatus()); //사용자가 특정 게시판의 좋아요를 눌렀는지 여부
        detailLoungeDto.setIsBookmark(detailBaseDto.getIsBookmark()); //사용자가 특정 게시판을 북마크했는지 여부

        //게시판 댓글 & 대댓글 셋팅
        detailLoungeDto.setComments(setCommentsAndReplies(user, findBoard));

        return detailLoungeDto;
    }

    public boolean checkAuth(Long boardId, User user) {
        Optional<Lounge> findLounge = loungeRepository.findLoungeWithUser(boardId);
        Lounge lounge = findLounge.orElseThrow(() -> new BoardNotFoundException("게시판이 존재하지 않습니다."));
        return lounge.getUser() == user;
    }
}
