package com.petlove.weblove.modules.ops.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.ops.dto.admin.AdminBlacklistListItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminBlacklistQuery;
import com.petlove.weblove.modules.ops.dto.admin.UpdateBlacklistEntryStatusRequest;
import com.petlove.weblove.modules.ops.dto.admin.UpsertBlacklistEntryRequest;
import com.petlove.weblove.modules.ops.service.RiskBlacklistService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/ops/risk/blacklists")
public class AdminRiskController {

    private final RiskBlacklistService riskBlacklistService;

    public AdminRiskController(RiskBlacklistService riskBlacklistService) {
        this.riskBlacklistService = riskBlacklistService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminBlacklistListItemDTO>> list(AdminBlacklistQuery query) {
        return ApiResponse.success(riskBlacklistService.list(query));
    }

    @PutMapping("/{blacklistId}")
    public ApiResponse<AdminBlacklistListItemDTO> upsert(@PathVariable Long blacklistId,
                                                          @Valid @RequestBody UpsertBlacklistEntryRequest request) {
        return ApiResponse.success(riskBlacklistService.upsert(blacklistId, request));
    }

    @PostMapping("/{blacklistId}/status")
    public ApiResponse<AdminBlacklistListItemDTO> updateStatus(@PathVariable Long blacklistId,
                                                                @Valid @RequestBody UpdateBlacklistEntryStatusRequest request) {
        return ApiResponse.success(riskBlacklistService.updateStatus(blacklistId, request));
    }
}
