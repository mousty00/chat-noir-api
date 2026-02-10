package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.dto.CatDTO;
import com.mousty00.chat_noir_api.entity.Cat;
import com.mousty00.chat_noir_api.mapper.CatMapper;
import com.mousty00.chat_noir_api.pagination.PaginatedResponse;
import com.mousty00.chat_noir_api.repository.CatRepository;
import com.mousty00.chat_noir_api.response.ApiResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class CatService extends BaseService<Cat, CatDTO, CatRepository, CatMapper> {

    public CatService(CatRepository repository, CatMapper mapper) {
        super(repository, mapper);
    }

    public ApiResponse<PaginatedResponse<CatDTO>> getCats() {
        return getPagedItems(null);
    }

    public ApiResponse<CatDTO> getCatById(UUID id) {
        return getItemById(id);
    }

    @Transactional
    public ApiResponse<?> deleteCat(UUID id) {
        return deleteItemById(id);
    }

    @Transactional
    public ApiResponse<CatDTO> saveCat(CatDTO request) {
        return saveItem(request);
    }

    @Transactional
    public ApiResponse<CatDTO> updateCat(UUID id, CatDTO request) {
        if (!repo.existsById(id)) {
            return ApiResponse.<CatDTO>builder()
                    .statusCode(HttpStatus.NOT_FOUND)
                    .status(HttpStatus.NOT_FOUND.value())
                    .message("Cat not found")
                    .success(false)
                    .error(true)
                    .build();
        }
        request.setId(id);
        return saveItem(request);
    }

}
