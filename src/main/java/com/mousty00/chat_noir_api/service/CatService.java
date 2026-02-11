package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.mapper.CatMapper;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CatService extends BaseService<Cat, CatDTO, CatRepository, CatMapper> {

    private final Logger log = LoggerFactory.getLogger(CatService.class);
    private final CatCategoryRepository catCategoryRepository;

    public CatService(
            CatRepository repo,
            CatMapper mapper,
            CatCategoryRepository catCategoryRepository
    ) {
        super(repo, mapper);
        this.catCategoryRepository = catCategoryRepository;
    }

    public ApiResponse<PaginatedResponse<CatDTO>> getCats(Integer page, Integer size) {
        if (page == null || size == null) {
            return getPagedItems(null);
        }

        return getPagedItems(PageRequest.of(page, size));
    }

    public ApiResponse<CatDTO> getCatById(UUID id) {
        return getItemById(id);
    }

    @Transactional
    public ApiResponse<?> deleteCat(UUID id) {
        return deleteItemById(id);
    }

    @Transactional
    public ApiResponse<CatDTO> saveCat(CatRequestDTO request) {
        try {
            // Validate category exists
            if (request.getCategoryId() == null) {
                return ApiResponse.<CatDTO>builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message("Category ID is required")
                        .success(false)
                        .error(true)
                        .build();
            }

            CatCategory category = catCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

            Cat cat = mapper.toEntityFromRequest(request);
            cat.setCategory(category);
            Cat savedCat = repo.save(cat);

            log.info("Cat saved successfully with ID: {}, category: {}", savedCat.getId(), savedCat.getCategory().getName());

            CatDTO catDTO = mapper.toDTO(savedCat);

            return ApiResponse.<CatDTO>builder()
                    .status(HttpStatus.OK.value())
                    .message("Cat saved!")
                    .success(true)
                    .data(catDTO)
                    .error(false)
                    .build();

        } catch (Exception e) {
            log.error("Error saving cat: {}", e.getMessage(), e);
            return ApiResponse.<CatDTO>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Error saving cat: " + e.getMessage())
                    .success(false)
                    .error(true)
                    .build();
        }
    }

    @Transactional
    public ApiResponse<CatDTO> updateCat(UUID id, CatDTO dto) {
        if (!repo.existsById(id)) {
            return ApiResponse.<CatDTO>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Cat not found")
                    .success(false)
                    .error(true)
                    .build();
        }
        dto.setId(id);
        return saveItem(dto);
    }

}
