package com.petlove.weblove.modules.ops.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.admin.service.AdminAuditService;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintReplyDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintTicketDetailDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintTicketListItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminComplaintTicketQuery;
import com.petlove.weblove.modules.ops.dto.admin.AdminReplyComplaintTicketRequest;
import com.petlove.weblove.modules.ops.dto.admin.UpdateComplaintTicketStatusRequest;
import com.petlove.weblove.modules.ops.dto.user.ComplaintEvidencePhotoDTO;
import com.petlove.weblove.modules.ops.entity.ComplaintTicket;
import com.petlove.weblove.modules.ops.entity.ComplaintTicketReply;
import com.petlove.weblove.modules.ops.enums.ComplaintPriority;
import com.petlove.weblove.modules.ops.enums.ComplaintReplyAuthorType;
import com.petlove.weblove.modules.ops.enums.ComplaintTargetType;
import com.petlove.weblove.modules.ops.enums.ComplaintTicketStatus;
import com.petlove.weblove.modules.ops.repository.ComplaintTicketReplyRepository;
import com.petlove.weblove.modules.ops.repository.ComplaintTicketRepository;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminComplaintService {

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };

    private final ComplaintTicketRepository complaintTicketRepository;
    private final ComplaintTicketReplyRepository complaintTicketReplyRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdminUserRepository adminUserRepository;
    private final FileObjectRepository fileObjectRepository;
    private final AdminAuditService adminAuditService;
    private final ObjectMapper objectMapper;

    public AdminComplaintService(ComplaintTicketRepository complaintTicketRepository,
                                 ComplaintTicketReplyRepository complaintTicketReplyRepository,
                                 UserProfileRepository userProfileRepository,
                                 AdminUserRepository adminUserRepository,
                                 FileObjectRepository fileObjectRepository,
                                 AdminAuditService adminAuditService,
                                 ObjectMapper objectMapper) {
        this.complaintTicketRepository = complaintTicketRepository;
        this.complaintTicketReplyRepository = complaintTicketReplyRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminUserRepository = adminUserRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.adminAuditService = adminAuditService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public PageResponse<AdminComplaintTicketListItemDTO> list(AdminComplaintTicketQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        ComplaintTicketStatus status = parseStatus(query.getStatus(), false);
        ComplaintPriority priority = parsePriority(query.getPriority(), false);
        ComplaintTargetType targetType = parseTargetType(query.getTargetType(), false);
        Long assignedAdminId = query.getAssignedAdminId();
        String keyword = normalizeText(query.getKeyword());
        LocalDate dateFrom = query.getDateFrom();
        LocalDate dateTo = query.getDateTo();

        if (dateFrom != null && dateTo != null && dateTo.isBefore(dateFrom)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "dateTo must not be earlier than dateFrom");
        }

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<ComplaintTicket> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (targetType != null) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            if (assignedAdminId != null) {
                predicates.add(cb.equal(root.get("assignedAdminId"), assignedAdminId));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                List<Predicate> keywordPredicates = new ArrayList<>();
                keywordPredicates.add(cb.like(cb.lower(root.get("ticketNo")), like));
                keywordPredicates.add(cb.like(cb.lower(root.get("title")), like));
                keywordPredicates.add(cb.like(cb.lower(root.get("content")), like));
                if (keyword.matches("\\d+")) {
                    keywordPredicates.add(cb.equal(root.get("reporterUserId"), Long.parseLong(keyword)));
                }
                predicates.add(cb.or(keywordPredicates.toArray(Predicate[]::new)));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), LocalDateTime.of(dateTo, LocalTime.MAX)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<ComplaintTicket> ticketPage = complaintTicketRepository.findAll(spec, pageable);
        if (ticketPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        Map<Long, String> adminNameMap = resolveAdminNameMap(ticketPage.getContent().stream()
            .map(ComplaintTicket::getAssignedAdminId)
            .toList());

        List<AdminComplaintTicketListItemDTO> items = ticketPage.getContent().stream().map(ticket ->
            new AdminComplaintTicketListItemDTO(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getReporterUserId(),
                ticket.getTargetType() == null ? null : ticket.getTargetType().name(),
                ticket.getTitle(),
                ticket.getPriority() == null ? null : ticket.getPriority().name(),
                ticket.getStatus() == null ? null : ticket.getStatus().name(),
                adminNameMap.get(ticket.getAssignedAdminId()),
                ticket.getLastReplyAt(),
                ticket.getCreatedAt()
            )
        ).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, ticketPage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public AdminComplaintTicketDetailDTO detail(Long ticketId) {
        ComplaintTicket ticket = complaintTicketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(ErrorCode.COMPLAINT_TICKET_NOT_FOUND, "Complaint ticket not found"));

        return toAdminDetailDTO(ticket);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional
    public AdminComplaintTicketDetailDTO reply(Long ticketId, AdminReplyComplaintTicketRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        ComplaintTicket ticket = complaintTicketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(ErrorCode.COMPLAINT_TICKET_NOT_FOUND, "Complaint ticket not found"));

        Map<String, Object> before = complaintAuditSnapshot(ticket);

        ComplaintTicketReply reply = new ComplaintTicketReply();
        reply.setTicketId(ticket.getId());
        reply.setAuthorType(ComplaintReplyAuthorType.ADMIN);
        reply.setAuthorAdminId(adminId);
        reply.setContent(normalizeRequiredText(request.getContent(), "content"));
        reply.setInternalNote(Boolean.TRUE.equals(request.getIsInternalNote()));
        complaintTicketReplyRepository.save(reply);

        ComplaintTicketStatus moveToStatus = parseStatus(request.getMoveToStatus(), false);
        if (moveToStatus != null) {
            if (moveToStatus != ComplaintTicketStatus.IN_REVIEW && moveToStatus != ComplaintTicketStatus.WAITING_USER) {
                throw new BizException(ErrorCode.COMPLAINT_TICKET_STATUS_INVALID, "moveToStatus is not supported");
            }
            ensureTransitionAllowed(ticket.getStatus(), moveToStatus);
            ticket.setStatus(moveToStatus);
        }

        ticket.setLastReplyAt(LocalDateTime.now());
        complaintTicketRepository.save(ticket);

        adminAuditService.writeAuditLog(
            adminId,
            "OPS_COMPLAINT_REPLY",
            "COMPLAINT_TICKET",
            String.valueOf(ticket.getId()),
            before,
            complaintAuditSnapshot(ticket),
            reply.isInternalNote() ? "internal_note" : "public_reply"
        );

        return toAdminDetailDTO(ticket);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional
    public AdminComplaintTicketDetailDTO updateStatus(Long ticketId, UpdateComplaintTicketStatusRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        ComplaintTicket ticket = complaintTicketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(ErrorCode.COMPLAINT_TICKET_NOT_FOUND, "Complaint ticket not found"));

        ComplaintTicketStatus targetStatus = parseStatus(request.getStatus(), true);
        if (targetStatus != ComplaintTicketStatus.IN_REVIEW
            && targetStatus != ComplaintTicketStatus.WAITING_USER
            && targetStatus != ComplaintTicketStatus.RESOLVED
            && targetStatus != ComplaintTicketStatus.REJECTED
            && targetStatus != ComplaintTicketStatus.CLOSED) {
            throw new BizException(ErrorCode.COMPLAINT_TICKET_STATUS_INVALID, "status is not supported");
        }
        ensureTransitionAllowed(ticket.getStatus(), targetStatus);

        Long assignedAdminId = request.getAssignedAdminId();
        if (assignedAdminId != null) {
            adminUserRepository.findById(assignedAdminId)
                .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "assignedAdminId is invalid"));
        }

        Map<String, Object> before = complaintAuditSnapshot(ticket);

        ticket.setStatus(targetStatus);
        if (assignedAdminId != null) {
            ticket.setAssignedAdminId(assignedAdminId);
        }
        ticket.setTriageNote(normalizeText(request.getTriageNote()));
        ticket.setResolutionNote(normalizeText(request.getResolutionNote()));

        if (targetStatus == ComplaintTicketStatus.RESOLVED
            || targetStatus == ComplaintTicketStatus.REJECTED
            || targetStatus == ComplaintTicketStatus.CLOSED) {
            ticket.setHandledAt(LocalDateTime.now());
        }

        complaintTicketRepository.save(ticket);

        adminAuditService.writeAuditLog(
            adminId,
            "OPS_COMPLAINT_STATUS_UPDATE",
            "COMPLAINT_TICKET",
            String.valueOf(ticket.getId()),
            before,
            complaintAuditSnapshot(ticket),
            null
        );

        return toAdminDetailDTO(ticket);
    }

    private void ensureTransitionAllowed(ComplaintTicketStatus from, ComplaintTicketStatus to) {
        if (from == null || to == null) {
            throw new BizException(ErrorCode.COMPLAINT_TICKET_STATUS_INVALID, "Complaint ticket status is invalid");
        }
        if (from == to) {
            return;
        }

        boolean allowed = switch (from) {
            case SUBMITTED -> to == ComplaintTicketStatus.IN_REVIEW || to == ComplaintTicketStatus.CANCELLED_BY_USER;
            case IN_REVIEW -> to == ComplaintTicketStatus.WAITING_USER
                || to == ComplaintTicketStatus.RESOLVED
                || to == ComplaintTicketStatus.REJECTED;
            case WAITING_USER -> to == ComplaintTicketStatus.IN_REVIEW || to == ComplaintTicketStatus.CANCELLED_BY_USER;
            case RESOLVED -> to == ComplaintTicketStatus.CLOSED;
            case REJECTED -> to == ComplaintTicketStatus.CLOSED;
            case CLOSED, CANCELLED_BY_USER -> false;
        };

        if (!allowed) {
            throw new BizException(ErrorCode.COMPLAINT_TICKET_STATUS_INVALID, "Complaint ticket status transition is invalid");
        }
    }

    private AdminComplaintTicketDetailDTO toAdminDetailDTO(ComplaintTicket ticket) {
        UserProfile reporterProfile = userProfileRepository.findByUserId(ticket.getReporterUserId()).orElse(null);

        Map<Long, String> adminNameMap = resolveAdminNameMap(
            ticket.getAssignedAdminId() == null ? Collections.emptyList() : List.of(ticket.getAssignedAdminId())
        );

        List<ComplaintTicketReply> replies = complaintTicketReplyRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId());
        Map<Long, UserProfile> userProfileMap = resolveUserProfiles(replies.stream().map(ComplaintTicketReply::getAuthorUserId).toList());
        Map<Long, AdminUser> adminMap = resolveAdmins(replies.stream().map(ComplaintTicketReply::getAuthorAdminId).toList());

        List<AdminComplaintReplyDTO> replyDTOs = replies.stream().map(reply -> new AdminComplaintReplyDTO(
            reply.getId(),
            reply.getAuthorType() == null ? null : reply.getAuthorType().name(),
            resolveAuthorName(reply, userProfileMap, adminMap),
            reply.getContent(),
            reply.isInternalNote(),
            reply.getCreatedAt()
        )).toList();

        return new AdminComplaintTicketDetailDTO(
            ticket.getId(),
            ticket.getTicketNo(),
            ticket.getReporterUserId(),
            reporterProfile != null ? reporterProfile.getNickname() : null,
            ticket.getTargetType() == null ? null : ticket.getTargetType().name(),
            ticket.getTargetId(),
            ticket.getTitle(),
            ticket.getContent(),
            ticket.getPriority() == null ? null : ticket.getPriority().name(),
            ticket.getStatus() == null ? null : ticket.getStatus().name(),
            ticket.getAssignedAdminId(),
            adminNameMap.get(ticket.getAssignedAdminId()),
            ticket.getTriageNote(),
            ticket.getResolutionNote(),
            ticket.getContactMobile(),
            resolveEvidencePhotos(ticket.getEvidenceFileIds()),
            replyDTOs,
            ticket.getLastReplyAt(),
            ticket.getHandledAt(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    private String resolveAuthorName(ComplaintTicketReply reply,
                                     Map<Long, UserProfile> userProfileMap,
                                     Map<Long, AdminUser> adminMap) {
        if (reply.getAuthorType() == ComplaintReplyAuthorType.USER) {
            UserProfile profile = userProfileMap.get(reply.getAuthorUserId());
            return profile != null ? profile.getNickname() : "用户";
        }
        if (reply.getAuthorType() == ComplaintReplyAuthorType.ADMIN) {
            AdminUser admin = adminMap.get(reply.getAuthorAdminId());
            return admin != null ? admin.getDisplayName() : "管理员";
        }
        return "系统";
    }

    private List<ComplaintEvidencePhotoDTO> resolveEvidencePhotos(String evidenceFileIdsJson) {
        List<Long> ids = fromJsonLongList(evidenceFileIdsJson);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(ids).stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));

        List<ComplaintEvidencePhotoDTO> photos = new ArrayList<>();
        for (Long id : ids) {
            FileObject fileObject = fileMap.get(id);
            if (fileObject == null) {
                continue;
            }
            photos.add(new ComplaintEvidencePhotoDTO(fileObject.getId(), fileObject.getPublicUrl()));
        }
        return photos;
    }

    private List<Long> fromJsonLongList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Long> values = objectMapper.readValue(json, LONG_LIST_TYPE);
            if (values == null) {
                return Collections.emptyList();
            }
            return values;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private Map<Long, String> resolveAdminNameMap(Collection<Long> adminIds) {
        Set<Long> normalized = adminIds == null ? Collections.emptySet() : adminIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }

        return adminUserRepository.findAllById(normalized).stream()
            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));
    }

    private Map<Long, UserProfile> resolveUserProfiles(Collection<Long> userIds) {
        Set<Long> normalized = userIds == null ? Collections.emptySet() : userIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }

        return userProfileRepository.findByUserIdIn(normalized).stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
    }

    private Map<Long, AdminUser> resolveAdmins(Collection<Long> adminIds) {
        Set<Long> normalized = adminIds == null ? Collections.emptySet() : adminIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));

        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }

        return adminUserRepository.findAllById(normalized).stream()
            .collect(Collectors.toMap(AdminUser::getId, Function.identity()));
    }

    private Map<String, Object> complaintAuditSnapshot(ComplaintTicket ticket) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", ticket.getId());
        snapshot.put("ticketNo", ticket.getTicketNo());
        snapshot.put("status", ticket.getStatus() == null ? null : ticket.getStatus().name());
        snapshot.put("priority", ticket.getPriority() == null ? null : ticket.getPriority().name());
        snapshot.put("assignedAdminId", ticket.getAssignedAdminId());
        snapshot.put("triageNote", ticket.getTriageNote());
        snapshot.put("resolutionNote", ticket.getResolutionNote());
        snapshot.put("lastReplyAt", ticket.getLastReplyAt());
        snapshot.put("handledAt", ticket.getHandledAt());
        return snapshot;
    }

    private ComplaintTicketStatus parseStatus(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return ComplaintTicketStatus.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private ComplaintPriority parsePriority(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "priority is required");
            }
            return null;
        }
        try {
            return ComplaintPriority.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "priority is invalid");
        }
    }

    private ComplaintTargetType parseTargetType(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "targetType is required");
            }
            return null;
        }
        try {
            return ComplaintTargetType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "targetType is invalid");
        }
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
