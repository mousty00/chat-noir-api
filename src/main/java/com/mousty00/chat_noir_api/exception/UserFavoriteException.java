package com.mousty00.chat_noir_api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserFavoriteException extends ApiException {

    public UserFavoriteException(String message, FavoriteErrorCode errorCode) {
        super(message, errorCode.getCode(), errorCode.getStatus());
    }

    public UserFavoriteException(String message, FavoriteErrorCode errorCode, Throwable cause) {
        super(message, errorCode.getCode(), errorCode.getStatus(), cause);
    }

    public static UserFavoriteException favoriteNotFound(UUID catMediaId) {
        return new UserFavoriteException("Favorite not found for catMediaId: " + catMediaId, FavoriteErrorCode.FAVORITE_NOT_FOUND);
    }

    public static UserFavoriteException alreadyFavorited(UUID catMediaId) {
        return new UserFavoriteException("CatMedia already in favorites: " + catMediaId, FavoriteErrorCode.ALREADY_FAVORITED);
    }

    public static UserFavoriteException favoriteSaveError(Throwable cause) {
        return new UserFavoriteException("Error saving favorite: " + cause.getMessage(), FavoriteErrorCode.FAVORITE_SAVE_ERROR, cause);
    }

    public static UserFavoriteException favoriteDeleteError(Throwable cause) {
        return new UserFavoriteException("Error deleting favorite: " + cause.getMessage(), FavoriteErrorCode.FAVORITE_DELETE_ERROR, cause);
    }

    public static UserFavoriteException favoriteNotFoundForCat(UUID catId) {
        return new UserFavoriteException("Favorite not found for cat: " + catId,  FavoriteErrorCode.FAVORITE_NOT_FOUND);
    }

    public static UserFavoriteException alreadyFavoritedCat(UUID catId) {
        return new UserFavoriteException("Cat already favorited: " + catId,  FavoriteErrorCode.ALREADY_FAVORITED);
    }

    @Getter
    public enum FavoriteErrorCode {
        FAVORITE_NOT_FOUND("FAV_001", HttpStatus.NOT_FOUND),
        ALREADY_FAVORITED("FAV_002", HttpStatus.CONFLICT),
        FAVORITE_SAVE_ERROR("FAV_003", HttpStatus.INTERNAL_SERVER_ERROR),
        FAVORITE_DELETE_ERROR("FAV_004", HttpStatus.INTERNAL_SERVER_ERROR);

        private final String code;
        private final HttpStatus status;

        FavoriteErrorCode(String code, HttpStatus status) {
            this.code = code;
            this.status = status;
        }
    }
}
