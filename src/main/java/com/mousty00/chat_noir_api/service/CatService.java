package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.cat.CatDTO;
import com.mousty00.chat_noir_api.dto.cat.CatRequestDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.mapper.CatMapper;
import com.mousty00.chat_noir_api.pagination.PaginatedResponse;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CatService extends BaseService<Cat, CatDTO, CatRepository, CatMapper> {

    public CatService(CatRepository repo, CatMapper mapper) {
        super(repo, mapper);
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
        Cat cat = mapper.toEntityFromRequest(request);
        CatDTO catDTO = mapper.toDTO(cat);
        return ApiResponse.<CatDTO>builder()
                .status(HttpStatus.OK.value())
                .message("Cat saved!")
                .success(true)
                .data(catDTO)
                .error(false)
                .build();
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
