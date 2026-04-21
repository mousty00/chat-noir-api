package com.mousty00.chat_noir_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class FriendshipException extends ApiException {

    public FriendshipException(String message, FriendshipErrorCode errorCode) {
        super(message, errorCode.getCode(), errorCode.getStatus());
    }

    public static FriendshipException notFound(UUID id) {
        return new FriendshipException("Friendship not found: " + id, FriendshipErrorCode.NOT_FOUND);
    }

    public static FriendshipException alreadyExists() {
        return new FriendshipException("Friendship request already exists", FriendshipErrorCode.ALREADY_EXISTS);
    }

    public static FriendshipException selfRequest() {
        return new FriendshipException("Cannot send friend request to yourself", FriendshipErrorCode.SELF_REQUEST);
    }

    public static FriendshipException notAddresseeOrRequester() {
        return new FriendshipException("You are not part of this friendship", FriendshipErrorCode.FORBIDDEN);
    }

    public static FriendshipException notFriends(UUID targetId) {
        return new FriendshipException("You are not friends with user: " + targetId, FriendshipErrorCode.NOT_FRIENDS);
    }

    @Getter
    public enum FriendshipErrorCode {
        NOT_FOUND("FRD_001", HttpStatus.NOT_FOUND),
        ALREADY_EXISTS("FRD_002", HttpStatus.CONFLICT),
        SELF_REQUEST("FRD_003", HttpStatus.BAD_REQUEST),
        FORBIDDEN("FRD_004", HttpStatus.FORBIDDEN),
        NOT_FRIENDS("FRD_005", HttpStatus.FORBIDDEN);

        private final String code;
        private final HttpStatus status;

        FriendshipErrorCode(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }
    }
}
