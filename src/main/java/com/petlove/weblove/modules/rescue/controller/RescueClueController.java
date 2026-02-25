package com.petlove.weblove.modules.rescue.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.rescue.dto.user.MyRescueClueQuery;
import com.petlove.weblove.modules.rescue.dto.user.RescueClueDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueClueListItemDTO;
import com.petlove.weblove.modules.rescue.dto.user.SubmitRescueClueRequest;
import com.petlove.weblove.modules.rescue.service.RescueClueService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rescue/clues")
public class RescueClueController {

    private final RescueClueService rescueClueService;

    public RescueClueController(RescueClueService rescueClueService) {
        this.rescueClueService = rescueClueService;
    }

    @PostMapping
    public ApiResponse<RescueClueDetailDTO> submit(@Valid @RequestBody SubmitRescueClueRequest request) {
        return ApiResponse.success(rescueClueService.submit(request));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<RescueClueListItemDTO>> myClues(MyRescueClueQuery query) {
        return ApiResponse.success(rescueClueService.myClues(query));
    }

    @GetMapping("/{clueId}")
    public ApiResponse<RescueClueDetailDTO> myDetail(@PathVariable Long clueId) {
        return ApiResponse.success(rescueClueService.myDetail(clueId));
    }
}
