package com.mousty00.chat_noir_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CatException extends ApiException {

    public CatException(String message, CatErrorCode errorCode) {
        super(message, errorCode.getCode(), errorCode.getStatus());
    }

    public CatException(String message, CatErrorCode errorCode, Throwable cause) {
        super(message, errorCode.getCode(), errorCode.getStatus(), cause);
    }

    public static CatException catDeleteError(Exception e) {
        return new CatException("Error deleting cat: " + e.getMessage(),
                CatException.CatErrorCode.CAT_DELETE_ERROR, e);
    }

    public static CatException catNotFound(UUID id) {
        return new CatException("Cat not found with id: " + id, CatErrorCode.CAT_NOT_FOUND);
    }

    public static CatException catMediaNotFound(UUID id) {
        return new CatException("cat media not found" + id, CatErrorCode.CAT_NOT_FOUND);
    }

    public static CatException categoryNotFound(UUID id) {
        return new CatException("Category not found with id: " + id, CatErrorCode.CATEGORY_NOT_FOUND);
    }

    public static CatException categoryRequired() {
        return new CatException("Category ID is required", CatErrorCode.CATEGORY_REQUIRED);
    }

    public static CatException categoriesError(Throwable cause) {
        return new CatException("Categories error: " + cause.getMessage(), CatErrorCode.CATEGORY_LIST_ERROR, cause);
    }


    public static CatException catSaveError(String message, Throwable cause) {
        return new CatException("Error saving cat: " + message, CatErrorCode.CAT_SAVE_ERROR, cause);
    }

    @Getter
    public enum CatErrorCode {
        CAT_NOT_FOUND("CAT_001", HttpStatus.NOT_FOUND),
        CATEGORY_NOT_FOUND("CAT_002", HttpStatus.NOT_FOUND),
        CAT_MEDIA_NOT_FOUND("CAT_003", HttpStatus.NOT_FOUND),
        CATEGORY_REQUIRED("CAT_004", HttpStatus.BAD_REQUEST),
        DUPLICATE_CAT("CAT_005", HttpStatus.CONFLICT),
        INVALID_CAT_DATA("CAT_006", HttpStatus.BAD_REQUEST),
        CAT_SAVE_ERROR("CAT_007", HttpStatus.INTERNAL_SERVER_ERROR),
        CAT_DELETE_ERROR("CAT_008", HttpStatus.INTERNAL_SERVER_ERROR),
        CATEGORY_LIST_ERROR("CAT__009", HttpStatus.INTERNAL_SERVER_ERROR);


        private final String code;
        private final HttpStatus status;

        CatErrorCode(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }

    }

}