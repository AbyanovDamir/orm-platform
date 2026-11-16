package ru.learning.platform.web;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import ru.learning.platform.service.PlatformException;

@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler(PlatformException.class)
    ProblemDetail platform(PlatformException error) {
        HttpStatus status = switch (error.getKind()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case BAD_INPUT -> HttpStatus.BAD_REQUEST;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case CONFLICT -> HttpStatus.CONFLICT;
        };
        return ProblemDetail.forStatusAndDetail(status, error.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidBody(MethodArgumentNotValidException error) {
        ProblemDetail response = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Проверьте поля запроса");
        response.setProperty("errors", error.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage()).toList());
        return response;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class})
    ProblemDetail invalidRequest() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Неверный формат или значение параметра запроса");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Запись уже существует или связана с другими объектами");
    }
}
