package com.petlove.weblove.modules.adoption.service;

import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.dto.user.AdoptionApplicationDTO;
import com.petlove.weblove.modules.adoption.dto.user.ApplicantSummaryDTO;
import com.petlove.weblove.modules.adoption.dto.user.HandleAdoptionApplicationRequest;
import com.petlove.weblove.modules.adoption.dto.user.MyAdoptionApplicationListItemDTO;
import com.petlove.weblove.modules.adoption.dto.user.SubmitAdoptionApplicationRequest;
import com.petlove.weblove.modules.adoption.entity.AdoptionApplication;
import com.petlove.weblove.modules.adoption.entity.AdoptionPost;
import com.petlove.weblove.modules.adoption.entity.PetMedia;
import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationHandleAction;
import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationStatus;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import com.petlove.weblove.modules.adoption.repository.AdoptionApplicationRepository;
import com.petlove.weblove.modules.adoption.repository.AdoptionPostRepository;
import com.petlove.weblove.modules.adoption.repository.PetMediaRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.risk.RiskActionKeys;
import com.petlove.weblove.modules.risk.RiskGuard;
import com.petlove.weblove.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdoptionApplicationService {

    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final AdoptionPostRepository adoptionPostRepository;
    private final PetMediaRepository petMediaRepository;
    private final FileObjectRepository fileObjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final RiskGuard riskGuard;

    public AdoptionApplicationService(AdoptionApplicationRepository adoptionApplicationRepository,
                                      AdoptionPostRepository adoptionPostRepository,
                                      PetMediaRepository petMediaRepository,
                                      FileObjectRepository fileObjectRepository,
                                      UserProfileRepository userProfileRepository,
                                      RiskGuard riskGuard) {
        this.adoptionApplicationRepository = adoptionApplicationRepository;
        this.adoptionPostRepository = adoptionPostRepository;
        this.petMediaRepository = petMediaRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.userProfileRepository = userProfileRepository;
        this.riskGuard = riskGuard;
    }

    @Transactional
    public AdoptionApplicationDTO submit(Long postId, SubmitAdoptionApplicationRequest request) {
        long userId = SecurityUtils.currentUserId();

        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        if (Objects.equals(post.getPublisherUserId(), userId)) {
            throw new BizException(ErrorCode.ADOPTION_CANNOT_APPLY_OWN_POST, "Cannot apply to own post");
        }

        if (post.getStatus() != AdoptionPostStatus.PUBLISHED) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_NOT_ALLOWED, "Only published post can be applied");
        }
        riskGuard.ensureUserActionAllowed(
            userId,
            post.getCityCode(),
            RiskActionKeys.ADOPTION_APPLICATION_SUBMIT,
            CityFeatureKey.ADOPTION
        );

        if (adoptionApplicationRepository.findByPostIdAndApplicantUserId(postId, userId).isPresent()) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_DUPLICATE, "Application already exists");
        }

        AdoptionApplication application = new AdoptionApplication();
        application.setPostId(postId);
        application.setApplicantUserId(userId);
        application.setMessage(normalizeRequiredText(request.getMessage()));
        application.setLivingEnvNote(normalizeText(request.getLivingEnvNote()));
        application.setPetExperienceNote(normalizeText(request.getPetExperienceNote()));
        application.setStatus(AdoptionApplicationStatus.SUBMITTED);

        try {
            application = adoptionApplicationRepository.save(application);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_DUPLICATE, "Application already exists");
        }

        return toDto(application, null);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyAdoptionApplicationListItemDTO> myApplications(int page, int pageSize, String status) {
        long userId = SecurityUtils.currentUserId();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        AdoptionApplicationStatus parsedStatus = parseApplicationStatus(status, false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<AdoptionApplication> applicationPage = parsedStatus == null
            ? adoptionApplicationRepository.findByApplicantUserId(userId, pageable)
            : adoptionApplicationRepository.findByApplicantUserIdAndStatus(userId, parsedStatus, pageable);

        if (applicationPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdoptionApplication> applications = applicationPage.getContent();
        Map<Long, AdoptionPost> postMap = adoptionPostRepository.findAllById(
                applications.stream().map(AdoptionApplication::getPostId).toList())
            .stream()
            .collect(Collectors.toMap(AdoptionPost::getId, Function.identity()));

        Map<Long, String> coverImageMap = resolveCoverImageMap(
            postMap.values().stream().map(AdoptionPost::getPetId).toList()
        );

        List<MyAdoptionApplicationListItemDTO> items = new ArrayList<>();
        for (AdoptionApplication application : applications) {
            AdoptionPost post = postMap.get(application.getPostId());
            items.add(new MyAdoptionApplicationListItemDTO(
                application.getId(),
                application.getPostId(),
                post != null ? post.getTitle() : "",
                post != null ? coverImageMap.get(post.getPetId()) : null,
                application.getStatus().name(),
                post != null ? post.getCityName() : "",
                application.getCreatedAt(),
                application.getHandledAt()
            ));
        }

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, applicationPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PageResponse<AdoptionApplicationDTO> postApplications(Long postId, int page, int pageSize) {
        long userId = SecurityUtils.currentUserId();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        if (!Objects.equals(post.getPublisherUserId(), userId)) {
            throw new BizException(ErrorCode.ADOPTION_POST_NOT_OWNER, "Adoption post is not owned by current user");
        }

        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<AdoptionApplication> applicationPage = adoptionApplicationRepository.findByPostId(postId, pageable);
        if (applicationPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(
                applicationPage.getContent().stream().map(AdoptionApplication::getApplicantUserId).toList())
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<AdoptionApplicationDTO> items = applicationPage.getContent().stream()
            .map(application -> toDto(application, profileMap.get(application.getApplicantUserId())))
            .toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, applicationPage.getTotalElements());
    }

    @Transactional
    public AdoptionApplicationDTO handle(Long applicationId, HandleAdoptionApplicationRequest request) {
        long userId = SecurityUtils.currentUserId();

        AdoptionApplication application = adoptionApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_APPLICATION_NOT_FOUND, "Adoption application not found"));

        AdoptionPost post = adoptionPostRepository.findById(application.getPostId())
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        if (!Objects.equals(post.getPublisherUserId(), userId)) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_NOT_OWNER, "Adoption application cannot be handled by current user");
        }
        if (application.getStatus() != AdoptionApplicationStatus.SUBMITTED) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_HANDLE_NOT_ALLOWED, "Only submitted application can be handled");
        }
        if (post.getStatus() != AdoptionPostStatus.PUBLISHED) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_HANDLE_NOT_ALLOWED, "Post status does not allow application handling");
        }

        LocalDateTime now = LocalDateTime.now();
        application.setHandledByUserId(userId);
        application.setHandledAt(now);
        application.setDecisionNote(normalizeText(request.getDecisionNote()));

        if (request.getAction() == AdoptionApplicationHandleAction.ACCEPT) {
            application.setStatus(AdoptionApplicationStatus.ACCEPTED);

            post.setStatus(AdoptionPostStatus.CLOSED);
            post.setClosedAt(now);
            adoptionPostRepository.save(post);

            List<AdoptionApplication> others = adoptionApplicationRepository.findByPostIdAndStatus(
                application.getPostId(),
                AdoptionApplicationStatus.SUBMITTED
            );

            for (AdoptionApplication other : others) {
                if (Objects.equals(other.getId(), application.getId())) {
                    continue;
                }
                other.setStatus(AdoptionApplicationStatus.REJECTED);
                other.setHandledByUserId(userId);
                other.setHandledAt(now);
                other.setDecisionNote("Rejected because another application was accepted");
            }
            adoptionApplicationRepository.saveAll(others);
        } else {
            application.setStatus(AdoptionApplicationStatus.REJECTED);
        }

        application = adoptionApplicationRepository.save(application);
        return toDto(application, null);
    }

    @Transactional
    public AdoptionApplicationDTO withdraw(Long applicationId) {
        long userId = SecurityUtils.currentUserId();

        AdoptionApplication application = adoptionApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_APPLICATION_NOT_FOUND, "Adoption application not found"));

        if (!Objects.equals(application.getApplicantUserId(), userId)) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_NOT_OWNER, "Adoption application is not owned by current user");
        }
        if (application.getStatus() != AdoptionApplicationStatus.SUBMITTED) {
            throw new BizException(ErrorCode.ADOPTION_APPLICATION_HANDLE_NOT_ALLOWED, "Only submitted application can be withdrawn");
        }

        application.setStatus(AdoptionApplicationStatus.WITHDRAWN);
        application.setHandledByUserId(userId);
        application.setHandledAt(LocalDateTime.now());
        application = adoptionApplicationRepository.save(application);

        return toDto(application, null);
    }

    private AdoptionApplicationDTO toDto(AdoptionApplication application, UserProfile applicantProfile) {
        ApplicantSummaryDTO applicant = null;
        if (applicantProfile != null) {
            applicant = new ApplicantSummaryDTO(
                applicantProfile.getUserId(),
                applicantProfile.getNickname(),
                applicantProfile.getAvatarUrl(),
                applicantProfile.isRealNameVerified()
            );
        }

        return new AdoptionApplicationDTO(
            application.getId(),
            application.getPostId(),
            applicant,
            application.getMessage(),
            application.getLivingEnvNote(),
            application.getPetExperienceNote(),
            application.getStatus().name(),
            application.getCreatedAt(),
            application.getHandledAt(),
            application.getDecisionNote()
        );
    }

    private Map<Long, String> resolveCoverImageMap(Collection<Long> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, PetMedia> firstMediaByPetId = new HashMap<>();
        for (PetMedia media : petMediaRepository.findByPetIdInOrderByPetIdAscSortOrderAsc(new LinkedHashSet<>(petIds))) {
            firstMediaByPetId.putIfAbsent(media.getPetId(), media);
        }

        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(
                firstMediaByPetId.values().stream().map(PetMedia::getFileObjectId).toList())
            .stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));

        Map<Long, String> result = new HashMap<>();
        for (Map.Entry<Long, PetMedia> entry : firstMediaByPetId.entrySet()) {
            FileObject fileObject = fileMap.get(entry.getValue().getFileObjectId());
            if (fileObject != null) {
                result.put(entry.getKey(), fileObject.getPublicUrl());
            }
        }

        return result;
    }

    private AdoptionApplicationStatus parseApplicationStatus(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return AdoptionApplicationStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private String normalizeRequiredText(String value) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new BizException(ErrorCode.INVALID_PARAM, "Required text is missing");
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
