package com.petlove.weblove.modules.ops.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.ops.dto.user.ComplaintEvidencePhotoDTO;
import com.petlove.weblove.modules.ops.dto.user.ComplaintReplyDTO;
import com.petlove.weblove.modules.ops.dto.user.ComplaintTicketDetailDTO;
import com.petlove.weblove.modules.ops.dto.user.ComplaintTicketListItemDTO;
import com.petlove.weblove.modules.ops.dto.user.MyComplaintTicketQuery;
import com.petlove.weblove.modules.ops.dto.user.ReplyComplaintTicketRequest;
import com.petlove.weblove.modules.ops.dto.user.SubmitComplaintTicketRequest;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComplaintTicketService {

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };

    private final ComplaintTicketRepository complaintTicketRepository;
    private final ComplaintTicketReplyRepository complaintTicketReplyRepository;
    private final FileObjectRepository fileObjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    public ComplaintTicketService(ComplaintTicketRepository complaintTicketRepository,
                                  ComplaintTicketReplyRepository complaintTicketReplyRepository,
                                  FileObjectRepository fileObjectRepository,
                                  UserProfileRepository userProfileRepository,
                                  AdminUserRepository adminUserRepository,
                                  ObjectMapper objectMapper) {
        this.complaintTicketRepository = complaintTicketRepository;
        this.complaintTicketReplyRepository = complaintTicketReplyRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ComplaintTicketDetailDTO submit(SubmitComplaintTicketRequest request) {
        long userId = SecurityUtils.currentUserId();

        ComplaintTargetType targetType = parseTargetType(request.getTargetType(), true);
        ComplaintPriority priority = parsePriority(request.getPriority(), false);
        if (priority == null) {
            priority = ComplaintPriority.MEDIUM;
        }

        List<Long> evidenceFileIds = normalizeFileIds(request.getEvidenceFileIds(), 9);
        validateEvidenceFiles(evidenceFileIds, userId);

        ComplaintTicket ticket = new ComplaintTicket();
        ticket.setTicketNo(generateTicketNo());
        ticket.setReporterUserId(userId);
        ticket.setTargetType(targetType);
        ticket.setTargetId(request.getTargetId());
        ticket.setTitle(normalizeRequiredText(request.getTitle(), "title"));
        ticket.setContent(normalizeRequiredText(request.getContent(), "content"));
        ticket.setPriority(priority);
        ticket.setStatus(ComplaintTicketStatus.SUBMITTED);

        String contactMobile = normalizeText(request.getContactMobile());
        ticket.setContactMobile(contactMobile);
        ticket.setContactMobileMasked(contactMobile == null ? null : MaskUtil.maskMobile(contactMobile));
        ticket.setEvidenceFileIds(toJson(evidenceFileIds));

        ComplaintTicket saved = complaintTicketRepository.save(ticket);
        return toUserDetail(saved, true);
    }

    @Transactional(readOnly = true)
    public PageResponse<ComplaintTicketListItemDTO> myTickets(MyComplaintTicketQuery query) {
        long userId = SecurityUtils.currentUserId();

        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        ComplaintTicketStatus status = parseStatus(query.getStatus(), false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<ComplaintTicket> ticketPage = status == null
            ? complaintTicketRepository.findByReporterUserId(userId, pageable)
            : complaintTicketRepository.findByReporterUserIdAndStatus(userId, status, pageable);

        if (ticketPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<ComplaintTicketListItemDTO> items = ticketPage.getContent().stream()
            .map(ticket -> new ComplaintTicketListItemDTO(
                ticket.getId(),
                ticket.getTicketNo(),
                ticket.getTargetType() == null ? null : ticket.getTargetType().name(),
                ticket.getTitle(),
                ticket.getPriority() == null ? null : ticket.getPriority().name(),
                ticket.getStatus() == null ? null : ticket.getStatus().name(),
                ticket.getLastReplyAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
            ))
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, ticketPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ComplaintTicketDetailDTO myDetail(Long ticketId) {
        long userId = SecurityUtils.currentUserId();
        ComplaintTicket ticket = complaintTicketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(ErrorCode.COMPLAINT_TICKET_NOT_FOUND, "Complaint ticket not found"));

        ensureOwner(ticket, userId);
        return toUserDetail(ticket, true);
    }

    @Transactional
    public ComplaintTicketDetailDTO reply(Long ticketId, ReplyComplaintTicketRequest request) {
        long userId = SecurityUtils.currentUserId();

        ComplaintTicket ticket = complaintTicketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(ErrorCode.COMPLAINT_TICKET_NOT_FOUND, "Complaint ticket not found"));

        ensureOwner(ticket, userId);

        if (!isUserReplyAllowed(ticket.getStatus())) {
            throw new BizException(ErrorCode.COMPLAINT_TICKET_STATUS_INVALID, "Current ticket status does not allow reply");
        }

        ComplaintTicketReply reply = new ComplaintTicketReply();
        reply.setTicketId(ticket.getId());
        reply.setAuthorType(ComplaintReplyAuthorType.USER);
        reply.setAuthorUserId(userId);
        reply.setContent(normalizeRequiredText(request.getContent(), "content"));
        reply.setInternalNote(false);
        complaintTicketReplyRepository.save(reply);

        if (ticket.getStatus() == ComplaintTicketStatus.WAITING_USER) {
            ticket.setStatus(ComplaintTicketStatus.IN_REVIEW);
        }
        ticket.setLastReplyAt(LocalDateTime.now());
        complaintTicketRepository.save(ticket);

        return toUserDetail(ticket, true);
    }

    @Transactional
    public ComplaintTicketDetailDTO cancel(Long ticketId) {
        long userId = SecurityUtils.currentUserId();

        ComplaintTicket ticket = complaintTicketRepository.findById(ticketId)
            .orElseThrow(() -> new BizException(ErrorCode.COMPLAINT_TICKET_NOT_FOUND, "Complaint ticket not found"));

        ensureOwner(ticket, userId);
        if (ticket.getStatus() != ComplaintTicketStatus.SUBMITTED && ticket.getStatus() != ComplaintTicketStatus.WAITING_USER) {
            throw new BizException(ErrorCode.COMPLAINT_TICKET_STATUS_INVALID, "Current ticket status does not allow cancel");
        }

        ticket.setStatus(ComplaintTicketStatus.CANCELLED_BY_USER);
        complaintTicketRepository.save(ticket);

        return toUserDetail(ticket, true);
    }

    @Transactional(readOnly = true)
    public ComplaintTicketDetailDTO toUserDetail(ComplaintTicket ticket, boolean maskContactMobile) {
        List<ComplaintTicketReply> replies = complaintTicketReplyRepository.findByTicketIdOrderByCreatedAtAscIdAsc(ticket.getId())
            .stream()
            .filter(reply -> !reply.isInternalNote())
            .toList();

        Map<Long, UserProfile> userProfileMap = buildUserProfileMap(replies.stream().map(ComplaintTicketReply::getAuthorUserId).toList());
        Map<Long, AdminUser> adminMap = buildAdminMap(replies.stream().map(ComplaintTicketReply::getAuthorAdminId).toList());

        List<ComplaintReplyDTO> replyDTOs = replies.stream()
            .map(reply -> new ComplaintReplyDTO(
                reply.getId(),
                reply.getAuthorType() == null ? null : reply.getAuthorType().name(),
                resolveReplyAuthorName(reply, userProfileMap, adminMap),
                reply.getContent(),
                reply.getCreatedAt()
            ))
            .toList();

        List<ComplaintEvidencePhotoDTO> photos = resolveEvidencePhotos(ticket.getEvidenceFileIds());

        return new ComplaintTicketDetailDTO(
            ticket.getId(),
            ticket.getTicketNo(),
            ticket.getTargetType() == null ? null : ticket.getTargetType().name(),
            ticket.getTargetId(),
            ticket.getTitle(),
            ticket.getContent(),
            ticket.getPriority() == null ? null : ticket.getPriority().name(),
            ticket.getStatus() == null ? null : ticket.getStatus().name(),
            ticket.getTriageNote(),
            ticket.getResolutionNote(),
            maskContactMobile ? ticket.getContactMobileMasked() : ticket.getContactMobile(),
            photos,
            replyDTOs,
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ComplaintReplyDTO> buildUserVisibleReplyDTOs(Collection<ComplaintTicketReply> replies) {
        List<ComplaintTicketReply> safeReplies = replies == null ? Collections.emptyList() : replies.stream()
            .filter(Objects::nonNull)
            .filter(reply -> !reply.isInternalNote())
            .toList();

        Map<Long, UserProfile> userProfileMap = buildUserProfileMap(safeReplies.stream().map(ComplaintTicketReply::getAuthorUserId).toList());
        Map<Long, AdminUser> adminMap = buildAdminMap(safeReplies.stream().map(ComplaintTicketReply::getAuthorAdminId).toList());

        return safeReplies.stream().map(reply -> new ComplaintReplyDTO(
            reply.getId(),
            reply.getAuthorType() == null ? null : reply.getAuthorType().name(),
            resolveReplyAuthorName(reply, userProfileMap, adminMap),
            reply.getContent(),
            reply.getCreatedAt()
        )).toList();
    }

    private String resolveReplyAuthorName(ComplaintTicketReply reply,
                                          Map<Long, UserProfile> userProfileMap,
                                          Map<Long, AdminUser> adminMap) {
        if (reply.getAuthorType() == ComplaintReplyAuthorType.USER) {
            UserProfile profile = userProfileMap.get(reply.getAuthorUserId());
            return profile != null ? profile.getNickname() : "用户";
        }
        if (reply.getAuthorType() == ComplaintReplyAuthorType.ADMIN) {
            AdminUser adminUser = adminMap.get(reply.getAuthorAdminId());
            return adminUser != null ? adminUser.getDisplayName() : "平台客服";
        }
        return "系统";
    }

    private Map<Long, UserProfile> buildUserProfileMap(Collection<Long> userIds) {
        Set<Long> normalized = userIds == null ? Collections.emptySet() : userIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return userProfileRepository.findByUserIdIn(normalized).stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
    }

    private Map<Long, AdminUser> buildAdminMap(Collection<Long> adminIds) {
        Set<Long> normalized = adminIds == null ? Collections.emptySet() : adminIds.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserRepository.findAllById(normalized).stream()
            .collect(Collectors.toMap(AdminUser::getId, Function.identity()));
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

    private void ensureOwner(ComplaintTicket ticket, Long userId) {
        if (!Objects.equals(ticket.getReporterUserId(), userId)) {
            throw new BizException(ErrorCode.COMPLAINT_TICKET_NOT_OWNER, "Complaint ticket is not owned by current user");
        }
    }

    private boolean isUserReplyAllowed(ComplaintTicketStatus status) {
        return status == ComplaintTicketStatus.SUBMITTED
            || status == ComplaintTicketStatus.IN_REVIEW
            || status == ComplaintTicketStatus.WAITING_USER;
    }

    private String generateTicketNo() {
        String prefix = "CP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int retry = 0;
        String ticketNo;
        do {
            ticketNo = prefix + ThreadLocalRandom.current().nextInt(100000, 999999);
            retry++;
        } while (complaintTicketRepository.existsByTicketNo(ticketNo) && retry < 20);

        if (complaintTicketRepository.existsByTicketNo(ticketNo)) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to generate complaint ticket number");
        }
        return ticketNo;
    }

    private void validateEvidenceFiles(List<Long> fileIds, Long userId) {
        if (fileIds.isEmpty()) {
            return;
        }

        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(fileIds).stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));

        for (Long fileId : fileIds) {
            FileObject fileObject = fileMap.get(fileId);
            if (fileObject == null || fileObject.getStatus() != FileStatus.READY
                || fileObject.getBizType() != FileBizType.COMPLAINT_EVIDENCE) {
                throw new BizException(ErrorCode.COMPLAINT_TICKET_FILE_INVALID, "Evidence file is invalid");
            }
            if (!Objects.equals(fileObject.getOwnerUserId(), userId)) {
                throw new BizException(ErrorCode.COMPLAINT_TICKET_FILE_NOT_OWNED, "Evidence file is not owned by current user");
            }
        }
    }

    private List<Long> normalizeFileIds(List<Long> ids, int maxCount) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        if (ids.size() > maxCount) {
            throw new BizException(ErrorCode.INVALID_PARAM, "evidenceFileIds size exceeds limit");
        }

        List<Long> normalized = ids.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();

        if (normalized.size() != ids.size()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "evidenceFileIds contains invalid value");
        }
        return normalized;
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
            return ComplaintTargetType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "targetType is invalid");
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
            return ComplaintPriority.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "priority is invalid");
        }
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
            return ComplaintTicketStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
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

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
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
