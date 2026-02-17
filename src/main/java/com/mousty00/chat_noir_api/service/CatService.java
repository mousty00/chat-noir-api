package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatFilterDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.entity.CatCategory;
import com.mousty00.chat_noir_api.exception.CatException;
import com.mousty00.chat_noir_api.exception.ResourceNotFoundException.ResourceType;
import com.mousty00.chat_noir_api.generic.GenericService;
import com.mousty00.chat_noir_api.mapper.CatMapper;
import com.mousty00.chat_noir_api.repository.CatCategoryRepository;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.specification.CatSpecifications;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

import static com.mousty00.chat_noir_api.exception.CatException.*;

@Service
public class CatService extends GenericService<Cat, CatDTO, CatRepository, CatMapper> {

    private static final Logger log = LoggerFactory.getLogger(CatService.class);
    private final CatCategoryRepository catCategoryRepository;
    private final S3Service s3Service;

    public CatService(
            CatRepository repo,
            CatMapper mapper,
            CatCategoryRepository catCategoryRepository,
            S3Service s3Service) {
        super(repo, mapper);
        this.catCategoryRepository = catCategoryRepository;
        this.s3Service = s3Service;
    }

    public ApiResponse<PaginatedResponse<CatDTO>> getCats(Integer page, Integer size, CatFilterDTO filterDTO) {
        Pageable pageable = PageDefaults.of(page, size);
        Specification<Cat> spec = CatSpecifications.filter(filterDTO);
        Page<CatDTO> pageResult = repo.findAll(spec, pageable).map(mapper::toDTO);

        return buildSuccessPageResponse(pageResult, "Cats retrieved successfully");
    }

    public ApiResponse<CatDTO> getCatById(UUID id) {
        try {
            return getItemById(id, ResourceType.CAT);
        } catch (Exception e) {
            throw catNotFound(id);
        }
    }

    @Transactional
    public ApiResponse<?> deleteCat(UUID id) {
        try {
            return deleteItemById(id);
        } catch (Exception e) {
            log.error("Error deleting cat with id: {}", id, e);
            throw catDeleteError(e);
        }
    }

    @Transactional
    public ApiResponse<CatDTO> saveCat(CatRequestDTO request) {
        try {
            if (request.getCategoryId() == null) {
                throw categoryRequired();
            }

            CatCategory category = catCategoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> categoryNotFound(request.getCategoryId()));

            Cat cat = mapper.toEntityFromRequest(request);
            cat.setCategory(category);
            Cat savedCat = repo.save(cat);

            log.info("Cat saved successfully with ID: {}, category: {}",
                    savedCat.getId(), savedCat.getCategory().getName());

            return ApiResponse.success(
                    HttpStatus.OK.value(),
                    "Cat saved successfully",
                    mapper.toDTO(savedCat)
            );

        } catch (CatException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error saving cat: {}", e.getMessage(), e);
            throw catSaveError(e.getMessage(), e);
        }
    }

    @Transactional
    public ApiResponse<CatDTO> updateCat(UUID id, CatDTO dto) {
        if (!repo.existsById(id)) {
            throw catNotFound(id);
        }
        dto.setId(id);
        return saveItem(dto);
    }

    public ApiResponse<String> downloadCatMedia(UUID id) {
        Cat cat = repo.findById(id).orElseThrow(() -> catNotFound(id));

        if (cat.getMedia() == null || cat.getMedia().getMediaKey() == null) {
            throw CatException.catMediaNotFound(id);
        }

        String downloadUrl = s3Service.generatePresignedUrl(
                cat.getMedia().getMediaKey(),
                Duration.ofMinutes(15)
        );

        return ApiResponse.success("Cat media ready to be downloaded!", downloadUrl);
    }
}