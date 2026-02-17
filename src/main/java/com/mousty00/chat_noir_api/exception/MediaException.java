package com.mousty00.chat_noir_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class MediaException extends ApiException {

    @Getter
    public enum MediaErrorCode {
        MEDIA_NOT_FOUND("MEDIA_001", HttpStatus.NOT_FOUND),
        INVALID_MEDIA_DATA("MEDIA_002", HttpStatus.BAD_REQUEST),
        MEDIA_SAVE_ERROR("MEDIA_003", HttpStatus.INTERNAL_SERVER_ERROR),
        MEDIA_DELETE_ERROR("MEDIA_004", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String code;
        private final HttpStatus status;

        MediaErrorCode(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }
    }

    public MediaException(String message, MediaException.MediaErrorCode errorCode) {
        super(message, errorCode.getCode(), errorCode.getStatus());
    }

    public MediaException(String message, MediaException.MediaErrorCode errorCode, Throwable throwable) {
        super(message, errorCode.getCode(), errorCode.getStatus(), throwable);
    }

    public static MediaException mediaInvalid(Throwable cause) {
        return new MediaException("Invalid media file: " + cause.getMessage(), MediaErrorCode.INVALID_MEDIA_DATA, cause);
    }

    public static MediaException mediaSaveError(Throwable cause) {
        return new MediaException("Error uploading media: " + cause.getMessage(), MediaErrorCode.MEDIA_SAVE_ERROR, cause);
    }

    public static MediaException mediaDeleteError(Throwable cause) {
        return new MediaException("Error deleting media: " + cause.getMessage(), MediaErrorCode.MEDIA_DELETE_ERROR, cause);
    }
}
