package com.mousty00.chat_noir_api.service;

import com.mousty00.chat_noir_api.aws.S3Service;
import com.mousty00.chat_noir_api.dto.api.ApiResponse;
import com.mousty00.chat_noir_api.dto.api.PaginatedResponse;
import com.mousty00.chat_noir_api.dto.user.UserDTO;
import com.mousty00.chat_noir_api.entity.User;
import com.mousty00.chat_noir_api.exception.ResourceNotFoundException;
import com.mousty00.chat_noir_api.exception.UserException;
import com.mousty00.chat_noir_api.generic.GenericService;
import com.mousty00.chat_noir_api.mapper.UserMapper;
import com.mousty00.chat_noir_api.repository.UserRepository;
import com.mousty00.chat_noir_api.specification.UserSpecifications;
import com.mousty00.chat_noir_api.util.PageDefaults;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.mousty00.chat_noir_api.exception.ResourceNotFoundException.*;

@Service
public class UserService extends GenericService<User, UserDTO, UserRepository, UserMapper> {

    private final UserMapper userMapper;
    private final S3Service s3Service;

    public UserService(UserMapper mapper,
                       UserRepository repo,
                       UserMapper userMapper,
                       S3Service s3Service
    ) {
        super(repo, mapper);
        this.userMapper = userMapper;
        this.s3Service = s3Service;
    }

    public ApiResponse<PaginatedResponse<UserDTO>> getUsers(Integer page, Integer size, String username) {
        Pageable pageable = PageDefaults.of(page, size);
        Specification<User> spec = UserSpecifications.hasUsername(username);
        Page<UserDTO> pageResult = repo.findAll(spec, pageable).map(userMapper::toDTO);

        /* TODO: generate / retrieve / map profile image
          List<UserDTO> users = pageResult.getContent();
          users.forEach((user -> {

          }));

         */

        return buildSuccessPageResponse(pageResult, "Users retrieved successfully");
    }

    public ApiResponse<UserDTO> getUserById(UUID id) {
        try {
            return getItemById(id, ResourceType.USER);
        } catch (Exception e) {
            throw UserException.userNotFound(id);
        }
    }
}
