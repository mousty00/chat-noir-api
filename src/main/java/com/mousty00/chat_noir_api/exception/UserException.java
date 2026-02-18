package com.mousty00.chat_noir_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserException extends ApiException {

    public UserException(String message, UserErrorCode errorCode) {
        super(message, errorCode.getCode(), errorCode.getStatus());
    }

    public UserException(String message, UserErrorCode errorCode, Throwable throwable) {
        super(message, errorCode.getCode(), errorCode.getStatus(), throwable);
    }

    public static UserException userDeleteError(Exception e) {
        return new UserException("Error deleting user: " + e.getMessage(),
                UserException.UserErrorCode.USER_DELETE_ERROR, e);
    }

    public static UserException userNotFoundById(UUID id) {
        return new UserException("User not found with id: " + id, UserException.UserErrorCode.USER_NOT_FOUND);
    }

    public static UserException userNotFound() {
        return new UserException("User not found", UserErrorCode.USER_NOT_FOUND);
    }

    public static UserException userMediaNotFound(UUID id) {
        return new UserException("user media not found" + id, UserException.UserErrorCode.USER_NOT_FOUND);
    }

    public static UserException userSaveError(String message, Throwable cause) {
        return new UserException("Error saving user: " + message, UserException.UserErrorCode.USER_SAVE_ERROR, cause);
    }

    @Getter
    public enum UserErrorCode {
        USER_NOT_FOUND("USER_001", HttpStatus.NOT_FOUND),
        USER_MEDIA_NOT_FOUND("USER_002", HttpStatus.NOT_FOUND),
        DUPLICATE_USER("USER_003", HttpStatus.CONFLICT),
        INVALID_USER_DATA("USER_004", HttpStatus.BAD_REQUEST),
        USER_SAVE_ERROR("USER_005", HttpStatus.INTERNAL_SERVER_ERROR),
        USER_DELETE_ERROR("USER_006", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String code;
        private final HttpStatus status;

        UserErrorCode(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }
    }
}
