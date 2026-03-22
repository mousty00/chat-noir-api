package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.service.CatSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/submissions")
@PreAuthorize("isAuthenticated()")
public class CatSubmissionRestController {

    private final CatSubmissionService service;

    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadMedia(
            @PathVariable UUID id,
            @RequestParam MultipartFile mediaFile
    ) {
        return service.uploadSubmissionMedia(id, mediaFile);
    }
}
