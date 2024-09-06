package com.ds.deploysurfingbackend.global.exception;

import com.ds.deploysurfingbackend.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.ds.deploysurfingbackend.global.exception.CommonErrorCode.INVALID_VALUE;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //custom Exception
    @ExceptionHandler({CustomException.class})
    protected ApiResponse<?> handleCustomException(CustomException e) {
        return ApiResponse.onFailure(e.getErrorCode().getCode(), e.getErrorMessage());

    }

    //@valid Exception
    @ExceptionHandler({MethodArgumentNotValidException.class})
    protected ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        CustomException validException = new CustomException(INVALID_VALUE, message);

        return ApiResponse.onFailure(validException.getErrorCode().getCode(), validException.getErrorMessage());
    }


    //일반 예외처리
//    @ExceptionHandler({Exception.class})
//    protected ResponseEntity<?> handleServerException(Exception ex) {
//        CustomException exception = new CustomException(SERVER_ERROR);
//        return ResponseEntity
//            .status(SERVER_ERROR.getHttpStatus())
//            .body(new ErrorResponse(exception));
//    }

}
