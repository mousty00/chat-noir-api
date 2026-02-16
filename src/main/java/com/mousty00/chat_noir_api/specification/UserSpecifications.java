package com.mousty00.chat_noir_api.specification;

import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecifications {

    private UserSpecifications(){}

    public static Specification<User> hasUsername(String username) {
        return hasTextLike(username);
    }

    public static Specification<User> filter(UserDTO user) {
        return Specification
                .where(hasTextLike(user.getUsername()))
                .and(hasTextLike(user.getEmail()));
    }

    private static Specification<User> hasTextLike(String text) {
        return (root, _, cb) -> {
            if (!StringUtils.hasText(text)) {
                return null;
            }

            return cb.like(cb.lower(root.get("username")), "%" + text.toLowerCase() + "%");
        };
    }
}
