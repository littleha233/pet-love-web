package com.petlove.weblove.modules.feeding.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.feeding.dto.provider.UpsertFeedingProviderProfileRequest;
import com.petlove.weblove.modules.feeding.dto.user.FeedingProviderDetailDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingProviderListItemDTO;
import com.petlove.weblove.modules.feeding.service.FeedingProviderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feeding/providers")
public class FeedingProviderController {

    private final FeedingProviderService feedingProviderService;

    public FeedingProviderController(FeedingProviderService feedingProviderService) {
        this.feedingProviderService = feedingProviderService;
    }

    @GetMapping
    public ApiResponse<PageResponse<FeedingProviderListItemDTO>> list(@RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "20") int pageSize,
                                                                       @RequestParam(required = false) String cityCode,
                                                                       @RequestParam(required = false) String petType,
                                                                       @RequestParam(required = false) String keyword,
                                                                       @RequestParam(required = false) String sortBy) {
        return ApiResponse.success(feedingProviderService.list(page, pageSize, cityCode, petType, keyword, sortBy));
    }

    @GetMapping("/{providerUserId}")
    public ApiResponse<FeedingProviderDetailDTO> detail(@PathVariable Long providerUserId) {
        return ApiResponse.success(feedingProviderService.detail(providerUserId));
    }

    @GetMapping("/me/profile")
    public ApiResponse<FeedingProviderDetailDTO> myProfile() {
        return ApiResponse.success(feedingProviderService.myProfile());
    }

    @PutMapping("/me/profile")
    public ApiResponse<FeedingProviderDetailDTO> upsert(@Valid @RequestBody UpsertFeedingProviderProfileRequest request) {
        return ApiResponse.success(feedingProviderService.upsertMyProfile(request));
    }
}
