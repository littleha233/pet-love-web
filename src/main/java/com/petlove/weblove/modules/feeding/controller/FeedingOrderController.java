package com.petlove.weblove.modules.feeding.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.feeding.dto.user.ConfirmFeedingOrderCompleteRequest;
import com.petlove.weblove.modules.feeding.dto.user.CreateFeedingOrderRequest;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderDetailDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOwnedPetOptionDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingReviewDTO;
import com.petlove.weblove.modules.feeding.dto.user.MyFeedingOrderListItemDTO;
import com.petlove.weblove.modules.feeding.dto.user.SubmitFeedingReviewRequest;
import com.petlove.weblove.modules.feeding.service.FeedingOrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feeding/orders")
public class FeedingOrderController {

    private final FeedingOrderService feedingOrderService;

    public FeedingOrderController(FeedingOrderService feedingOrderService) {
        this.feedingOrderService = feedingOrderService;
    }

    @PostMapping
    public ApiResponse<FeedingOrderDetailDTO> create(@Valid @RequestBody CreateFeedingOrderRequest request) {
        return ApiResponse.success(feedingOrderService.create(request));
    }

    @GetMapping("/pets/options")
    public ApiResponse<List<FeedingOwnedPetOptionDTO>> myPetOptions() {
        return ApiResponse.success(feedingOrderService.myPetOptions());
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<MyFeedingOrderListItemDTO>> myOrders(@RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "20") int pageSize,
                                                                          @RequestParam(required = false) String status) {
        return ApiResponse.success(feedingOrderService.myOrders(page, pageSize, status));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<FeedingOrderDetailDTO> detail(@PathVariable Long orderId) {
        return ApiResponse.success(feedingOrderService.detail(orderId));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<FeedingOrderDetailDTO> cancel(@PathVariable Long orderId) {
        return ApiResponse.success(feedingOrderService.cancelByOwner(orderId));
    }

    @PostMapping("/{orderId}/confirm-complete")
    public ApiResponse<FeedingOrderDetailDTO> confirmComplete(@PathVariable Long orderId,
                                                               @Valid @RequestBody(required = false) ConfirmFeedingOrderCompleteRequest request) {
        ConfirmFeedingOrderCompleteRequest body = request == null ? new ConfirmFeedingOrderCompleteRequest() : request;
        return ApiResponse.success(feedingOrderService.confirmComplete(orderId, body));
    }

    @PostMapping("/{orderId}/review")
    public ApiResponse<FeedingReviewDTO> review(@PathVariable Long orderId,
                                                 @Valid @RequestBody SubmitFeedingReviewRequest request) {
        return ApiResponse.success(feedingOrderService.submitReview(orderId, request));
    }
}
