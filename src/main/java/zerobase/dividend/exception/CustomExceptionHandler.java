package zerobase.dividend.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice  //필터와 비슷하게 controller 코드 보다 조금 더 바깥에서 동작하는 레이어 (필터보다 controller 에 더 가까움)
public class CustomExceptionHandler {

    @ExceptionHandler(AbstractException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(AbstractException e){

        ErrorResponse errorResponse = ErrorResponse.builder()
                                        .code(e.getStatusCode())
                                        .message(e.getMessage())
                                        .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.resolve(e.getStatusCode()));
    }
}
