package com.mousty00.chat_noir_api.specification;

import com.mousty00.chat_noir_api.dto.cat.CatFilterDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class CatSpecifications {

    private CatSpecifications() {
    }

    public static Specification<Cat> hasCategory(String category) {
        return (root, _, cb) -> {
            if (!StringUtils.hasText(category)) {
                return null;
            }
            return cb.like(cb.lower(root.get("category").get("name")), "%" + category.toLowerCase() + "%");
        };
    }

    public static Specification<Cat> hasColor(String color) {
        return (root, _, cb) -> {
            if (!StringUtils.hasText(color)) {
                return null;
            }
            return cb.like(cb.lower(root.get("color")), "%" + color.toLowerCase() + "%");
        };
    }

    public static Specification<Cat> hasNameContaining(String name) {
        return (root, _, cb) -> {
            if (!StringUtils.hasText(name)) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Cat> fromSource(String source) {
        return (root, _, cb) -> {
            if (!StringUtils.hasText(source)) {
                return null;
            }
            return cb.like(cb.lower(root.get("sourceName")), "%" + source.toLowerCase() + "%");
        };
    }

    public static Specification<Cat> filter(CatFilterDTO filterDTO) {
        return Specification
                .where(hasCategory(filterDTO.category()))
                .and(hasColor(filterDTO.color()))
                .and(hasNameContaining(filterDTO.name()))
                .and(fromSource(filterDTO.source()));
    }
}