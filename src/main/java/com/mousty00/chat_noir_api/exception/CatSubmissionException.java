package com.mousty00.chat_noir_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class CatSubmissionException extends ApiException {

    public CatSubmissionException(String message, SubmissionErrorCode errorCode) {
        super(message, errorCode.getCode(), errorCode.getStatus());
    }

    public CatSubmissionException(String message, SubmissionErrorCode errorCode, Throwable cause) {
        super(message, errorCode.getCode(), errorCode.getStatus(), cause);
    }

    public static CatSubmissionException submissionNotFound(UUID id) {
        return new CatSubmissionException("Submission not found: " + id, SubmissionErrorCode.SUBMISSION_NOT_FOUND);
    }

    public static CatSubmissionException dailyLimitReached() {
        return new CatSubmissionException("Daily submission limit of 3 reached. Try again tomorrow.", SubmissionErrorCode.DAILY_LIMIT_REACHED);
    }

    public static CatSubmissionException submissionSaveError(Throwable cause) {
        return new CatSubmissionException("Error saving submission: " + cause.getMessage(), SubmissionErrorCode.SUBMISSION_SAVE_ERROR, cause);
    }

    public static CatSubmissionException alreadyReviewed(UUID id) {
        return new CatSubmissionException("Submission already reviewed: " + id, SubmissionErrorCode.ALREADY_REVIEWED);
    }

    @Getter
    public enum SubmissionErrorCode {
        SUBMISSION_NOT_FOUND("SUB_001", HttpStatus.NOT_FOUND),
        DAILY_LIMIT_REACHED("SUB_002", HttpStatus.TOO_MANY_REQUESTS),
        SUBMISSION_SAVE_ERROR("SUB_003", HttpStatus.INTERNAL_SERVER_ERROR),
        ALREADY_REVIEWED("SUB_004", HttpStatus.CONFLICT);

        private final String code;
        private final HttpStatus status;

        SubmissionErrorCode(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }
    }
}
