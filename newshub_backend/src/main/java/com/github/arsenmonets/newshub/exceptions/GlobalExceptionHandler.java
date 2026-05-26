package com.github.arsenmonets.newshub.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import io.jsonwebtoken.JwtException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

        // --- 404 NOT FOUND ---

        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ErrorResponse handleResourceNotFound(ResourceNotFoundException ex) {
                return new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.NOT_FOUND.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ErrorResponse handleNoResourceFound(
                        org.springframework.web.servlet.resource.NoResourceFoundException ex) {
                return new ErrorResponse(
                                "Ресурс не знайдено: " + ex.getResourcePath(),
                                HttpStatus.NOT_FOUND.value(),
                                LocalDateTime.now());
        }

        // --- 400 BAD REQUEST ---

        @ExceptionHandler(BadRequestException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ErrorResponse handleBadRequest(BadRequestException ex) {
                return new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .reduce((acc, error) -> acc + ", " + error)
                                .orElse("Помилка валідації");

                return new ErrorResponse(
                                message,
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ErrorResponse handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                return new ErrorResponse(
                                String.format("Параметр '%s' має бути типу %s", ex.getName(),
                                                ex.getRequiredType().getSimpleName()),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ErrorResponse handleMissingParams(
                        org.springframework.web.bind.MissingServletRequestParameterException ex) {
                return new ErrorResponse(
                                "Відсутній обов'язковий параметр: " + ex.getParameterName(),
                                HttpStatus.BAD_REQUEST.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
        @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
        public ErrorResponse handleMethodNotSupported(
                        org.springframework.web.HttpRequestMethodNotSupportedException ex) {
                return new ErrorResponse(
                                "Метод " + ex.getMethod() + " не підтримується для цього шляху",
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                LocalDateTime.now());
        }

        // --- 401 & 403 SECURITY ---

        @ExceptionHandler(BadCredentialsException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ErrorResponse handleBadCredentials(BadCredentialsException ex) {
                return new ErrorResponse(
                                "Невірний логін або пароль",
                                HttpStatus.UNAUTHORIZED.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(AuthenticationException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ErrorResponse handleAuthenticationException(AuthenticationException ex) {
                return new ErrorResponse(
                                "Ви не авторизовані. Увійдіть в систему",
                                HttpStatus.UNAUTHORIZED.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(JwtException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ErrorResponse handleInvalidToken(JwtException ex) {
                return new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.UNAUTHORIZED.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ErrorResponse handleAccessDeniedException(org.springframework.security.access.AccessDeniedException ex) {
                return new ErrorResponse(
                                "У вас немає прав доступу до цього ресурсу",
                                HttpStatus.FORBIDDEN.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(CustomAccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ErrorResponse handleCustomAccessDenied(CustomAccessDeniedException ex) {
                return new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.FORBIDDEN.value(),
                                LocalDateTime.now());
        }

        // --- 409 CONFLICT & 423 LOCKED ---

        @ExceptionHandler(ResourceAlreadyExistsException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ErrorResponse handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
                return new ErrorResponse(
                                ex.getMessage(),
                                HttpStatus.CONFLICT.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
        @ResponseStatus(HttpStatus.CONFLICT)
        public ErrorResponse handleDataIntegrityViolation(org.springframework.dao.DataIntegrityViolationException ex) {
                return new ErrorResponse(
                                "Ресурс з такими даними вже існує (порушення цілісності даних)",
                                HttpStatus.CONFLICT.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(LockedException.class)
        @ResponseStatus(HttpStatus.LOCKED)
        public ErrorResponse handleLocked(LockedException ex) {
                return new ErrorResponse(
                                "Ваш акаунт заблокований. Зв'яжіться з адміністратором",
                                HttpStatus.LOCKED.value(),
                                LocalDateTime.now());
        }

        // --- 500 & 503 DATABASE / SERVER ERRORS ---

        @ExceptionHandler(org.springframework.dao.DataAccessException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ErrorResponse handleDataAccessException(org.springframework.dao.DataAccessException ex) {
                return new ErrorResponse(
                                "Помилка доступу до бази даних",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(org.hibernate.exception.JDBCConnectionException.class)
        @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
        public ErrorResponse handleJDBCConnectionException(org.hibernate.exception.JDBCConnectionException ex) {
                return new ErrorResponse(
                                "База даних недоступна. Спробуйте пізніше",
                                HttpStatus.SERVICE_UNAVAILABLE.value(),
                                LocalDateTime.now());
        }

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ErrorResponse handleGeneralException(Exception ex) {
                return new ErrorResponse(
                                "Внутрішня помилка сервера. Тип помилки: " + ex.getClass().getCanonicalName(),
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                LocalDateTime.now());
        }
}