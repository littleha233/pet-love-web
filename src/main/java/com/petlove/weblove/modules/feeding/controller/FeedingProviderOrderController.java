package com.petlove.weblove.modules.feeding.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.feeding.dto.provider.CancelProviderOrderRequest;
import com.petlove.weblove.modules.feeding.dto.provider.ProviderOrderListItemDTO;
import com.petlove.weblove.modules.feeding.dto.provider.ProviderRespondOrderRequest;
import com.petlove.weblove.modules.feeding.dto.provider.SubmitVisitLogRequest;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderDetailDTO;
import com.petlove.weblove.modules.feeding.service.FeedingProviderOrderService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feeding/provider-orders")
public class FeedingProviderOrderController {

    private final FeedingProviderOrderService feedingProviderOrderService;

    public FeedingProviderOrderController(FeedingProviderOrderService feedingProviderOrderService) {
        this.feedingProviderOrderService = feedingProviderOrderService;
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<ProviderOrderListItemDTO>> myOrders(@RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "20") int pageSize,
                                                                         @RequestParam(required = false) String status) {
        return ApiResponse.success(feedingProviderOrderService.myOrders(page, pageSize, status));
    }

    @PostMapping("/{orderId}/respond")
    public ApiResponse<FeedingOrderDetailDTO> respond(@PathVariable Long orderId,
                                                       @Valid @RequestBody ProviderRespondOrderRequest request) {
        return ApiResponse.success(feedingProviderOrderService.respond(orderId, request));
    }

    @PostMapping("/visits/{visitId}/start")
    public ApiResponse<FeedingOrderDetailDTO> startVisit(@PathVariable Long visitId) {
        return ApiResponse.success(feedingProviderOrderService.startVisit(visitId));
    }

    @PostMapping("/visits/{visitId}/submit-log")
    public ApiResponse<FeedingOrderDetailDTO> submitVisitLog(@PathVariable Long visitId,
                                                              @Valid @RequestBody SubmitVisitLogRequest request) {
        return ApiResponse.success(feedingProviderOrderService.submitVisitLog(visitId, request));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<FeedingOrderDetailDTO> cancel(@PathVariable Long orderId,
                                                      @Valid @RequestBody CancelProviderOrderRequest request) {
        return ApiResponse.success(feedingProviderOrderService.cancelByProvider(orderId, request));
    }
}
