package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.enums.EPAGE;
import com.mousty00.chat_noir_api.mapper.BaseMapper;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseService<ENTITY, DTO, REPO extends JpaRepository<ENTITY, UUID>, MAPPER extends BaseMapper<ENTITY, DTO>> {

    protected final REPO repo;
    protected final MAPPER mapper;

    /**
     * Fetches a paged list of DTOs.
     *
     * @param pageable The pagination info (usually passed from the Controller)
     */
    public ApiResponse<PaginatedResponse<DTO>> getPagedItems(Pageable pageable) {

        Pageable request = (pageable != null) ? pageable : PageRequest.of(0, EPAGE.DEFAULT_SIZE.size);

        Page<DTO> page = repo.findAll(request).map(mapper::toDTO);
        PaginatedResponse<DTO> data = buildPaginatedResponse(page);

        return ApiResponse.<PaginatedResponse<DTO>>builder()
                .status(HttpStatus.OK.value())
                .message("")
                .success(true)
                .error(false)
                .data(data)
                .build();

    }

    public ApiResponse<DTO> getItemById(UUID id) {
        Result<DTO> result = getDtoResult(id);

        return ApiResponse.<DTO>builder()
                .status(result.status().value())
                .message(result.message())
                .success(result.isPresent())
                .error(!result.isPresent())
                .data(result.data())
                .build();
    }

    @Transactional
    public ApiResponse<DTO> saveItem(DTO dto) {

        ENTITY entity = mapper.toEntity(dto);
        ENTITY saved = this.repo.save(entity);
        DTO result = mapper.toDTO(saved);

        return ApiResponse.<DTO>builder()
                .status(HttpStatus.OK.value())
                .message("saved successfully")
                .success(true)
                .error(false)
                .data(result)
                .build();
    }

    @Transactional
    public ApiResponse<?> deleteItemById(UUID id) {
        repo.deleteById(id);

        return ApiResponse.<DTO>builder()
                .status(HttpStatus.OK.value())
                .message("cat deleted successfully")
                .success(true)
                .error(false)
                .data(null)
                .build();
    }

    // ---------- UTILS ---------- //

    public <T> PaginatedResponse<T> buildPaginatedResponse(Page<T> page) {
        PaginatedResponse<T> response = new PaginatedResponse<>();
        response.setResult(page.getContent());
        response.setCurrentPage(page.getNumber());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setPageSize(page.getSize());
        response.setHasNext(page.hasNext());
        response.setHasPrevious(page.hasPrevious());
        return response;
    }

    private @NonNull Result<DTO> getDtoResult(UUID id) {
        return repo.findById(id)
                .map(entity -> new Result<>(mapper.toDTO(entity), true, HttpStatus.OK, ""))
                .orElse(new Result<>(null, false, HttpStatus.NOT_FOUND,"not found"));
    }

    private record Result<DTO>(DTO data, boolean isPresent, HttpStatus status, String message) { }

}