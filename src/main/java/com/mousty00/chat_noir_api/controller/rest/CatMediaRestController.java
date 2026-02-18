package com.mousty00.chat_noir_api.controller.rest;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatMediaStreamInfo;
import com.mousty00.chat_noir_api.service.CatMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cats")
public class CatMediaRestController {

    private final CatMediaService catMediaService;

    @PostMapping(value = "/{id}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadMedia(@PathVariable UUID id, @RequestParam MultipartFile mediaFile) {
        return catMediaService.uploadMediaWithCleanup(id, mediaFile);
    }

    @GetMapping("/{id}/media/info")
    public ApiResponse<CatMediaStreamInfo> getMediaStreamInfo(@PathVariable("id") UUID catId) {
        return catMediaService.getCatMediaStreamInfo(catId);
    }

    @GetMapping("/{id}/media/stream")
    public ResponseEntity<StreamingResponseBody> streamCatMedia(@PathVariable("id") UUID catId) {
        return catMediaService.streamCatMedia(catId);
    }
}