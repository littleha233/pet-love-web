package com.petlove.weblove.modules.ops.controller;

import com.petlove.weblove.common.api.ApiResponse;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.modules.ops.dto.user.ComplaintTicketDetailDTO;
import com.petlove.weblove.modules.ops.dto.user.ComplaintTicketListItemDTO;
import com.petlove.weblove.modules.ops.dto.user.MyComplaintTicketQuery;
import com.petlove.weblove.modules.ops.dto.user.ReplyComplaintTicketRequest;
import com.petlove.weblove.modules.ops.dto.user.SubmitComplaintTicketRequest;
import com.petlove.weblove.modules.ops.service.ComplaintTicketService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/support/complaints")
public class ComplaintTicketController {

    private final ComplaintTicketService complaintTicketService;

    public ComplaintTicketController(ComplaintTicketService complaintTicketService) {
        this.complaintTicketService = complaintTicketService;
    }

    @PostMapping
    public ApiResponse<ComplaintTicketDetailDTO> submit(@Valid @RequestBody SubmitComplaintTicketRequest request) {
        return ApiResponse.success(complaintTicketService.submit(request));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<ComplaintTicketListItemDTO>> myTickets(MyComplaintTicketQuery query) {
        return ApiResponse.success(complaintTicketService.myTickets(query));
    }

    @GetMapping("/{ticketId}")
    public ApiResponse<ComplaintTicketDetailDTO> myDetail(@PathVariable Long ticketId) {
        return ApiResponse.success(complaintTicketService.myDetail(ticketId));
    }

    @PostMapping("/{ticketId}/reply")
    public ApiResponse<ComplaintTicketDetailDTO> reply(@PathVariable Long ticketId,
                                                        @Valid @RequestBody ReplyComplaintTicketRequest request) {
        return ApiResponse.success(complaintTicketService.reply(ticketId, request));
    }

    @PostMapping("/{ticketId}/cancel")
    public ApiResponse<ComplaintTicketDetailDTO> cancel(@PathVariable Long ticketId) {
        return ApiResponse.success(complaintTicketService.cancel(ticketId));
    }
}
