package com.mousty00.chat_noir_api.controller.graphql;

import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.cat.CatMediaStreamInfo;
import com.mousty00.chat_noir_api.service.CatMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class CatMediaController {

    private final CatMediaService service;

    @QueryMapping
    public ApiResponse<CatMediaStreamInfo> catMediaDownloadInfo(@Argument UUID id) {
        return service.getCatMediaStreamInfo(id);
    }
}
