package com.petlove.weblove.modules.adoption.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.adoption.dto.user.CreateRehomePostRequest;
import com.petlove.weblove.modules.adoption.dto.user.MyRehomePostListItemDTO;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostDetailDTO;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostListItemDTO;
import com.petlove.weblove.modules.adoption.service.AdoptionPostService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/adoptions")
public class AdoptionPostController {

    private final AdoptionPostService adoptionPostService;

    public AdoptionPostController(AdoptionPostService adoptionPostService) {
        this.adoptionPostService = adoptionPostService;
    }

    @GetMapping("/posts")
    public ApiResponse<PageResponse<RehomePostListItemDTO>> list(@RequestParam(defaultValue = "1") int page,
                                                                  @RequestParam(defaultValue = "20") int pageSize,
                                                                  @RequestParam(required = false) String cityCode,
                                                                  @RequestParam(required = false) String petType,
                                                                  @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adoptionPostService.listPublishedPosts(page, pageSize, cityCode, petType, keyword));
    }

    @GetMapping("/posts/{postId}")
    public ApiResponse<RehomePostDetailDTO> detail(@PathVariable Long postId) {
        return ApiResponse.success(adoptionPostService.detail(postId));
    }

    @PostMapping("/posts")
    public ApiResponse<RehomePostDetailDTO> create(@Valid @RequestBody CreateRehomePostRequest request) {
        return ApiResponse.success(adoptionPostService.create(request));
    }

    @GetMapping("/my/posts")
    public ApiResponse<PageResponse<MyRehomePostListItemDTO>> myPosts(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                                       @RequestParam(required = false) String status) {
        return ApiResponse.success(adoptionPostService.myPosts(page, pageSize, status));
    }

    @PostMapping("/posts/{postId}/resubmit")
    public ApiResponse<RehomePostDetailDTO> resubmit(@PathVariable Long postId) {
        return ApiResponse.success(adoptionPostService.resubmit(postId));
    }

    @PostMapping("/posts/{postId}/close")
    public ApiResponse<RehomePostDetailDTO> close(@PathVariable Long postId) {
        return ApiResponse.success(adoptionPostService.close(postId));
    }
}
