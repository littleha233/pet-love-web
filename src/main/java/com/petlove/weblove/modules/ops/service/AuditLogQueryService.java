package com.petlove.weblove.modules.ops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.admin.entity.AdminAuditLog;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.repository.AdminAuditLogRepository;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.ops.dto.admin.AdminAuditLogDetailDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminAuditLogListItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminAuditLogQuery;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
public class AuditLogQueryService {

    private final AdminAuditLogRepository adminAuditLogRepository;
    private final AdminUserRepository adminUserRepository;
    private final ObjectMapper objectMapper;

    public AuditLogQueryService(AdminAuditLogRepository adminAuditLogRepository,
                                AdminUserRepository adminUserRepository,
                                ObjectMapper objectMapper) {
        this.adminAuditLogRepository = adminAuditLogRepository;
        this.adminUserRepository = adminUserRepository;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public PageResponse<AdminAuditLogListItemDTO> list(AdminAuditLogQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        String moduleName = normalizeText(query.getModuleName());
        String actionName = normalizeText(query.getActionName());
        String targetType = normalizeText(query.getTargetType());
        String targetId = normalizeText(query.getTargetId());
        String keyword = normalizeText(query.getKeyword());

        LocalDate dateFrom = query.getDateFrom();
        LocalDate dateTo = query.getDateTo();
        if (dateFrom != null && dateTo != null && dateTo.isBefore(dateFrom)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "dateTo must not be earlier than dateFrom");
        }

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<AdminAuditLog> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getOperatorAdminId() != null) {
                predicates.add(cb.equal(root.get("adminUserId"), query.getOperatorAdminId()));
            }
            if (moduleName != null) {
                predicates.add(cb.like(cb.upper(root.get("action")), moduleName.toUpperCase(Locale.ROOT) + "%"));
            }
            if (actionName != null) {
                predicates.add(cb.like(cb.upper(root.get("action")), "%" + actionName.toUpperCase(Locale.ROOT) + "%"));
            }
            if (targetType != null) {
                predicates.add(cb.equal(cb.upper(root.get("targetType")), targetType.toUpperCase(Locale.ROOT)));
            }
            if (targetId != null) {
                predicates.add(cb.equal(root.get("targetId"), targetId));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(cb.coalesce(root.get("remark"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("requestId"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("action"), "")), like)
                ));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), LocalDateTime.of(dateTo, LocalTime.MAX)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<AdminAuditLog> logPage = adminAuditLogRepository.findAll(spec, pageable);
        if (logPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        Map<Long, String> adminNameMap = resolveAdminNameMap(logPage.getContent().stream().map(AdminAuditLog::getAdminUserId).collect(Collectors.toSet()));

        List<AdminAuditLogListItemDTO> items = logPage.getContent().stream().map(log -> {
            ActionParts parts = splitAction(log.getAction());
            return new AdminAuditLogListItemDTO(
                log.getId(),
                log.getAdminUserId(),
                adminNameMap.get(log.getAdminUserId()),
                parts.moduleName(),
                parts.actionName(),
                log.getTargetType(),
                log.getTargetId(),
                log.getRemark(),
                log.getCreatedAt()
            );
        }).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, logPage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public AdminAuditLogDetailDTO detail(Long auditLogId) {
        AdminAuditLog log = adminAuditLogRepository.findById(auditLogId)
            .orElseThrow(() -> new BizException(ErrorCode.AUDIT_LOG_NOT_FOUND, "Audit log not found"));

        String operatorName = adminUserRepository.findById(log.getAdminUserId()).map(AdminUser::getDisplayName).orElse(null);
        ActionParts parts = splitAction(log.getAction());

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("remark", log.getRemark());
        extraData.put("requestId", log.getRequestId());

        return new AdminAuditLogDetailDTO(
            log.getId(),
            log.getAdminUserId(),
            operatorName,
            parts.moduleName(),
            parts.actionName(),
            log.getTargetType(),
            log.getTargetId(),
            parseJsonOrText(log.getBeforeSnapshot()),
            parseJsonOrText(log.getAfterSnapshot()),
            extraData,
            log.getCreatedAt()
        );
    }

    private Object parseJsonOrText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(value);
        } catch (Exception ex) {
            return value;
        }
    }

    private Map<Long, String> resolveAdminNameMap(Set<Long> adminIds) {
        if (adminIds == null || adminIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return adminUserRepository.findAllById(adminIds).stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));
    }

    private ActionParts splitAction(String action) {
        String safe = action == null ? "UNKNOWN" : action.trim();
        if (safe.isEmpty()) {
            return new ActionParts("UNKNOWN", "UNKNOWN");
        }
        int idx = safe.indexOf('_');
        if (idx <= 0) {
            return new ActionParts(safe.toUpperCase(Locale.ROOT), safe.toUpperCase(Locale.ROOT));
        }
        return new ActionParts(
            safe.substring(0, idx).toUpperCase(Locale.ROOT),
            safe.substring(idx + 1).toUpperCase(Locale.ROOT)
        );
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private record ActionParts(String moduleName, String actionName) {
    }
}
