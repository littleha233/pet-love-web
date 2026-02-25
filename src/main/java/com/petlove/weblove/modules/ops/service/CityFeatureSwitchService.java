package com.petlove.weblove.modules.ops.service;

import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.admin.service.AdminAuditService;
import com.petlove.weblove.modules.ops.dto.admin.CityFeatureSwitchDTO;
import com.petlove.weblove.modules.ops.dto.admin.CityFeatureSwitchQuery;
import com.petlove.weblove.modules.ops.dto.admin.UpsertCityFeatureSwitchRequest;
import com.petlove.weblove.modules.ops.entity.CityFeatureSwitch;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.ops.repository.CityFeatureSwitchRepository;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CityFeatureSwitchService {

    public record CityFeatureCheckResult(boolean allowed, ErrorCode errorCode, String noticeText) {

        static CityFeatureCheckResult allow() {
            return new CityFeatureCheckResult(true, null, null);
        }

        static CityFeatureCheckResult deny(ErrorCode errorCode, String noticeText) {
            return new CityFeatureCheckResult(false, errorCode, noticeText);
        }
    }

    private final CityFeatureSwitchRepository cityFeatureSwitchRepository;
    private final CityRepository cityRepository;
    private final AdminAuditService adminAuditService;

    public CityFeatureSwitchService(CityFeatureSwitchRepository cityFeatureSwitchRepository,
                                    CityRepository cityRepository,
                                    AdminAuditService adminAuditService) {
        this.cityFeatureSwitchRepository = cityFeatureSwitchRepository;
        this.cityRepository = cityRepository;
        this.adminAuditService = adminAuditService;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public PageResponse<CityFeatureSwitchDTO> list(CityFeatureSwitchQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        String cityCode = normalizeText(query.getCityCode());
        CityFeatureKey featureKey = parseFeatureKey(query.getFeatureKey(), false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.ASC, "cityCode").and(Sort.by(Sort.Direction.ASC, "featureKey"))
        );

        Specification<CityFeatureSwitch> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (cityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            }
            if (featureKey != null) {
                predicates.add(cb.equal(root.get("featureKey"), featureKey));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<CityFeatureSwitch> switchPage = cityFeatureSwitchRepository.findAll(spec, pageable);
        if (switchPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<CityFeatureSwitchDTO> items = switchPage.getContent().stream().map(this::toDTO).toList();
        return new PageResponse<>(items, normalizedPage, normalizedPageSize, switchPage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public CityFeatureSwitchDTO upsert(Long switchId, UpsertCityFeatureSwitchRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        String cityCode = normalizeRequiredText(request.getCityCode(), "cityCode");
        String cityName = normalizeRequiredText(request.getCityName(), "cityName");
        CityFeatureKey featureKey = parseFeatureKey(request.getFeatureKey(), true);

        cityRepository.findByCityCode(cityCode)
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "cityCode is invalid"));

        LocalDateTime effectiveFrom = parseDateTime(request.getEffectiveFrom(), "effectiveFrom", false);
        LocalDateTime effectiveTo = parseDateTime(request.getEffectiveTo(), "effectiveTo", false);
        if (effectiveFrom != null && effectiveTo != null && !effectiveTo.isAfter(effectiveFrom)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "effectiveTo must be later than effectiveFrom");
        }

        CityFeatureSwitch featureSwitch;
        boolean create;

        if (switchId != null && switchId > 0) {
            featureSwitch = cityFeatureSwitchRepository.findById(switchId)
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "City feature switch not found"));
            create = false;
        } else {
            Optional<CityFeatureSwitch> existing = cityFeatureSwitchRepository.findByCityCodeAndFeatureKey(cityCode, featureKey);
            if (existing.isPresent()) {
                featureSwitch = existing.get();
                create = false;
            } else {
                featureSwitch = new CityFeatureSwitch();
                create = true;
            }
        }

        Map<String, Object> before = create ? null : auditSnapshot(featureSwitch);

        featureSwitch.setCityCode(cityCode);
        featureSwitch.setCityName(cityName);
        featureSwitch.setFeatureKey(featureKey);
        featureSwitch.setEnabled(Boolean.TRUE.equals(request.getIsEnabled()));
        featureSwitch.setAllowRead(Boolean.TRUE.equals(request.getAllowRead()));
        featureSwitch.setAllowWrite(Boolean.TRUE.equals(request.getAllowWrite()));
        featureSwitch.setNoticeText(normalizeText(request.getNoticeText()));
        featureSwitch.setEffectiveFrom(effectiveFrom);
        featureSwitch.setEffectiveTo(effectiveTo);
        featureSwitch.setUpdatedByAdminId(adminId);

        CityFeatureSwitch saved = cityFeatureSwitchRepository.save(featureSwitch);

        adminAuditService.writeAuditLog(
            adminId,
            "OPS_CITY_FEATURE_UPSERT",
            "CITY_FEATURE_SWITCH",
            String.valueOf(saved.getId()),
            before,
            auditSnapshot(saved),
            null
        );

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public CityFeatureCheckResult checkReadAllowed(String cityCode, CityFeatureKey featureKey) {
        Optional<CityFeatureSwitch> optional = findActiveRule(cityCode, featureKey);
        if (optional.isEmpty()) {
            return CityFeatureCheckResult.allow();
        }

        CityFeatureSwitch featureSwitch = optional.get();
        if (!featureSwitch.isEnabled()) {
            return CityFeatureCheckResult.deny(
                ErrorCode.CITY_FEATURE_NOT_OPEN,
                defaultNotice(featureSwitch, "该城市该功能暂未开放")
            );
        }
        if (!featureSwitch.isAllowRead()) {
            return CityFeatureCheckResult.deny(
                ErrorCode.CITY_FEATURE_READ_DISABLED,
                defaultNotice(featureSwitch, "该城市该功能暂不支持浏览")
            );
        }
        return CityFeatureCheckResult.allow();
    }

    @Transactional(readOnly = true)
    public CityFeatureCheckResult checkWriteAllowed(String cityCode, CityFeatureKey featureKey) {
        Optional<CityFeatureSwitch> optional = findActiveRule(cityCode, featureKey);
        if (optional.isEmpty()) {
            return CityFeatureCheckResult.allow();
        }

        CityFeatureSwitch featureSwitch = optional.get();
        if (!featureSwitch.isEnabled()) {
            return CityFeatureCheckResult.deny(
                ErrorCode.CITY_FEATURE_NOT_OPEN,
                defaultNotice(featureSwitch, "该城市该功能暂未开放")
            );
        }
        if (!featureSwitch.isAllowWrite()) {
            return CityFeatureCheckResult.deny(
                ErrorCode.CITY_FEATURE_WRITE_DISABLED,
                defaultNotice(featureSwitch, "该城市该功能暂不支持当前操作")
            );
        }
        return CityFeatureCheckResult.allow();
    }

    @Transactional(readOnly = true)
    public Set<String> findReadBlockedCityCodes(CityFeatureKey featureKey) {
        if (featureKey == null) {
            return Collections.emptySet();
        }

        LocalDateTime now = LocalDateTime.now();
        Set<String> blockedCityCodes = new LinkedHashSet<>();
        for (CityFeatureSwitch featureSwitch : cityFeatureSwitchRepository.findByFeatureKey(featureKey)) {
            if (!isRuleEffectiveAt(featureSwitch, now)) {
                continue;
            }
            if (featureSwitch.isEnabled() && featureSwitch.isAllowRead()) {
                continue;
            }
            String cityCode = normalizeText(featureSwitch.getCityCode());
            if (cityCode != null) {
                blockedCityCodes.add(cityCode);
            }
        }
        return blockedCityCodes;
    }

    private Optional<CityFeatureSwitch> findActiveRule(String cityCode, CityFeatureKey featureKey) {
        String normalizedCityCode = normalizeText(cityCode);
        if (normalizedCityCode == null || featureKey == null) {
            return Optional.empty();
        }

        Optional<CityFeatureSwitch> optional = cityFeatureSwitchRepository
            .findByCityCodeAndFeatureKey(normalizedCityCode, featureKey);
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        CityFeatureSwitch featureSwitch = optional.get();
        if (!isRuleEffectiveAt(featureSwitch, LocalDateTime.now())) {
            return Optional.empty();
        }
        return Optional.of(featureSwitch);
    }

    private boolean isRuleEffectiveAt(CityFeatureSwitch featureSwitch, LocalDateTime now) {
        if (featureSwitch == null || now == null) {
            return false;
        }
        if (featureSwitch.getEffectiveFrom() != null && now.isBefore(featureSwitch.getEffectiveFrom())) {
            return false;
        }
        return featureSwitch.getEffectiveTo() == null || !now.isAfter(featureSwitch.getEffectiveTo());
    }

    private String defaultNotice(CityFeatureSwitch featureSwitch, String fallback) {
        String notice = normalizeText(featureSwitch.getNoticeText());
        return notice == null ? fallback : notice;
    }

    private CityFeatureSwitchDTO toDTO(CityFeatureSwitch featureSwitch) {
        return new CityFeatureSwitchDTO(
            featureSwitch.getId(),
            featureSwitch.getCityCode(),
            featureSwitch.getCityName(),
            featureSwitch.getFeatureKey() == null ? null : featureSwitch.getFeatureKey().name(),
            featureSwitch.isEnabled(),
            featureSwitch.isAllowRead(),
            featureSwitch.isAllowWrite(),
            featureSwitch.getNoticeText(),
            featureSwitch.getEffectiveFrom(),
            featureSwitch.getEffectiveTo(),
            featureSwitch.getUpdatedAt()
        );
    }

    private Map<String, Object> auditSnapshot(CityFeatureSwitch featureSwitch) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", featureSwitch.getId());
        snapshot.put("cityCode", featureSwitch.getCityCode());
        snapshot.put("cityName", featureSwitch.getCityName());
        snapshot.put("featureKey", featureSwitch.getFeatureKey() == null ? null : featureSwitch.getFeatureKey().name());
        snapshot.put("isEnabled", featureSwitch.isEnabled());
        snapshot.put("allowRead", featureSwitch.isAllowRead());
        snapshot.put("allowWrite", featureSwitch.isAllowWrite());
        snapshot.put("noticeText", featureSwitch.getNoticeText());
        snapshot.put("effectiveFrom", featureSwitch.getEffectiveFrom());
        snapshot.put("effectiveTo", featureSwitch.getEffectiveTo());
        return snapshot;
    }

    private CityFeatureKey parseFeatureKey(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "featureKey is required");
            }
            return null;
        }
        try {
            return CityFeatureKey.valueOf(normalized.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "featureKey is invalid");
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
