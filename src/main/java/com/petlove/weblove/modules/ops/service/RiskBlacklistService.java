package com.petlove.weblove.modules.ops.service;

import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.admin.service.AdminAuditService;
import com.petlove.weblove.modules.ops.dto.admin.AdminBlacklistListItemDTO;
import com.petlove.weblove.modules.ops.dto.admin.AdminBlacklistQuery;
import com.petlove.weblove.modules.ops.dto.admin.UpdateBlacklistEntryStatusRequest;
import com.petlove.weblove.modules.ops.dto.admin.UpsertBlacklistEntryRequest;
import com.petlove.weblove.modules.ops.entity.RiskBlacklistEntry;
import com.petlove.weblove.modules.ops.enums.BlacklistActionMode;
import com.petlove.weblove.modules.ops.enums.BlacklistScopeType;
import com.petlove.weblove.modules.ops.enums.BlacklistStatus;
import com.petlove.weblove.modules.ops.enums.BlacklistSubjectType;
import com.petlove.weblove.modules.ops.repository.RiskBlacklistEntryRepository;
import com.petlove.weblove.modules.risk.RiskActionKeys;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskBlacklistService {

    private final RiskBlacklistEntryRepository riskBlacklistEntryRepository;
    private final AdminAuditService adminAuditService;

    public RiskBlacklistService(RiskBlacklistEntryRepository riskBlacklistEntryRepository,
                                AdminAuditService adminAuditService) {
        this.riskBlacklistEntryRepository = riskBlacklistEntryRepository;
        this.adminAuditService = adminAuditService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public PageResponse<AdminBlacklistListItemDTO> list(AdminBlacklistQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        BlacklistSubjectType subjectType = parseSubjectType(query.getSubjectType(), false);
        BlacklistScopeType scopeType = parseScopeType(query.getScopeType(), false);
        BlacklistStatus status = parseStatus(query.getStatus(), false);

        String subjectValue = normalizeText(query.getSubjectValue());
        String scopeValue = normalizeText(query.getScopeValue());
        String keyword = normalizeText(query.getKeyword());

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<RiskBlacklistEntry> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (subjectType != null) {
                predicates.add(cb.equal(root.get("subjectType"), subjectType));
            }
            if (scopeType != null) {
                predicates.add(cb.equal(root.get("scopeType"), scopeType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (subjectValue != null) {
                predicates.add(cb.like(cb.lower(root.get("subjectValue")), "%" + subjectValue.toLowerCase(Locale.ROOT) + "%"));
            }
            if (scopeValue != null) {
                predicates.add(cb.equal(root.get("scopeValue"), scopeValue.toUpperCase(Locale.ROOT)));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("subjectValue")), like),
                    cb.like(cb.lower(root.get("scopeValue")), like),
                    cb.like(cb.lower(root.get("reasonCode")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("reasonNote"), "")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<RiskBlacklistEntry> entryPage = riskBlacklistEntryRepository.findAll(spec, pageable);
        if (entryPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdminBlacklistListItemDTO> items = entryPage.getContent().stream()
            .map(this::toListItemDTO)
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, entryPage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public AdminBlacklistListItemDTO upsert(Long blacklistId, UpsertBlacklistEntryRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        BlacklistSubjectType subjectType = parseSubjectType(request.getSubjectType(), true);
        BlacklistScopeType scopeType = parseScopeType(request.getScopeType(), true);
        BlacklistActionMode actionMode = parseActionMode(request.getActionMode(), true);
        BlacklistStatus status = parseStatus(request.getStatus(), false);

        String subjectValue = normalizeRequiredText(request.getSubjectValue(), "subjectValue");
        String scopeValue = normalizeRequiredText(request.getScopeValue(), "scopeValue").toUpperCase(Locale.ROOT);
        String reasonCode = normalizeRequiredText(request.getReasonCode(), "reasonCode");
        String reasonNote = normalizeText(request.getReasonNote());
        LocalDateTime startAt = parseDateTime(request.getStartAt(), "startAt", true);
        LocalDateTime endAt = parseDateTime(request.getEndAt(), "endAt", false);

        if (endAt != null && !endAt.isAfter(startAt)) {
            throw new BizException(ErrorCode.RISK_CONFIG_INVALID, "endAt must be later than startAt");
        }

        RiskBlacklistEntry entry;
        boolean create = blacklistId == null || blacklistId <= 0;
        if (create) {
            entry = new RiskBlacklistEntry();
            entry.setCreatedByAdminId(adminId);
            if (status == null) {
                status = BlacklistStatus.ACTIVE;
            }
        } else {
            entry = riskBlacklistEntryRepository.findById(blacklistId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Blacklist entry not found"));
            if (status == null) {
                status = entry.getStatus();
            }
        }

        Map<String, Object> before = create ? null : auditSnapshot(entry);

        entry.setSubjectType(subjectType);
        entry.setSubjectValue(subjectValue);
        entry.setScopeType(scopeType);
        entry.setScopeValue(scopeValue);
        entry.setActionMode(actionMode);
        entry.setReasonCode(reasonCode);
        entry.setReasonNote(reasonNote);
        entry.setStartAt(startAt);
        entry.setEndAt(endAt);
        entry.setStatus(status);
        entry.setUpdatedByAdminId(adminId);

        RiskBlacklistEntry saved = riskBlacklistEntryRepository.save(entry);

        adminAuditService.writeAuditLog(
            adminId,
            "OPS_BLACKLIST_UPSERT",
            "RISK_BLACKLIST",
            String.valueOf(saved.getId()),
            before,
            auditSnapshot(saved),
            null
        );

        return toListItemDTO(saved);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public AdminBlacklistListItemDTO updateStatus(Long blacklistId, UpdateBlacklistEntryStatusRequest request) {
        long adminId = SecurityUtils.currentAdminId();
        RiskBlacklistEntry entry = riskBlacklistEntryRepository.findById(blacklistId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Blacklist entry not found"));

        BlacklistStatus targetStatus = parseStatus(request.getStatus(), true);
        if (targetStatus == BlacklistStatus.EXPIRED) {
            throw new BizException(ErrorCode.RISK_CONFIG_INVALID, "status does not support EXPIRED");
        }

        Map<String, Object> before = auditSnapshot(entry);

        entry.setStatus(targetStatus);
        if (normalizeText(request.getReasonNote()) != null) {
            entry.setReasonNote(normalizeText(request.getReasonNote()));
        }
        entry.setUpdatedByAdminId(adminId);
        RiskBlacklistEntry saved = riskBlacklistEntryRepository.save(entry);

        adminAuditService.writeAuditLog(
            adminId,
            "OPS_BLACKLIST_STATUS_UPDATE",
            "RISK_BLACKLIST",
            String.valueOf(saved.getId()),
            before,
            auditSnapshot(saved),
            null
        );

        return toListItemDTO(saved);
    }

    @Transactional(readOnly = true)
    public Optional<RiskBlacklistEntry> findFirstBlockingEntry(Long userId, String mobile, String actionKey) {
        List<RiskBlacklistEntry> candidates = new ArrayList<>();

        if (userId != null) {
            candidates.addAll(riskBlacklistEntryRepository.findBySubjectTypeAndSubjectValueAndStatus(
                BlacklistSubjectType.USER_ID,
                String.valueOf(userId),
                BlacklistStatus.ACTIVE
            ));
        }

        String normalizedMobile = normalizeText(mobile);
        if (normalizedMobile != null) {
            candidates.addAll(riskBlacklistEntryRepository.findBySubjectTypeAndSubjectValueAndStatus(
                BlacklistSubjectType.MOBILE,
                normalizedMobile,
                BlacklistStatus.ACTIVE
            ));
        }

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        String module = RiskActionKeys.resolveModule(actionKey);
        LocalDateTime now = LocalDateTime.now();

        return candidates.stream()
            .filter(entry -> isEntryEffective(entry, now))
            .filter(entry -> matchesScope(entry, module, actionKey))
            .filter(entry -> entry.getActionMode() == BlacklistActionMode.BLOCK)
            .sorted(Comparator.comparing(RiskBlacklistEntry::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
            .findFirst();
    }

    private boolean isEntryEffective(RiskBlacklistEntry entry, LocalDateTime now) {
        if (entry.getStatus() != BlacklistStatus.ACTIVE) {
            return false;
        }
        if (entry.getStartAt() != null && now.isBefore(entry.getStartAt())) {
            return false;
        }
        return entry.getEndAt() == null || !now.isAfter(entry.getEndAt());
    }

    private boolean matchesScope(RiskBlacklistEntry entry, String module, String actionKey) {
        String scopeValue = normalizeText(entry.getScopeValue());
        if (entry.getScopeType() == BlacklistScopeType.GLOBAL) {
            return true;
        }
        if (entry.getScopeType() == BlacklistScopeType.MODULE) {
            return Objects.equals(scopeValue == null ? null : scopeValue.toUpperCase(Locale.ROOT), module);
        }
        if (entry.getScopeType() == BlacklistScopeType.ACTION) {
            return Objects.equals(scopeValue == null ? null : scopeValue.toUpperCase(Locale.ROOT),
                normalizeText(actionKey) == null ? null : normalizeText(actionKey).toUpperCase(Locale.ROOT));
        }
        return false;
    }

    private AdminBlacklistListItemDTO toListItemDTO(RiskBlacklistEntry entry) {
        return new AdminBlacklistListItemDTO(
            entry.getId(),
            entry.getSubjectType() == null ? null : entry.getSubjectType().name(),
            maskSubjectValue(entry.getSubjectType(), entry.getSubjectValue()),
            entry.getScopeType() == null ? null : entry.getScopeType().name(),
            entry.getScopeValue(),
            entry.getActionMode() == null ? null : entry.getActionMode().name(),
            entry.getReasonCode(),
            resolveStatus(entry).name(),
            entry.getStartAt(),
            entry.getEndAt(),
            entry.getUpdatedAt()
        );
    }

    private Map<String, Object> auditSnapshot(RiskBlacklistEntry entry) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", entry.getId());
        snapshot.put("subjectType", entry.getSubjectType() == null ? null : entry.getSubjectType().name());
        snapshot.put("subjectValue", entry.getSubjectValue());
        snapshot.put("scopeType", entry.getScopeType() == null ? null : entry.getScopeType().name());
        snapshot.put("scopeValue", entry.getScopeValue());
        snapshot.put("actionMode", entry.getActionMode() == null ? null : entry.getActionMode().name());
        snapshot.put("reasonCode", entry.getReasonCode());
        snapshot.put("status", resolveStatus(entry).name());
        snapshot.put("startAt", entry.getStartAt());
        snapshot.put("endAt", entry.getEndAt());
        return snapshot;
    }

    private BlacklistStatus resolveStatus(RiskBlacklistEntry entry) {
        if (entry.getStatus() == BlacklistStatus.ACTIVE
            && entry.getEndAt() != null
            && entry.getEndAt().isBefore(LocalDateTime.now())) {
            return BlacklistStatus.EXPIRED;
        }
        return entry.getStatus();
    }

    private String maskSubjectValue(BlacklistSubjectType subjectType, String subjectValue) {
        if (subjectValue == null || subjectValue.isBlank()) {
            return "";
        }
        if (subjectType == BlacklistSubjectType.MOBILE) {
            return MaskUtil.maskMobile(subjectValue.trim());
        }
        String value = subjectValue.trim();
        if (value.length() <= 4) {
            return "****";
        }
        return value.substring(0, 2) + "****" + value.substring(value.length() - 2);
    }

    private BlacklistSubjectType parseSubjectType(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "subjectType is required");
            }
            return null;
        }
        try {
            return BlacklistSubjectType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "subjectType is invalid");
        }
    }

    private BlacklistScopeType parseScopeType(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "scopeType is required");
            }
            return null;
        }
        try {
            return BlacklistScopeType.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "scopeType is invalid");
        }
    }

    private BlacklistActionMode parseActionMode(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "actionMode is required");
            }
            return null;
        }
        try {
            return BlacklistActionMode.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "actionMode is invalid");
        }
    }

    private BlacklistStatus parseStatus(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return BlacklistStatus.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private LocalDateTime parseDateTime(String raw, String field, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, field + " is required");
            }
            return null;
        }
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, field + " is invalid");
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
