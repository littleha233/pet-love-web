package com.petlove.weblove.modules.rescue.service;

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
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueClueDetailDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueClueListItemDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueClueQuery;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueGuideDetailDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueGuideListItemDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueGuideQuery;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueResourceDetailDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueResourceListItemDTO;
import com.petlove.weblove.modules.rescue.dto.admin.AdminRescueResourceQuery;
import com.petlove.weblove.modules.rescue.dto.admin.UpdateRescueClueStatusRequest;
import com.petlove.weblove.modules.rescue.dto.admin.UpsertRescueGuideRequest;
import com.petlove.weblove.modules.rescue.dto.admin.UpsertRescueResourceRequest;
import com.petlove.weblove.modules.rescue.dto.user.RescueCluePhotoDTO;
import com.petlove.weblove.modules.rescue.entity.RescueClue;
import com.petlove.weblove.modules.rescue.entity.RescueClueMedia;
import com.petlove.weblove.modules.rescue.entity.RescueGuide;
import com.petlove.weblove.modules.rescue.entity.RescueResource;
import com.petlove.weblove.modules.rescue.enums.RescueClueStatus;
import com.petlove.weblove.modules.rescue.enums.RescueGuideScenarioCode;
import com.petlove.weblove.modules.rescue.enums.RescueGuideStatus;
import com.petlove.weblove.modules.rescue.enums.RescuePetType;
import com.petlove.weblove.modules.rescue.enums.RescueResourceStatus;
import com.petlove.weblove.modules.rescue.enums.RescueResourceType;
import com.petlove.weblove.modules.rescue.enums.RescueUrgencyLevel;
import com.petlove.weblove.modules.rescue.repository.RescueClueMediaRepository;
import com.petlove.weblove.modules.rescue.repository.RescueClueRepository;
import com.petlove.weblove.modules.rescue.repository.RescueGuideRepository;
import com.petlove.weblove.modules.rescue.repository.RescueResourceRepository;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
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
public class AdminRescueService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private static final TypeReference<List<Long>> LONG_LIST_TYPE = new TypeReference<>() {
    };

    private final RescueGuideRepository rescueGuideRepository;
    private final RescueResourceRepository rescueResourceRepository;
    private final RescueClueRepository rescueClueRepository;
    private final RescueClueMediaRepository rescueClueMediaRepository;
    private final FileObjectRepository fileObjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final CityRepository cityRepository;
    private final AdminUserRepository adminUserRepository;
    private final AdminAuditService adminAuditService;
    private final ObjectMapper objectMapper;

    public AdminRescueService(RescueGuideRepository rescueGuideRepository,
                              RescueResourceRepository rescueResourceRepository,
                              RescueClueRepository rescueClueRepository,
                              RescueClueMediaRepository rescueClueMediaRepository,
                              FileObjectRepository fileObjectRepository,
                              UserProfileRepository userProfileRepository,
                              CityRepository cityRepository,
                              AdminUserRepository adminUserRepository,
                              AdminAuditService adminAuditService,
                              ObjectMapper objectMapper) {
        this.rescueGuideRepository = rescueGuideRepository;
        this.rescueResourceRepository = rescueResourceRepository;
        this.rescueClueRepository = rescueClueRepository;
        this.rescueClueMediaRepository = rescueClueMediaRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.userProfileRepository = userProfileRepository;
        this.cityRepository = cityRepository;
        this.adminUserRepository = adminUserRepository;
        this.adminAuditService = adminAuditService;
        this.objectMapper = objectMapper;
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public PageResponse<AdminRescueGuideListItemDTO> listGuides(AdminRescueGuideQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        RescueGuideStatus status = parseGuideStatus(query.getStatus(), false);
        RescueGuideScenarioCode scenarioCode = parseScenarioCode(query.getScenarioCode(), false);
        String cityCode = normalizeText(query.getCityCode());
        String keyword = normalizeText(query.getKeyword());

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt")
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<RescueGuide> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (scenarioCode != null) {
                predicates.add(cb.equal(root.get("scenarioCode"), scenarioCode));
            }
            if (cityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("summary")), like),
                    cb.like(cb.lower(root.get("contentMd")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<RescueGuide> guidePage = rescueGuideRepository.findAll(spec, pageable);
        if (guidePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdminRescueGuideListItemDTO> items = guidePage.getContent().stream()
            .map(guide -> new AdminRescueGuideListItemDTO(
                guide.getId(),
                guide.getScenarioCode() == null ? null : guide.getScenarioCode().name(),
                guide.getTitle(),
                guide.getCityCode(),
                guide.getStatus() == null ? null : guide.getStatus().name(),
                guide.getVersion(),
                guide.getPublishedAt(),
                guide.getUpdatedAt()
            ))
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, guidePage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public AdminRescueGuideDetailDTO guideDetail(Long guideId) {
        RescueGuide guide = rescueGuideRepository.findById(guideId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_GUIDE_NOT_FOUND, "Rescue guide not found"));
        return toAdminGuideDetailDTO(guide);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public AdminRescueGuideDetailDTO createGuide(UpsertRescueGuideRequest request) {
        return upsertGuideInternal(null, request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public AdminRescueGuideDetailDTO upsertGuide(Long guideId, UpsertRescueGuideRequest request) {
        return upsertGuideInternal(guideId, request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public void publishGuide(Long guideId) {
        long adminId = SecurityUtils.currentAdminId();
        RescueGuide guide = rescueGuideRepository.findById(guideId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_GUIDE_NOT_FOUND, "Rescue guide not found"));

        if (!isGuideStatusTransitionAllowed(guide.getStatus(), RescueGuideStatus.PUBLISHED)) {
            throw new BizException(ErrorCode.RESCUE_GUIDE_STATUS_INVALID, "Rescue guide status transition is invalid");
        }

        Map<String, Object> before = guideAuditSnapshot(guide);

        guide.setStatus(RescueGuideStatus.PUBLISHED);
        guide.setPublishedAt(LocalDateTime.now());
        guide.setUpdatedByAdminId(adminId);
        guide.setVersion((guide.getVersion() == null ? 1 : guide.getVersion()) + 1);
        rescueGuideRepository.save(guide);

        adminAuditService.writeAuditLog(
            adminId,
            "PUBLISH_RESCUE_GUIDE",
            "RESCUE_GUIDE",
            String.valueOf(guide.getId()),
            before,
            guideAuditSnapshot(guide),
            null
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public void offlineGuide(Long guideId) {
        long adminId = SecurityUtils.currentAdminId();
        RescueGuide guide = rescueGuideRepository.findById(guideId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_GUIDE_NOT_FOUND, "Rescue guide not found"));

        if (!isGuideStatusTransitionAllowed(guide.getStatus(), RescueGuideStatus.OFFLINE)) {
            throw new BizException(ErrorCode.RESCUE_GUIDE_STATUS_INVALID, "Rescue guide status transition is invalid");
        }

        Map<String, Object> before = guideAuditSnapshot(guide);

        guide.setStatus(RescueGuideStatus.OFFLINE);
        guide.setUpdatedByAdminId(adminId);
        guide.setVersion((guide.getVersion() == null ? 1 : guide.getVersion()) + 1);
        rescueGuideRepository.save(guide);

        adminAuditService.writeAuditLog(
            adminId,
            "OFFLINE_RESCUE_GUIDE",
            "RESCUE_GUIDE",
            String.valueOf(guide.getId()),
            before,
            guideAuditSnapshot(guide),
            null
        );
    }

    private AdminRescueGuideDetailDTO upsertGuideInternal(Long guideId, UpsertRescueGuideRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        RescueGuideScenarioCode scenarioCode = parseScenarioCode(request.getScenarioCode(), true);
        String title = normalizeRequiredText(request.getTitle(), "title");
        String contentMd = normalizeRequiredText(request.getContentMd(), "contentMd");
        String summary = normalizeText(request.getSummary());
        String cityCode = normalizeText(request.getCityCode());
        List<String> tags = normalizeStringList(request.getTags(), 32, 20);
        int sortOrder = request.getSortOrder() == null ? 0 : request.getSortOrder();

        if (cityCode != null) {
            ensureCityExists(cityCode);
        }

        RescueGuide guide;
        boolean create = guideId == null;
        if (create) {
            guide = new RescueGuide();
            guide.setCreatedByAdminId(adminId);
            guide.setVersion(1);
            guide.setStatus(RescueGuideStatus.DRAFT);
        } else {
            guide = rescueGuideRepository.findById(guideId)
                .orElseThrow(() -> new BizException(ErrorCode.RESCUE_GUIDE_NOT_FOUND, "Rescue guide not found"));
        }

        RescueGuideStatus targetStatus = parseGuideStatus(request.getStatus(), false);
        if (targetStatus == null) {
            targetStatus = guide.getStatus() == null ? RescueGuideStatus.DRAFT : guide.getStatus();
        }

        if (!isGuideStatusTransitionAllowed(guide.getStatus(), targetStatus)) {
            throw new BizException(ErrorCode.RESCUE_GUIDE_STATUS_INVALID, "Rescue guide status transition is invalid");
        }

        if (targetStatus == RescueGuideStatus.PUBLISHED) {
            ensureGuidePublishable(title, contentMd, scenarioCode);
        }

        Map<String, Object> before = create ? null : guideAuditSnapshot(guide);

        guide.setScenarioCode(scenarioCode);
        guide.setTitle(title);
        guide.setSummary(summary);
        guide.setContentMd(contentMd);
        guide.setCityCode(cityCode);
        guide.setTags(toJson(tags));
        guide.setSortOrder(sortOrder);
        guide.setStatus(targetStatus);
        if (targetStatus == RescueGuideStatus.PUBLISHED && guide.getPublishedAt() == null) {
            guide.setPublishedAt(LocalDateTime.now());
        }
        if (!create) {
            guide.setVersion((guide.getVersion() == null ? 1 : guide.getVersion()) + 1);
        }
        guide.setUpdatedByAdminId(adminId);

        RescueGuide saved = rescueGuideRepository.save(guide);

        adminAuditService.writeAuditLog(
            adminId,
            "UPSERT_RESCUE_GUIDE",
            "RESCUE_GUIDE",
            String.valueOf(saved.getId()),
            before,
            guideAuditSnapshot(saved),
            null
        );

        return toAdminGuideDetailDTO(saved);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public PageResponse<AdminRescueResourceListItemDTO> listResources(AdminRescueResourceQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        RescueResourceStatus status = parseResourceStatus(query.getStatus(), false);
        RescueResourceType resourceType = parseResourceType(query.getResourceType(), false);
        String cityCode = normalizeText(query.getCityCode());
        String keyword = normalizeText(query.getKeyword());

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt")
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<RescueResource> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (resourceType != null) {
                predicates.add(cb.equal(root.get("resourceType"), resourceType));
            }
            if (cityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("cityName")), like),
                    cb.like(cb.lower(root.get("districtName")), like),
                    cb.like(cb.lower(root.get("address")), like),
                    cb.like(cb.lower(root.get("serviceScope")), like)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<RescueResource> resourcePage = rescueResourceRepository.findAll(spec, pageable);
        if (resourcePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdminRescueResourceListItemDTO> items = resourcePage.getContent().stream()
            .map(resource -> new AdminRescueResourceListItemDTO(
                resource.getId(),
                resource.getResourceType() == null ? null : resource.getResourceType().name(),
                resource.getName(),
                resource.getCityName(),
                resource.getStatus() == null ? null : resource.getStatus().name(),
                resource.getVerifiedAt(),
                resource.getUpdatedAt()
            ))
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, resourcePage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional(readOnly = true)
    public AdminRescueResourceDetailDTO resourceDetail(Long resourceId) {
        RescueResource resource = rescueResourceRepository.findById(resourceId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_RESOURCE_NOT_FOUND, "Rescue resource not found"));
        return toAdminResourceDetailDTO(resource);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public AdminRescueResourceDetailDTO createResource(UpsertRescueResourceRequest request) {
        return upsertResourceInternal(null, request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public AdminRescueResourceDetailDTO upsertResource(Long resourceId, UpsertRescueResourceRequest request) {
        return upsertResourceInternal(resourceId, request);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public void activateResource(Long resourceId) {
        updateResourceStatus(resourceId, RescueResourceStatus.ACTIVE, "ACTIVATE_RESCUE_RESOURCE");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public void pauseResource(Long resourceId) {
        updateResourceStatus(resourceId, RescueResourceStatus.PAUSED, "PAUSE_RESCUE_RESOURCE");
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS')")
    @Transactional
    public void offlineResource(Long resourceId) {
        updateResourceStatus(resourceId, RescueResourceStatus.OFFLINE, "OFFLINE_RESCUE_RESOURCE");
    }

    private AdminRescueResourceDetailDTO upsertResourceInternal(Long resourceId, UpsertRescueResourceRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        RescueResourceType resourceType = parseResourceType(request.getResourceType(), true);
        String name = normalizeRequiredText(request.getName(), "name");
        String cityCode = normalizeRequiredText(request.getCityCode(), "cityCode");
        String cityName = normalizeRequiredText(request.getCityName(), "cityName");

        ensureCityExists(cityCode);

        RescueResource resource;
        boolean create = resourceId == null;
        if (create) {
            resource = new RescueResource();
            resource.setCreatedByAdminId(adminId);
            resource.setStatus(RescueResourceStatus.DRAFT);
        } else {
            resource = rescueResourceRepository.findById(resourceId)
                .orElseThrow(() -> new BizException(ErrorCode.RESCUE_RESOURCE_NOT_FOUND, "Rescue resource not found"));
        }

        RescueResourceStatus targetStatus = parseResourceStatus(request.getStatus(), false);
        if (targetStatus == null) {
            targetStatus = resource.getStatus() == null ? RescueResourceStatus.DRAFT : resource.getStatus();
        }

        if (!isResourceStatusTransitionAllowed(resource.getStatus(), targetStatus)) {
            throw new BizException(
                ErrorCode.RESCUE_RESOURCE_STATUS_INVALID,
                "Rescue resource status transition is invalid"
            );
        }

        Map<String, Object> before = create ? null : resourceAuditSnapshot(resource);

        resource.setResourceType(resourceType);
        resource.setName(name);
        resource.setCityCode(cityCode);
        resource.setCityName(cityName);
        resource.setDistrictName(normalizeText(request.getDistrictName()));
        resource.setAddress(normalizeText(request.getAddress()));
        resource.setContactPhone(normalizeText(request.getContactPhone()));
        resource.setContactWechat(normalizeText(request.getContactWechat()));
        resource.setContactOther(normalizeText(request.getContactOther()));
        resource.setServiceHours(normalizeText(request.getServiceHours()));
        resource.setServiceScope(normalizeText(request.getServiceScope()));
        resource.setAcceptPetTypes(toJson(normalizePetTypeList(request.getAcceptPetTypes())));
        resource.setCapabilityTags(toJson(normalizeStringList(request.getCapabilityTags(), 32, 20)));
        resource.setDescription(normalizeText(request.getDescription()));
        resource.setSourceUrl(normalizeText(request.getSourceUrl()));
        resource.setVerifiedAt(parseDateTime(request.getVerifiedAt(), false));
        resource.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        resource.setStatus(targetStatus);
        resource.setUpdatedByAdminId(adminId);

        if (targetStatus == RescueResourceStatus.ACTIVE) {
            ensureResourceActiveCompleteness(resource);
        }

        RescueResource saved = rescueResourceRepository.save(resource);

        adminAuditService.writeAuditLog(
            adminId,
            "UPSERT_RESCUE_RESOURCE",
            "RESCUE_RESOURCE",
            String.valueOf(saved.getId()),
            before,
            resourceAuditSnapshot(saved),
            null
        );

        return toAdminResourceDetailDTO(saved);
    }

    private void updateResourceStatus(Long resourceId, RescueResourceStatus targetStatus, String action) {
        long adminId = SecurityUtils.currentAdminId();

        RescueResource resource = rescueResourceRepository.findById(resourceId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_RESOURCE_NOT_FOUND, "Rescue resource not found"));

        if (!isResourceStatusTransitionAllowed(resource.getStatus(), targetStatus)) {
            throw new BizException(
                ErrorCode.RESCUE_RESOURCE_STATUS_INVALID,
                "Rescue resource status transition is invalid"
            );
        }

        if (targetStatus == RescueResourceStatus.ACTIVE) {
            ensureResourceActiveCompleteness(resource);
        }

        Map<String, Object> before = resourceAuditSnapshot(resource);

        resource.setStatus(targetStatus);
        resource.setUpdatedByAdminId(adminId);
        rescueResourceRepository.save(resource);

        adminAuditService.writeAuditLog(
            adminId,
            action,
            "RESCUE_RESOURCE",
            String.valueOf(resource.getId()),
            before,
            resourceAuditSnapshot(resource),
            null
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public PageResponse<AdminRescueClueListItemDTO> listClues(AdminRescueClueQuery query) {
        int page = query.getPage() == null ? 1 : query.getPage();
        int pageSize = query.getPageSize() == null ? 20 : query.getPageSize();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        RescueClueStatus status = parseClueStatus(query.getStatus(), false);
        RescueUrgencyLevel urgencyLevel = parseUrgencyLevel(query.getUrgencyLevel(), false);
        RescuePetType petType = parsePetType(query.getPetType(), false);
        String cityCode = normalizeText(query.getCityCode());
        String keyword = normalizeText(query.getKeyword());
        LocalDate dateFrom = query.getDateFrom();
        LocalDate dateTo = query.getDateTo();

        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "dateFrom cannot be greater than dateTo");
        }

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<RescueClue> spec = (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (urgencyLevel != null) {
                predicates.add(cb.equal(root.get("urgencyLevel"), urgencyLevel));
            }
            if (petType != null) {
                predicates.add(cb.equal(root.get("petType"), petType));
            }
            if (cityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), cityCode));
            }
            if (keyword != null) {
                String like = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("clueNo")), like),
                    cb.like(cb.lower(root.get("contactName")), like),
                    cb.like(cb.lower(root.get("contactMobile")), like),
                    cb.like(cb.lower(root.get("locationText")), like)
                ));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), dateFrom.atStartOfDay()));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), dateTo.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<RescueClue> cluePage = rescueClueRepository.findAll(spec, pageable);
        if (cluePage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        Set<Long> handledAdminIds = cluePage.getContent().stream()
            .map(RescueClue::getHandledByAdminId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        Map<Long, String> adminNameMap = adminUserRepository.findAllById(handledAdminIds).stream()
            .collect(Collectors.toMap(AdminUser::getId, AdminUser::getDisplayName));

        List<AdminRescueClueListItemDTO> items = cluePage.getContent().stream()
            .map(clue -> new AdminRescueClueListItemDTO(
                clue.getId(),
                clue.getClueNo(),
                clue.getCityName(),
                clue.getDistrictName(),
                clue.getPetType() == null ? null : clue.getPetType().name(),
                clue.getUrgencyLevel() == null ? null : clue.getUrgencyLevel().name(),
                clue.getStatus() == null ? null : clue.getStatus().name(),
                clue.getReporterUserId(),
                clue.getHandledByAdminId() == null ? null : adminNameMap.get(clue.getHandledByAdminId()),
                clue.getHandledAt(),
                clue.getCreatedAt()
            ))
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, cluePage.getTotalElements());
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_OPS','ROLE_CS')")
    @Transactional(readOnly = true)
    public AdminRescueClueDetailDTO clueDetail(Long clueId) {
        RescueClue clue = rescueClueRepository.findById(clueId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_CLUE_NOT_FOUND, "Rescue clue not found"));
        return toAdminClueDetailDTO(clue);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_AUDITOR','ROLE_CS')")
    @Transactional
    public AdminRescueClueDetailDTO updateClueStatus(Long clueId, UpdateRescueClueStatusRequest request) {
        long adminId = SecurityUtils.currentAdminId();

        RescueClue clue = rescueClueRepository.findById(clueId)
            .orElseThrow(() -> new BizException(ErrorCode.RESCUE_CLUE_NOT_FOUND, "Rescue clue not found"));

        RescueClueStatus targetStatus = parseClueStatus(request.getStatus(), true);
        if (targetStatus == RescueClueStatus.SUBMITTED) {
            throw new BizException(ErrorCode.RESCUE_CLUE_STATUS_INVALID, "Target status cannot be SUBMITTED");
        }

        if (!isClueStatusTransitionAllowed(clue.getStatus(), targetStatus)) {
            throw new BizException(ErrorCode.RESCUE_CLUE_STATUS_INVALID, "Rescue clue status transition is invalid");
        }

        List<Long> suggestedResourceIds = null;
        if (request.getSuggestedResourceIds() != null) {
            suggestedResourceIds = normalizeResourceIds(request.getSuggestedResourceIds(), 10);
            validateSuggestedResources(clue.getCityCode(), suggestedResourceIds);
        }

        Map<String, Object> before = clueAuditSnapshot(clue);

        clue.setStatus(targetStatus);
        if (request.getTriageNote() != null) {
            clue.setTriageNote(normalizeText(request.getTriageNote()));
        }
        if (request.getResolutionNote() != null) {
            clue.setResolutionNote(normalizeText(request.getResolutionNote()));
        }
        if (suggestedResourceIds != null) {
            clue.setSuggestedResourceIds(toJson(suggestedResourceIds));
        }
        clue.setHandledByAdminId(adminId);
        clue.setHandledAt(LocalDateTime.now());

        RescueClue saved = rescueClueRepository.save(clue);

        adminAuditService.writeAuditLog(
            adminId,
            "UPDATE_RESCUE_CLUE_STATUS",
            "RESCUE_CLUE",
            String.valueOf(saved.getId()),
            before,
            clueAuditSnapshot(saved),
            null
        );

        return toAdminClueDetailDTO(saved);
    }

    private void validateSuggestedResources(String cityCode, List<Long> resourceIds) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return;
        }

        List<RescueResource> resources = rescueResourceRepository.findByIdIn(resourceIds);
        if (resources.size() != resourceIds.size()) {
            throw new BizException(
                ErrorCode.RESCUE_CLUE_SUGGESTED_RESOURCE_INVALID,
                "Suggested resource ids contain invalid value"
            );
        }

        for (RescueResource resource : resources) {
            if (!Objects.equals(cityCode, resource.getCityCode())) {
                throw new BizException(
                    ErrorCode.RESCUE_CLUE_SUGGESTED_RESOURCE_INVALID,
                    "Suggested resource must be in the same city"
                );
            }
        }
    }

    private AdminRescueGuideDetailDTO toAdminGuideDetailDTO(RescueGuide guide) {
        return new AdminRescueGuideDetailDTO(
            guide.getId(),
            guide.getScenarioCode() == null ? null : guide.getScenarioCode().name(),
            guide.getTitle(),
            guide.getSummary(),
            guide.getContentMd(),
            guide.getCityCode(),
            fromJsonStringList(guide.getTags()),
            guide.getSortOrder(),
            guide.getStatus() == null ? null : guide.getStatus().name(),
            guide.getVersion(),
            guide.getPublishedAt(),
            guide.getCreatedByAdminId(),
            guide.getUpdatedByAdminId(),
            guide.getCreatedAt(),
            guide.getUpdatedAt()
        );
    }

    private AdminRescueResourceDetailDTO toAdminResourceDetailDTO(RescueResource resource) {
        return new AdminRescueResourceDetailDTO(
            resource.getId(),
            resource.getResourceType() == null ? null : resource.getResourceType().name(),
            resource.getName(),
            resource.getCityCode(),
            resource.getCityName(),
            resource.getDistrictName(),
            resource.getAddress(),
            resource.getContactPhone(),
            resource.getContactWechat(),
            resource.getContactOther(),
            resource.getServiceHours(),
            resource.getServiceScope(),
            fromJsonStringList(resource.getAcceptPetTypes()),
            fromJsonStringList(resource.getCapabilityTags()),
            resource.getDescription(),
            resource.getSourceUrl(),
            resource.getVerifiedAt(),
            resource.getSortOrder(),
            resource.getStatus() == null ? null : resource.getStatus().name(),
            resource.getCreatedByAdminId(),
            resource.getUpdatedByAdminId(),
            resource.getCreatedAt(),
            resource.getUpdatedAt()
        );
    }

    private AdminRescueClueDetailDTO toAdminClueDetailDTO(RescueClue clue) {
        UserProfile reporterProfile = userProfileRepository.findByUserId(clue.getReporterUserId()).orElse(null);
        String handledByAdminName = clue.getHandledByAdminId() == null
            ? null
            : adminUserRepository.findById(clue.getHandledByAdminId()).map(AdminUser::getDisplayName).orElse(null);

        List<RescueClueMedia> mediaList = rescueClueMediaRepository.findByClueIdOrderBySortOrderAsc(clue.getId());
        Map<Long, FileObject> fileMap = toFileMap(mediaList.stream().map(RescueClueMedia::getFileObjectId).toList());

        List<RescueCluePhotoDTO> photos = mediaList.stream().map(media -> {
            FileObject fileObject = fileMap.get(media.getFileObjectId());
            return new RescueCluePhotoDTO(
                media.getFileObjectId(),
                fileObject != null ? fileObject.getPublicUrl() : null,
                media.getSortOrder()
            );
        }).toList();

        return new AdminRescueClueDetailDTO(
            clue.getId(),
            clue.getClueNo(),
            clue.getReporterUserId(),
            reporterProfile != null ? reporterProfile.getNickname() : null,
            clue.getCityCode(),
            clue.getCityName(),
            clue.getDistrictName(),
            clue.getLocationText(),
            clue.getGeoLat(),
            clue.getGeoLng(),
            clue.getPetType() == null ? null : clue.getPetType().name(),
            clue.getEstimatedCount(),
            clue.getUrgencyLevel() == null ? null : clue.getUrgencyLevel().name(),
            fromJsonStringList(clue.getConditionTags()),
            clue.getDescription(),
            clue.getContactName(),
            clue.getContactMobile(),
            clue.getStatus() == null ? null : clue.getStatus().name(),
            clue.getTriageNote(),
            clue.getResolutionNote(),
            fromJsonLongList(clue.getSuggestedResourceIds()),
            photos,
            handledByAdminName,
            clue.getHandledAt(),
            clue.getCreatedAt(),
            clue.getUpdatedAt()
        );
    }

    private Map<String, Object> guideAuditSnapshot(RescueGuide guide) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", guide.getId());
        data.put("scenarioCode", guide.getScenarioCode() == null ? null : guide.getScenarioCode().name());
        data.put("title", guide.getTitle());
        data.put("cityCode", guide.getCityCode());
        data.put("status", guide.getStatus() == null ? null : guide.getStatus().name());
        data.put("version", guide.getVersion());
        data.put("publishedAt", guide.getPublishedAt());
        return data;
    }

    private Map<String, Object> resourceAuditSnapshot(RescueResource resource) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", resource.getId());
        data.put("resourceType", resource.getResourceType() == null ? null : resource.getResourceType().name());
        data.put("name", resource.getName());
        data.put("cityCode", resource.getCityCode());
        data.put("status", resource.getStatus() == null ? null : resource.getStatus().name());
        data.put("verifiedAt", resource.getVerifiedAt());
        return data;
    }

    private Map<String, Object> clueAuditSnapshot(RescueClue clue) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", clue.getId());
        data.put("clueNo", clue.getClueNo());
        data.put("status", clue.getStatus() == null ? null : clue.getStatus().name());
        data.put("triageNote", clue.getTriageNote());
        data.put("resolutionNote", clue.getResolutionNote());
        data.put("suggestedResourceIds", clue.getSuggestedResourceIds());
        data.put("handledByAdminId", clue.getHandledByAdminId());
        data.put("handledAt", clue.getHandledAt());
        return data;
    }

    private boolean isGuideStatusTransitionAllowed(RescueGuideStatus current, RescueGuideStatus target) {
        if (target == null) {
            return false;
        }
        if (current == target) {
            return true;
        }
        if (current == null) {
            return target == RescueGuideStatus.DRAFT;
        }
        return switch (current) {
            case DRAFT -> target == RescueGuideStatus.PUBLISHED;
            case PUBLISHED -> target == RescueGuideStatus.OFFLINE;
            case OFFLINE -> target == RescueGuideStatus.PUBLISHED;
        };
    }

    private boolean isResourceStatusTransitionAllowed(RescueResourceStatus current, RescueResourceStatus target) {
        if (target == null) {
            return false;
        }
        if (current == target) {
            return true;
        }
        if (current == null) {
            return target == RescueResourceStatus.DRAFT;
        }
        return switch (current) {
            case DRAFT -> target == RescueResourceStatus.ACTIVE;
            case ACTIVE -> target == RescueResourceStatus.PAUSED || target == RescueResourceStatus.OFFLINE;
            case PAUSED -> target == RescueResourceStatus.ACTIVE || target == RescueResourceStatus.OFFLINE;
            case OFFLINE -> false;
        };
    }

    private boolean isClueStatusTransitionAllowed(RescueClueStatus current, RescueClueStatus target) {
        if (current == null || target == null || current == target) {
            return false;
        }
        return switch (current) {
            case SUBMITTED -> target == RescueClueStatus.TRIAGED;
            case TRIAGED -> target == RescueClueStatus.IN_PROGRESS
                || target == RescueClueStatus.RESOLVED
                || target == RescueClueStatus.CLOSED
                || target == RescueClueStatus.INVALID;
            case IN_PROGRESS -> target == RescueClueStatus.RESOLVED
                || target == RescueClueStatus.CLOSED
                || target == RescueClueStatus.INVALID;
            case RESOLVED, CLOSED, INVALID -> false;
        };
    }

    private void ensureGuidePublishable(String title,
                                        String contentMd,
                                        RescueGuideScenarioCode scenarioCode) {
        if (scenarioCode == null || title == null || contentMd == null) {
            throw new BizException(ErrorCode.RESCUE_GUIDE_STATUS_INVALID, "Guide is incomplete for PUBLISHED status");
        }
    }

    private void ensureResourceActiveCompleteness(RescueResource resource) {
        boolean hasContact = normalizeText(resource.getContactPhone()) != null
            || normalizeText(resource.getContactWechat()) != null
            || normalizeText(resource.getContactOther()) != null;
        boolean hasAddress = normalizeText(resource.getAddress()) != null;

        if (normalizeText(resource.getName()) == null
            || normalizeText(resource.getCityCode()) == null
            || (!hasContact && !hasAddress)) {
            throw new BizException(
                ErrorCode.RESCUE_RESOURCE_STATUS_INVALID,
                "Resource is incomplete for ACTIVE status"
            );
        }
    }

    private void ensureCityExists(String cityCode) {
        City city = cityRepository.findByCityCode(cityCode)
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "cityCode is invalid"));
        if (!city.isEnabled()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "city is disabled");
        }
    }

    private RescueGuideScenarioCode parseScenarioCode(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "scenarioCode is required");
            }
            return null;
        }
        try {
            return RescueGuideScenarioCode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "scenarioCode is invalid");
        }
    }

    private RescueGuideStatus parseGuideStatus(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return RescueGuideStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.RESCUE_GUIDE_STATUS_INVALID, "Rescue guide status is invalid");
        }
    }

    private RescueResourceType parseResourceType(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "resourceType is required");
            }
            return null;
        }
        try {
            return RescueResourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "resourceType is invalid");
        }
    }

    private RescueResourceStatus parseResourceStatus(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return RescueResourceStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(
                ErrorCode.RESCUE_RESOURCE_STATUS_INVALID,
                "Rescue resource status is invalid"
            );
        }
    }

    private RescueClueStatus parseClueStatus(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return RescueClueStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.RESCUE_CLUE_STATUS_INVALID, "Rescue clue status is invalid");
        }
    }

    private RescueUrgencyLevel parseUrgencyLevel(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "urgencyLevel is required");
            }
            return null;
        }
        try {
            return RescueUrgencyLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "urgencyLevel is invalid");
        }
    }

    private RescuePetType parsePetType(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petType is required");
            }
            return null;
        }
        try {
            return RescuePetType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petType is invalid");
        }
    }

    private List<Long> normalizeResourceIds(List<Long> resourceIds, int maxCount) {
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyList();
        }
        if (resourceIds.size() > maxCount) {
            throw new BizException(ErrorCode.INVALID_PARAM, "suggestedResourceIds size exceeds limit");
        }

        List<Long> normalized = resourceIds.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();

        if (normalized.size() != resourceIds.size()) {
            throw new BizException(
                ErrorCode.RESCUE_CLUE_SUGGESTED_RESOURCE_INVALID,
                "suggestedResourceIds contains invalid value"
            );
        }
        return normalized;
    }

    private List<String> normalizeStringList(List<String> values, int maxItemLength, int maxCount) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String text = normalizeText(value);
            if (text == null) {
                throw new BizException(ErrorCode.INVALID_PARAM, "list contains blank value");
            }
            if (text.length() > maxItemLength) {
                throw new BizException(ErrorCode.INVALID_PARAM, "list item is too long");
            }
            normalized.add(text);
        }

        if (normalized.size() > maxCount) {
            throw new BizException(ErrorCode.INVALID_PARAM, "list size exceeds limit");
        }
        return new ArrayList<>(normalized);
    }

    private List<String> normalizePetTypeList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String value : values) {
            String text = normalizeText(value);
            if (text == null) {
                throw new BizException(ErrorCode.INVALID_PARAM, "acceptPetTypes contains blank value");
            }
            String upper = text.toUpperCase(Locale.ROOT);
            try {
                RescuePetType.valueOf(upper);
            } catch (IllegalArgumentException ex) {
                throw new BizException(ErrorCode.INVALID_PARAM, "acceptPetTypes contains invalid value");
            }
            normalized.add(upper);
        }
        return new ArrayList<>(normalized);
    }

    private LocalDateTime parseDateTime(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "datetime value is required");
            }
            return null;
        }
        String normalized = value.trim();
        try {
            return LocalDateTime.parse(normalized);
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(normalized).atStartOfDay();
            } catch (DateTimeParseException ex) {
                throw new BizException(ErrorCode.INVALID_PARAM, "datetime value is invalid");
            }
        }
    }

    private List<String> fromJsonStringList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private List<Long> fromJsonLongList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Long> values = objectMapper.readValue(json, LONG_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
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
            throw new BizException(ErrorCode.INTERNAL_ERROR, "JSON serialization failed");
        }
    }

    private Map<Long, FileObject> toFileMap(Collection<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return fileObjectRepository.findAllById(fileIds).stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));
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
