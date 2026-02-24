package com.petlove.weblove.modules.adoption.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.dto.user.ApplicationStatsDTO;
import com.petlove.weblove.modules.adoption.dto.user.CreateRehomePostRequest;
import com.petlove.weblove.modules.adoption.dto.user.MyRehomePostListItemDTO;
import com.petlove.weblove.modules.adoption.dto.user.PetMediaDTO;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostDetailDTO;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostListItemDTO;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostPetDTO;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostPublisherDTO;
import com.petlove.weblove.modules.adoption.dto.user.ViewerContextDTO;
import com.petlove.weblove.modules.adoption.entity.AdoptionApplication;
import com.petlove.weblove.modules.adoption.entity.AdoptionPost;
import com.petlove.weblove.modules.adoption.entity.Pet;
import com.petlove.weblove.modules.adoption.entity.PetMedia;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import com.petlove.weblove.modules.adoption.enums.PetMediaType;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.repository.AdoptionApplicationRepository;
import com.petlove.weblove.modules.adoption.repository.AdoptionPostRepository;
import com.petlove.weblove.modules.adoption.repository.PetMediaRepository;
import com.petlove.weblove.modules.adoption.repository.PetRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.AuthPrincipal;
import com.petlove.weblove.security.AuthPrincipalType;
import com.petlove.weblove.security.SecurityUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdoptionPostService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final AdoptionPostRepository adoptionPostRepository;
    private final PetRepository petRepository;
    private final PetMediaRepository petMediaRepository;
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final FileObjectRepository fileObjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final CityRepository cityRepository;
    private final ObjectMapper objectMapper;

    public AdoptionPostService(AdoptionPostRepository adoptionPostRepository,
                               PetRepository petRepository,
                               PetMediaRepository petMediaRepository,
                               AdoptionApplicationRepository adoptionApplicationRepository,
                               FileObjectRepository fileObjectRepository,
                               UserProfileRepository userProfileRepository,
                               CityRepository cityRepository,
                               ObjectMapper objectMapper) {
        this.adoptionPostRepository = adoptionPostRepository;
        this.petRepository = petRepository;
        this.petMediaRepository = petMediaRepository;
        this.adoptionApplicationRepository = adoptionApplicationRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.userProfileRepository = userProfileRepository;
        this.cityRepository = cityRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<RehomePostListItemDTO> listPublishedPosts(int page,
                                                                  int pageSize,
                                                                  String cityCode,
                                                                  String petType,
                                                                  String keyword) {
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);

        PetType parsedPetType = parsePetType(petType, false);
        String normalizedCityCode = normalizeText(cityCode);
        String normalizedKeyword = normalizeText(keyword);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "publishedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Specification<AdoptionPost> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), AdoptionPostStatus.PUBLISHED));

            if (normalizedCityCode != null) {
                predicates.add(cb.equal(root.get("cityCode"), normalizedCityCode));
            }

            if (normalizedKeyword != null) {
                String like = "%" + normalizedKeyword.toLowerCase() + "%";
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("title")), like),
                    cb.like(cb.lower(root.get("content")), like)
                ));
            }

            if (parsedPetType != null && query != null) {
                predicates.add(root.get("petId").in(buildPetTypeSubquery(query, root, cb, parsedPetType)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };

        Page<AdoptionPost> postPage = adoptionPostRepository.findAll(spec, pageable);
        if (postPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdoptionPost> posts = postPage.getContent();
        Map<Long, Pet> petMap = petRepository.findAllById(posts.stream().map(AdoptionPost::getPetId).toList())
            .stream()
            .collect(Collectors.toMap(Pet::getId, Function.identity()));
        Map<Long, String> coverImageMap = resolveCoverImageMap(posts.stream().map(AdoptionPost::getPetId).toList());

        List<RehomePostListItemDTO> items = posts.stream().map(post -> {
            Pet pet = petMap.get(post.getPetId());
            return new RehomePostListItemDTO(
                post.getId(),
                post.getTitle(),
                post.getCityCode(),
                post.getCityName(),
                post.getDistrictName(),
                pet != null && pet.getPetType() != null ? pet.getPetType().name() : null,
                pet != null ? pet.getName() : null,
                pet != null && pet.getGender() != null ? pet.getGender().name() : null,
                pet != null ? pet.getAgeMonths() : null,
                pet != null ? pet.getBreed() : null,
                coverImageMap.get(post.getPetId()),
                pet != null ? fromJsonList(pet.getTemperamentTags()) : null,
                post.getPublishedAt(),
                post.getViewCount() == null ? 0 : post.getViewCount()
            );
        }).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, postPage.getTotalElements());
    }

    @Transactional
    public RehomePostDetailDTO detail(Long postId) {
        Long viewerUserId = currentUserIdOrNull();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        boolean isOwner = viewerUserId != null && Objects.equals(post.getPublisherUserId(), viewerUserId);
        if (!isOwner && post.getStatus() != AdoptionPostStatus.PUBLISHED) {
            throw new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found");
        }

        return buildDetail(post, viewerUserId, true);
    }

    @Transactional
    public RehomePostDetailDTO create(CreateRehomePostRequest request) {
        long userId = SecurityUtils.currentUserId();
        ensureRealNameVerified(userId);
        validateCity(request.getCityCode());

        List<Long> imageFileIds = normalizeFileIds(request.getPetImageFileIds());
        validatePetImageFiles(imageFileIds, userId);

        Pet pet = new Pet();
        pet.setOwnerUserId(userId);
        pet.setPetType(request.getPetType());
        pet.setName(normalizeText(request.getPetName()));
        pet.setGender(request.getPetGender());
        pet.setAgeMonths(request.getAgeMonths());
        pet.setBreed(normalizeText(request.getBreed()));
        pet.setWeightKg(request.getWeightKg());
        pet.setNeuteredStatus(request.getNeuteredStatus());
        pet.setVaccinatedStatus(request.getVaccinatedStatus());
        pet.setHealthNote(normalizeText(request.getHealthNote()));
        pet.setTemperamentTags(toJson(normalizeTags(request.getTemperamentTags())));
        pet.setSpecialCareNote(normalizeText(request.getSpecialCareNote()));
        pet = petRepository.save(pet);

        List<PetMedia> mediaList = new ArrayList<>();
        for (int i = 0; i < imageFileIds.size(); i++) {
            PetMedia media = new PetMedia();
            media.setPetId(pet.getId());
            media.setFileObjectId(imageFileIds.get(i));
            media.setMediaType(PetMediaType.IMAGE);
            media.setSortOrder(i);
            mediaList.add(media);
        }
        petMediaRepository.saveAll(mediaList);

        AdoptionPost post = new AdoptionPost();
        post.setPublisherUserId(userId);
        post.setPetId(pet.getId());
        post.setTitle(normalizeRequiredText(request.getTitle()));
        post.setContent(normalizeRequiredText(request.getContent()));
        post.setCityCode(normalizeRequiredText(request.getCityCode()));
        post.setCityName(normalizeRequiredText(request.getCityName()));
        post.setDistrictName(normalizeText(request.getDistrictName()));
        post.setStatus(AdoptionPostStatus.PENDING_REVIEW);
        post.setSubmitVersion(1);
        post.setViewCount(0);
        post = adoptionPostRepository.save(post);

        return buildDetail(post, userId, false);
    }

    @Transactional(readOnly = true)
    public PageResponse<MyRehomePostListItemDTO> myPosts(int page, int pageSize, String status) {
        long userId = SecurityUtils.currentUserId();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        AdoptionPostStatus parsedStatus = parsePostStatus(status, false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<AdoptionPost> postPage = parsedStatus == null
            ? adoptionPostRepository.findByPublisherUserId(userId, pageable)
            : adoptionPostRepository.findByPublisherUserIdAndStatus(userId, parsedStatus, pageable);

        if (postPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<AdoptionPost> posts = postPage.getContent();
        List<Long> postIds = posts.stream().map(AdoptionPost::getId).toList();
        Map<Long, String> coverImageMap = resolveCoverImageMap(posts.stream().map(AdoptionPost::getPetId).toList());

        Map<Long, Integer> applicationCountMap = adoptionApplicationRepository.findByPostIdIn(postIds).stream()
            .collect(Collectors.groupingBy(
                AdoptionApplication::getPostId,
                Collectors.collectingAndThen(Collectors.counting(), Math::toIntExact)
            ));

        List<MyRehomePostListItemDTO> items = posts.stream().map(post -> new MyRehomePostListItemDTO(
            post.getId(),
            post.getTitle(),
            post.getStatus().name(),
            coverImageMap.get(post.getPetId()),
            post.getCityName(),
            applicationCountMap.getOrDefault(post.getId(), 0),
            post.getRejectReasonText(),
            post.getUpdatedAt(),
            post.getPublishedAt()
        )).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, postPage.getTotalElements());
    }

    @Transactional
    public RehomePostDetailDTO resubmit(Long postId) {
        long userId = SecurityUtils.currentUserId();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        ensurePostOwner(post, userId);
        ensureRealNameVerified(userId);

        if (post.getStatus() != AdoptionPostStatus.REJECTED) {
            throw new BizException(ErrorCode.ADOPTION_POST_STATUS_INVALID, "Only rejected post can be resubmitted");
        }

        post.setStatus(AdoptionPostStatus.PENDING_REVIEW);
        post.setSubmitVersion((post.getSubmitVersion() == null ? 1 : post.getSubmitVersion()) + 1);
        post.setRejectReasonCode(null);
        post.setRejectReasonText(null);
        post.setReviewedByAdminId(null);
        post.setReviewedAt(null);
        adoptionPostRepository.save(post);

        return buildDetail(post, userId, false);
    }

    @Transactional
    public RehomePostDetailDTO close(Long postId) {
        long userId = SecurityUtils.currentUserId();
        AdoptionPost post = adoptionPostRepository.findById(postId)
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Adoption post not found"));

        ensurePostOwner(post, userId);

        if (post.getStatus() != AdoptionPostStatus.PUBLISHED) {
            throw new BizException(ErrorCode.ADOPTION_POST_STATUS_INVALID, "Only published post can be closed");
        }

        post.setStatus(AdoptionPostStatus.CLOSED);
        post.setClosedAt(java.time.LocalDateTime.now());
        adoptionPostRepository.save(post);

        return buildDetail(post, userId, false);
    }

    private RehomePostDetailDTO buildDetail(AdoptionPost post, Long viewerUserId, boolean increaseViewCount) {
        if (increaseViewCount) {
            int current = post.getViewCount() == null ? 0 : post.getViewCount();
            post.setViewCount(current + 1);
            post = adoptionPostRepository.save(post);
        }

        Pet pet = petRepository.findById(post.getPetId())
            .orElseThrow(() -> new BizException(ErrorCode.ADOPTION_POST_NOT_FOUND, "Pet not found"));
        List<PetMedia> petMedias = petMediaRepository.findByPetIdOrderBySortOrderAsc(pet.getId());

        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(
                petMedias.stream().map(PetMedia::getFileObjectId).toList())
            .stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));

        UserProfile publisherProfile = userProfileRepository.findByUserId(post.getPublisherUserId()).orElse(null);

        int total = Math.toIntExact(adoptionApplicationRepository.countByPostId(post.getId()));
        int acceptedCount = Math.toIntExact(adoptionApplicationRepository.countByPostIdAndStatus(
            post.getId(),
            com.petlove.weblove.modules.adoption.enums.AdoptionApplicationStatus.ACCEPTED
        ));

        boolean hasApplied = viewerUserId != null && adoptionApplicationRepository
            .findByPostIdAndApplicantUserId(post.getId(), viewerUserId)
            .isPresent();

        ViewerContextDTO viewerContext = buildViewerContext(post, viewerUserId, hasApplied);

        RehomePostPublisherDTO publisher = new RehomePostPublisherDTO(
            post.getPublisherUserId(),
            publisherProfile != null && publisherProfile.getNickname() != null
                ? publisherProfile.getNickname()
                : "用户" + post.getPublisherUserId(),
            publisherProfile != null ? publisherProfile.getAvatarUrl() : null,
            publisherProfile != null ? publisherProfile.isRealNameVerified() : null
        );

        List<PetMediaDTO> media = petMedias.stream().map(item -> {
            FileObject fileObject = fileMap.get(item.getFileObjectId());
            return new PetMediaDTO(
                item.getFileObjectId(),
                fileObject != null ? fileObject.getPublicUrl() : null,
                item.getSortOrder() == null ? 0 : item.getSortOrder()
            );
        }).toList();

        RehomePostPetDTO petDTO = new RehomePostPetDTO(
            pet.getId(),
            pet.getPetType() != null ? pet.getPetType().name() : null,
            pet.getName(),
            pet.getGender() != null ? pet.getGender().name() : null,
            pet.getAgeMonths(),
            pet.getBreed(),
            pet.getWeightKg(),
            pet.getNeuteredStatus() != null ? pet.getNeuteredStatus().name() : null,
            pet.getVaccinatedStatus() != null ? pet.getVaccinatedStatus().name() : null,
            pet.getHealthNote(),
            fromJsonList(pet.getTemperamentTags()),
            pet.getSpecialCareNote(),
            media
        );

        return new RehomePostDetailDTO(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getStatus().name(),
            post.getCityCode(),
            post.getCityName(),
            post.getDistrictName(),
            publisher,
            petDTO,
            post.getPublishedAt(),
            post.getViewCount() == null ? 0 : post.getViewCount(),
            new ApplicationStatsDTO(total, acceptedCount),
            viewerContext
        );
    }

    private ViewerContextDTO buildViewerContext(AdoptionPost post, Long viewerUserId, boolean hasApplied) {
        if (viewerUserId == null) {
            return null;
        }
        if (Objects.equals(post.getPublisherUserId(), viewerUserId)) {
            return new ViewerContextDTO(false, false, "CANNOT_APPLY_OWN_POST");
        }
        if (post.getStatus() != AdoptionPostStatus.PUBLISHED) {
            return new ViewerContextDTO(false, false, "POST_NOT_PUBLISHED");
        }
        if (hasApplied) {
            return new ViewerContextDTO(true, false, "ALREADY_APPLIED");
        }
        return new ViewerContextDTO(false, true, null);
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

    private Subquery<Long> buildPetTypeSubquery(jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                 Root<AdoptionPost> postRoot,
                                                 jakarta.persistence.criteria.CriteriaBuilder cb,
                                                 PetType petType) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<Pet> petRoot = subquery.from(Pet.class);
        subquery.select(petRoot.get("id"));
        subquery.where(petRoot.get("id").in(postRoot.get("petId")),
            cb.equal(petRoot.get("petType"), petType));
        return subquery;
    }

    private void validatePetImageFiles(List<Long> fileIds, long currentUserId) {
        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(fileIds)
            .stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));

        for (Long fileId : fileIds) {
            FileObject fileObject = fileMap.get(fileId);
            if (fileObject == null) {
                throw new BizException(ErrorCode.ADOPTION_POST_FILE_INVALID, "Pet image file not found");
            }
            if (!Objects.equals(fileObject.getOwnerUserId(), currentUserId)) {
                throw new BizException(ErrorCode.ADOPTION_POST_FILE_NOT_OWNED, "Pet image file is not owned by current user");
            }
            if (fileObject.getStatus() != FileStatus.READY) {
                throw new BizException(ErrorCode.ADOPTION_POST_FILE_INVALID, "Pet image file is not ready");
            }
            if (fileObject.getBizType() != FileBizType.PET_MEDIA && fileObject.getBizType() != FileBizType.OTHER) {
                throw new BizException(ErrorCode.ADOPTION_POST_FILE_INVALID, "Pet image file bizType is invalid");
            }
        }
    }

    private void ensureRealNameVerified(long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        if (profile == null || !profile.isRealNameVerified()) {
            throw new BizException(ErrorCode.ADOPTION_REAL_NAME_REQUIRED, "Real-name verification is required");
        }
    }

    private void validateCity(String cityCode) {
        String normalized = normalizeRequiredText(cityCode);
        City city = cityRepository.findByCityCode(normalized)
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "City code is invalid"));
        if (!city.isEnabled()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "City is not enabled");
        }
    }

    private void ensurePostOwner(AdoptionPost post, long currentUserId) {
        if (!Objects.equals(post.getPublisherUserId(), currentUserId)) {
            throw new BizException(ErrorCode.ADOPTION_POST_NOT_OWNER, "Adoption post is not owned by current user");
        }
    }

    private AdoptionPostStatus parsePostStatus(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return AdoptionPostStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private PetType parsePetType(String raw, boolean required) {
        String normalized = normalizeText(raw);
        if (normalized == null) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petType is required");
            }
            return null;
        }
        try {
            return PetType.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petType is invalid");
        }
    }

    private List<Long> normalizeFileIds(List<Long> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petImageFileIds is required");
        }
        LinkedHashSet<Long> deduplicated = new LinkedHashSet<>();
        for (Long fileId : fileIds) {
            if (fileId == null || fileId <= 0) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petImageFileIds contains invalid value");
            }
            if (!deduplicated.add(fileId)) {
                throw new BizException(ErrorCode.INVALID_PARAM, "petImageFileIds cannot contain duplicate values");
            }
        }
        return new ArrayList<>(deduplicated);
    }

    private List<String> normalizeTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        List<String> normalized = tags.stream()
            .map(this::normalizeText)
            .filter(Objects::nonNull)
            .toList();
        return normalized.isEmpty() ? null : normalized;
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

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, STRING_LIST_TYPE);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to parse JSON field");
        }
    }

    private String toJson(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to serialize JSON field");
        }
    }

    private Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthPrincipal principal)) {
            return null;
        }
        return principal.type() == AuthPrincipalType.USER ? principal.id() : null;
    }
}
