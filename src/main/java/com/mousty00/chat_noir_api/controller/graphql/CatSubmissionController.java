package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.cat.CatSubmissionDTO;
import com.mousty00.chat_noir_api.dto.cat.CatSubmissionRequestDTO;
import com.mousty00.chat_noir_api.service.CatSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CatSubmissionController {

    private final CatSubmissionService service;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaginatedResponse<CatSubmissionDTO>> mySubmissions(
            @Argument Integer page,
            @Argument Integer size
    ) {
        return service.getMySubmissions(page, size);
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaginatedResponse<CatSubmissionDTO>> pendingSubmissions(
            @Argument Integer page,
            @Argument Integer size
    ) {
        return service.getPendingSubmissions(page, size);
    }

    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<CatSubmissionDTO> submitCat(@Argument CatSubmissionRequestDTO submission) {
        return service.submitCat(submission);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatSubmissionDTO> approveSubmission(@Argument UUID id) {
        return service.approveSubmission(id);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<CatSubmissionDTO> rejectSubmission(@Argument UUID id, @Argument String reason) {
        return service.rejectSubmission(id, reason);
    }
}
