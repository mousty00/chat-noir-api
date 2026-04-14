package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatCategoryDTO;
import com.mousty00.chat_noir_api.dto.cat.CreateCategoryRequestDTO;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.util.generic.GenericService;
import com.mousty00.chat_noir_api.mapper.CatCategoryMapper;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CatCategoryService extends GenericService<CatCategory, CatCategoryDTO, CatCategoryRepository, CatCategoryMapper> {

    public CatCategoryService(CatCategoryRepository repo, CatCategoryMapper mapper) {
        super(repo, mapper);
    }

    @Transactional
    public ApiResponse<List<CatCategoryDTO>> getCategories() {
        try {
            List<CatCategoryDTO> categories = repo.findAll().stream().map(mapper::toDTO).toList();
            return ApiResponse.success("", categories);
        } catch (Exception e) {
            throw CatException.categoriesError(e);
        }
    }

    @Transactional
    public ApiResponse<CatCategoryDTO> createCategory(CreateCategoryRequestDTO request) {
        try {
            CatCategory entity = CatCategory.builder()
                    .name(request.name())
                    .mediaTypeHint(request.mediaTypeHint() != null ? request.mediaTypeHint() : "")
                    .build();
            CatCategory saved = repo.save(entity);
            return ApiResponse.success("Category created successfully", mapper.toDTO(saved));
        } catch (Exception e) {
            throw CatException.categoriesError(e);
        }
    }

    @Transactional
    public ApiResponse<CatCategoryDTO> updateCategory(UUID id, CreateCategoryRequestDTO request) {
        try {
            CatCategory existing = repo.findById(id)
                    .orElseThrow(() -> CatException.categoryNotFound(id));
            existing.setName(request.name());
            existing.setMediaTypeHint(request.mediaTypeHint() != null ? request.mediaTypeHint() : "");
            CatCategory saved = repo.save(existing);
            return ApiResponse.success("Category updated successfully", mapper.toDTO(saved));
        } catch (CatException e) {
            throw e;
        } catch (Exception e) {
            throw CatException.categoriesError(e);
        }
    }

    @Transactional
    public ApiResponse<CatCategoryDTO> deleteCategory(UUID id) {
        try {
            CatCategory existing = repo.findById(id)
                    .orElseThrow(() -> CatException.categoryNotFound(id));
            repo.deleteById(id);
            return ApiResponse.success("Category deleted successfully", mapper.toDTO(existing));
        } catch (CatException e) {
            throw e;
        } catch (Exception e) {
            throw CatException.categoriesError(e);
        }
    }
}
