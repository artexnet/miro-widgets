package com.miro.hw.artexnet.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.miro.hw.artexnet.exception.NotFoundException;
import com.miro.hw.artexnet.exception.ValidationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class ApiErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (ex.getCause() instanceof InvalidFormatException) {
            return handleExceptionInternal(ex, ex.getMessage(), headers, HttpStatus.UNPROCESSABLE_ENTITY, request);
        }
        return handleExceptionInternal(ex, ex.getMessage(), headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        final String code = ex.getErrorCode();
        final String property = ex.getPropertyName();
        final Object value = ex.getValue();
        String message = String.format("Value [%s] of property [%s] is not valid: %s", value, property, code);
        return handleExceptionInternal(ex, message, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        final StringBuilder sb = new StringBuilder();
        for (FieldError fe : fieldErrors) {
            sb.append(fe.getField()).append(" ");
            sb.append(fe.getDefaultMessage()).append("; ");
        }
        return handleExceptionInternal(ex, sb.toString(), headers, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { ValidationException.class })
    protected ResponseEntity<Object> handleValidationError(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpHeaders.EMPTY, HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(value = { NotFoundException.class })
    protected ResponseEntity<Object> handleNotFoundError(RuntimeException ex, WebRequest request) {
        return handleExceptionInternal(ex, ex.getMessage(), HttpHeaders.EMPTY, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(TransactionSystemException.class)
    protected ResponseEntity<List<String>> handleTransactionException(TransactionSystemException ex) throws Throwable {
        Throwable cause = ex.getCause();

        if (!(cause instanceof RollbackException)) throw cause;
        if (!(cause.getCause() instanceof ConstraintViolationException)) throw cause.getCause();

        ConstraintViolationException validationException = (ConstraintViolationException) cause.getCause();
        List<String> messages = validationException.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        return new ResponseEntity<>(messages, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
