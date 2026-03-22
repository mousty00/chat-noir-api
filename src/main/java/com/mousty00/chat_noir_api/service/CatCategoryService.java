package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.ResourceNotFoundException.ResourceType;
import com.mousty00.chat_noir_api.util.generic.GenericService;
import com.mousty00.chat_noir_api.mapper.CatCategoryMapper;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CatCategoryService extends GenericService<CatCategory, CatCategoryDTO, CatCategoryRepository, CatCategoryMapper> {

    public CatCategoryService(CatCategoryRepository repo, CatCategoryMapper mapper) {
        super(repo, mapper);
    }

    @Transactional
    @Cacheable("categories")
    public ApiResponse<List<CatCategoryDTO>> getCategories() {
        try {
            List<CatCategoryDTO> categories = repo.findAll().stream().map(mapper::toDTO).toList();
            return ApiResponse.success("", categories);
        } catch (Exception e) {
            throw CatException.categoriesError(e);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public ApiResponse<CatCategoryDTO> saveItem(CatCategoryDTO dto) {
        return super.saveItem(dto);
    }

    @Override
    @Transactional
    @CacheEvict(value = "categories", allEntries = true)
    public ApiResponse<?> deleteItemById(UUID id) {
        return super.deleteItemById(id);
    }

}
