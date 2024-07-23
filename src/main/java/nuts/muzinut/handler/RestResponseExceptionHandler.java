package nuts.muzinut.handler;

import lombok.extern.slf4j.Slf4j;
import nuts.muzinut.dto.ErrorDto;
import nuts.muzinut.exception.*;
import nuts.muzinut.exception.board.BoardNotExistException;
import nuts.muzinut.exception.board.BoardNotFoundException;
import nuts.muzinut.exception.board.BoardSearchTypeNotExistException;
import nuts.muzinut.exception.chat.AlreadyExistRequestException;
import nuts.muzinut.exception.chat.BlockUserException;
import nuts.muzinut.exception.token.ExpiredTokenException;
import nuts.muzinut.exception.token.IllegalTokenException;
import nuts.muzinut.exception.token.TokenException;
import nuts.muzinut.exception.token.UnsupportedTokenException;
import nuts.muzinut.exception.user.AlreadyExistUser;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Order(1)
@RestControllerAdvice
public class RestResponseExceptionHandler extends ResponseEntityExceptionHandler {

    //이미 존재하는 회원이 회원가입을 시도할 때 발생한다
    @ResponseStatus(CONFLICT)
    @ExceptionHandler(value = { DuplicateMemberException.class, AlreadyExistUser.class })
    @ResponseBody
    private ErrorDto conflict(RuntimeException ex, WebRequest request) {
        return new ErrorDto(CONFLICT.value(), ex.getMessage());
    }

    @ResponseStatus(FORBIDDEN)
    @ExceptionHandler(value = { NotFoundMemberException.class, AccessDeniedException.class,
            InvalidPasswordException.class, BlockUserException.class})
    @ResponseBody
    private ErrorDto forbidden(RuntimeException ex, WebRequest request) {
        return new ErrorDto(FORBIDDEN.value(), ex.getMessage());
    }

    //토큰에 대한 예외처리 추가
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(value = { EmailVertFailException.class, NotFoundEntityException.class,
            BoardNotFoundException.class, NoUploadFileException.class, TokenException.class,
            UnsupportedTokenException.class, IllegalTokenException.class, NullPointerException.class,
            BoardSearchTypeNotExistException.class})
    @ResponseBody
    private ErrorDto BAD_REQUEST(RuntimeException ex, WebRequest request){
        return new ErrorDto(BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(NOT_ACCEPTABLE)
    @ExceptionHandler(value = { ExpiredTokenException.class, AlreadyExistRequestException.class})
    @ResponseBody
    private ErrorDto NOT_ACCEPTABLE(RuntimeException ex, WebRequest request){
        return new ErrorDto(NOT_ACCEPTABLE.value(), ex.getMessage());
    }

    
    @ResponseStatus(NO_CONTENT)
    @ExceptionHandler(value = {BoardNotExistException.class, NotFoundFileException.class})
    @ResponseBody
    private ErrorDto NO_CONTENT(RuntimeException ex, WebRequest request) {
        log.info("NotFoundFileException 호출");
        return new ErrorDto(NO_CONTENT.value(), ex.getMessage());
    }

    // 새로운 예외 핸들러 추가
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(value = { IllegalArgumentException.class })
    @ResponseBody
    private ErrorDto handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ErrorDto(BAD_REQUEST.value(), ex.getMessage());
    }

    @ResponseStatus(NOT_FOUND)
    @ExceptionHandler(value = { NoDataFoundException.class })
    @ResponseBody
    private ErrorDto notFound(RuntimeException ex, WebRequest request) {
        return new ErrorDto(NOT_FOUND.value(), ex.getMessage());
    }

    @ResponseStatus(REQUEST_ENTITY_TOO_LARGE)
    @ExceptionHandler(value = { LimitPlayNutException.class })
    @ResponseBody
    private ErrorDto limitPlayNut(RuntimeException ex, WebRequest request) {
        return new ErrorDto(REQUEST_ENTITY_TOO_LARGE.value(), ex.getMessage());
    }
    // 앨범 Entity 생성 실패 핸들러 추가
    @ResponseStatus(SERVICE_UNAVAILABLE)
    @ExceptionHandler(value = { AlbumCreateFailException.class })
    @ResponseBody
    private ErrorDto INTERNAL_SERVER_ERROR(AlbumCreateFailException ex, WebRequest request) {
        return new ErrorDto(SERVICE_UNAVAILABLE.value(), ex.getMessage());
    }

    // 앨범 Entity 갯수 초과 핸들러 추가
    @ResponseStatus(REQUEST_ENTITY_TOO_LARGE)
    @ExceptionHandler(value = { EntityOversizeException.class, LackVoteAmountException.class })
    @ResponseBody
    private ErrorDto REQUEST_ENTITY_TOO_LARGE(RuntimeException ex, WebRequest request) {
        return new ErrorDto(REQUEST_ENTITY_TOO_LARGE.value(), ex.getMessage());
    }
}
