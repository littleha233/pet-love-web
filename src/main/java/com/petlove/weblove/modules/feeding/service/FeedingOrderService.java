package com.petlove.weblove.modules.feeding.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.adoption.entity.Pet;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.repository.PetRepository;
import com.petlove.weblove.modules.feeding.dto.user.ConfirmFeedingOrderCompleteRequest;
import com.petlove.weblove.modules.feeding.dto.user.CreateFeedingOrderRequest;
import com.petlove.weblove.modules.feeding.dto.user.CreateFeedingOrderVisitDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderDetailDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOwnedPetOptionDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderPetSnapshotDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderViewerContextDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderVisitDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingReviewDTO;
import com.petlove.weblove.modules.feeding.dto.user.MyFeedingOrderListItemDTO;
import com.petlove.weblove.modules.feeding.dto.user.SubmitFeedingReviewRequest;
import com.petlove.weblove.modules.feeding.dto.user.VisitPhotoDTO;
import com.petlove.weblove.modules.feeding.entity.FeedingOrder;
import com.petlove.weblove.modules.feeding.entity.FeedingOrderPet;
import com.petlove.weblove.modules.feeding.entity.FeedingOrderReview;
import com.petlove.weblove.modules.feeding.entity.FeedingOrderVisit;
import com.petlove.weblove.modules.feeding.entity.FeedingProviderProfile;
import com.petlove.weblove.modules.feeding.entity.FeedingVisitMedia;
import com.petlove.weblove.modules.feeding.enums.FeedingOrderStatus;
import com.petlove.weblove.modules.feeding.enums.FeedingProviderProfileStatus;
import com.petlove.weblove.modules.feeding.enums.FeedingServiceItemTag;
import com.petlove.weblove.modules.feeding.enums.FeedingVisitStatus;
import com.petlove.weblove.modules.feeding.repository.FeedingOrderPetRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingOrderRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingOrderReviewRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingOrderVisitRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingProviderProfileRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingVisitMediaRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.ops.enums.CityFeatureKey;
import com.petlove.weblove.modules.risk.RiskActionKeys;
import com.petlove.weblove.modules.risk.RiskGuard;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.SecurityUtils;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
public class FeedingOrderService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<>() {
    };

    private final FeedingOrderRepository feedingOrderRepository;
    private final FeedingProviderProfileRepository feedingProviderProfileRepository;
    private final FeedingOrderPetRepository feedingOrderPetRepository;
    private final FeedingOrderVisitRepository feedingOrderVisitRepository;
    private final FeedingVisitMediaRepository feedingVisitMediaRepository;
    private final FeedingOrderReviewRepository feedingOrderReviewRepository;
    private final PetRepository petRepository;
    private final UserProfileRepository userProfileRepository;
    private final FileObjectRepository fileObjectRepository;
    private final CityRepository cityRepository;
    private final RiskGuard riskGuard;
    private final ObjectMapper objectMapper;

    public FeedingOrderService(FeedingOrderRepository feedingOrderRepository,
                               FeedingProviderProfileRepository feedingProviderProfileRepository,
                               FeedingOrderPetRepository feedingOrderPetRepository,
                               FeedingOrderVisitRepository feedingOrderVisitRepository,
                               FeedingVisitMediaRepository feedingVisitMediaRepository,
                               FeedingOrderReviewRepository feedingOrderReviewRepository,
                               PetRepository petRepository,
                               UserProfileRepository userProfileRepository,
                               FileObjectRepository fileObjectRepository,
                               CityRepository cityRepository,
                               RiskGuard riskGuard,
                               ObjectMapper objectMapper) {
        this.feedingOrderRepository = feedingOrderRepository;
        this.feedingProviderProfileRepository = feedingProviderProfileRepository;
        this.feedingOrderPetRepository = feedingOrderPetRepository;
        this.feedingOrderVisitRepository = feedingOrderVisitRepository;
        this.feedingVisitMediaRepository = feedingVisitMediaRepository;
        this.feedingOrderReviewRepository = feedingOrderReviewRepository;
        this.petRepository = petRepository;
        this.userProfileRepository = userProfileRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.cityRepository = cityRepository;
        this.riskGuard = riskGuard;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public FeedingOrderDetailDTO create(CreateFeedingOrderRequest request) {
        long ownerUserId = SecurityUtils.currentUserId();
        ensureRealNameVerified(ownerUserId);

        FeedingProviderProfile providerProfile = feedingProviderProfileRepository
            .findByProviderUserId(request.getProviderUserId())
            .orElseThrow(() -> new BizException(
                ErrorCode.FEEDING_PROVIDER_PROFILE_NOT_FOUND,
                "Feeding provider profile not found"
            ));

        if (providerProfile.getStatus() != FeedingProviderProfileStatus.ACTIVE) {
            throw new BizException(
                ErrorCode.FEEDING_PROVIDER_PROFILE_STATUS_INVALID,
                "Feeding provider profile is not active"
            );
        }
        if (Objects.equals(providerProfile.getProviderUserId(), ownerUserId)) {
            throw new BizException(ErrorCode.INVALID_PARAM, "Cannot create feeding order for self");
        }

        validateCity(request.getServiceCityCode());
        riskGuard.ensureUserActionAllowed(
            ownerUserId,
            request.getServiceCityCode(),
            RiskActionKeys.FEEDING_ORDER_CREATE,
            CityFeatureKey.FEEDING
        );

        List<String> serviceItemTags = normalizeServiceItemTags(request.getServiceItemTags(), true);
        List<Long> petIds = normalizePetIds(request.getPetIds());

        Map<Long, Pet> petMap = petRepository.findAllById(petIds).stream()
            .collect(Collectors.toMap(Pet::getId, Function.identity()));
        if (petMap.size() != petIds.size()) {
            throw new BizException(ErrorCode.FEEDING_ORDER_PET_INVALID, "Pet list contains invalid pet id");
        }

        List<Pet> pets = new ArrayList<>();
        for (Long petId : petIds) {
            Pet pet = petMap.get(petId);
            if (pet == null) {
                throw new BizException(ErrorCode.FEEDING_ORDER_PET_INVALID, "Pet list contains invalid pet id");
            }
            if (!Objects.equals(pet.getOwnerUserId(), ownerUserId)) {
                throw new BizException(ErrorCode.FEEDING_ORDER_PET_NOT_OWNED, "Pet is not owned by current user");
            }
            pets.add(pet);
        }

        List<CreateFeedingOrderVisitDTO> visitRequests = request.getVisits();
        List<VisitPlan> visitPlans = new ArrayList<>();
        for (CreateFeedingOrderVisitDTO visit : visitRequests) {
            LocalDateTime startAt = parseDateTime(visit.getPlannedStartAt(), "plannedStartAt");
            LocalDateTime endAt = parseDateTime(visit.getPlannedEndAt(), "plannedEndAt");
            if (!endAt.isAfter(startAt)) {
                throw new BizException(ErrorCode.INVALID_PARAM, "plannedEndAt must be later than plannedStartAt");
            }
            visitPlans.add(new VisitPlan(startAt, endAt));
        }

        FeedingOrder order = new FeedingOrder();
        order.setOrderNo(generateOrderNo());
        order.setOwnerUserId(ownerUserId);
        order.setProviderUserId(providerProfile.getProviderUserId());
        order.setProviderProfileId(providerProfile.getId());
        order.setStatus(FeedingOrderStatus.PENDING_PROVIDER_ACCEPT);
        order.setServiceCityCode(normalizeRequiredText(request.getServiceCityCode(), "serviceCityCode"));
        order.setServiceCityName(normalizeRequiredText(request.getServiceCityName(), "serviceCityName"));
        order.setServiceDistrictName(normalizeText(request.getServiceDistrictName()));
        order.setServiceAddressDetail(normalizeRequiredText(request.getServiceAddressDetail(), "serviceAddressDetail"));
        order.setServiceAddressNote(normalizeText(request.getServiceAddressNote()));
        order.setContactName(normalizeRequiredText(request.getContactName(), "contactName"));
        order.setContactMobile(normalizeRequiredText(request.getContactMobile(), "contactMobile"));
        order.setContactMobileMasked(MaskUtil.maskMobile(order.getContactMobile()));
        order.setServiceItemTags(toJson(serviceItemTags));
        order.setVisitCount(visitPlans.size());
        order.setOwnerNote(normalizeText(request.getOwnerNote()));
        order.setRequestedTotalAmount(request.getRequestedTotalAmount());
        order.setQuotedTotalAmount(null);
        order.setCurrency("CNY");
        order.setProviderResponseNote(null);

        order = feedingOrderRepository.save(order);

        List<FeedingOrderPet> snapshots = new ArrayList<>();
        for (Pet pet : pets) {
            FeedingOrderPet orderPet = new FeedingOrderPet();
            orderPet.setOrderId(order.getId());
            orderPet.setPetId(pet.getId());
            orderPet.setPetNameSnapshot(pet.getName());
            orderPet.setPetTypeSnapshot(pet.getPetType());
            orderPet.setPetGenderSnapshot(pet.getGender());
            orderPet.setAgeMonthsSnapshot(pet.getAgeMonths());
            orderPet.setBreedSnapshot(pet.getBreed());
            orderPet.setSpecialCareNoteSnapshot(pet.getSpecialCareNote());
            snapshots.add(orderPet);
        }
        feedingOrderPetRepository.saveAll(snapshots);

        List<FeedingOrderVisit> visits = new ArrayList<>();
        for (int i = 0; i < visitPlans.size(); i++) {
            VisitPlan plan = visitPlans.get(i);
            FeedingOrderVisit orderVisit = new FeedingOrderVisit();
            orderVisit.setOrderId(order.getId());
            orderVisit.setVisitIndex(i + 1);
            orderVisit.setPlannedStartAt(plan.startAt());
            orderVisit.setPlannedEndAt(plan.endAt());
            orderVisit.setStatus(FeedingVisitStatus.PENDING);
            orderVisit.setFoodDone(false);
            orderVisit.setWaterDone(false);
            orderVisit.setLitterDone(false);
            orderVisit.setPlayDone(false);
            visits.add(orderVisit);
        }
        feedingOrderVisitRepository.saveAll(visits);

        return buildDetail(order, ownerUserId);
    }

    @Transactional(readOnly = true)
    public List<FeedingOwnedPetOptionDTO> myPetOptions() {
        long ownerUserId = SecurityUtils.currentUserId();
        List<Pet> pets = petRepository.findByOwnerUserIdOrderByIdDesc(ownerUserId);
        return pets.stream().map(pet -> new FeedingOwnedPetOptionDTO(
            pet.getId(),
            pet.getName(),
            pet.getPetType() != null ? pet.getPetType().name() : null,
            pet.getGender() != null ? pet.getGender().name() : null,
            pet.getAgeMonths(),
            pet.getBreed()
        )).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MyFeedingOrderListItemDTO> myOrders(int page, int pageSize, String status) {
        long ownerUserId = SecurityUtils.currentUserId();
        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        FeedingOrderStatus parsedStatus = parseOrderStatus(status, false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<FeedingOrder> orderPage = parsedStatus == null
            ? feedingOrderRepository.findByOwnerUserId(ownerUserId, pageable)
            : feedingOrderRepository.findByOwnerUserIdAndStatus(ownerUserId, parsedStatus, pageable);

        if (orderPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<FeedingOrder> orders = orderPage.getContent();
        Set<Long> providerUserIds = orders.stream().map(FeedingOrder::getProviderUserId).collect(Collectors.toSet());
        Map<Long, UserProfile> providerUserProfileMap = userProfileRepository.findByUserIdIn(providerUserIds)
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));
        Map<Long, FeedingProviderProfile> providerProfileMap = feedingProviderProfileRepository.findByProviderUserIdIn(providerUserIds)
            .stream()
            .collect(Collectors.toMap(FeedingProviderProfile::getProviderUserId, Function.identity()));

        List<MyFeedingOrderListItemDTO> items = orders.stream().map(order -> {
            UserProfile providerUserProfile = providerUserProfileMap.get(order.getProviderUserId());
            FeedingProviderProfile providerProfile = providerProfileMap.get(order.getProviderUserId());
            return new MyFeedingOrderListItemDTO(
                order.getId(),
                order.getOrderNo(),
                order.getStatus().name(),
                order.getProviderUserId(),
                displayProviderName(providerProfile, providerUserProfile, order.getProviderUserId()),
                providerUserProfile != null ? providerUserProfile.getAvatarUrl() : null,
                order.getServiceCityName(),
                order.getVisitCount(),
                order.getQuotedTotalAmount(),
                nextVisitPlannedAt(order.getId()),
                order.getCreatedAt(),
                order.getUpdatedAt()
            );
        }).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, orderPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public FeedingOrderDetailDTO detail(Long orderId) {
        long userId = SecurityUtils.currentUserId();
        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getOwnerUserId(), userId) && !Objects.equals(order.getProviderUserId(), userId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found");
        }

        return buildDetail(order, userId);
    }

    @Transactional
    public FeedingOrderDetailDTO cancelByOwner(Long orderId) {
        long ownerUserId = SecurityUtils.currentUserId();
        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getOwnerUserId(), ownerUserId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_OWNER, "Feeding order is not owned by current user");
        }

        if (!canCancelByOwner(order)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_CANNOT_CANCEL, "Feeding order cannot be cancelled");
        }

        order.setStatus(FeedingOrderStatus.CANCELLED_BY_OWNER);
        order.setCancelReason("Cancelled by owner");
        feedingOrderRepository.save(order);
        cancelPendingVisits(order.getId());

        return buildDetail(order, ownerUserId);
    }

    @Transactional
    public FeedingOrderDetailDTO confirmComplete(Long orderId, ConfirmFeedingOrderCompleteRequest request) {
        long ownerUserId = SecurityUtils.currentUserId();
        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getOwnerUserId(), ownerUserId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_OWNER, "Feeding order is not owned by current user");
        }

        if (order.getStatus() != FeedingOrderStatus.WAITING_OWNER_CONFIRM) {
            throw new BizException(ErrorCode.FEEDING_ORDER_CANNOT_CONFIRM, "Feeding order cannot be confirmed");
        }

        order.setStatus(FeedingOrderStatus.COMPLETED);
        order.setOwnerConfirmedAt(LocalDateTime.now());
        order.setProviderResponseNote(appendOwnerRemark(order.getProviderResponseNote(), request));
        feedingOrderRepository.save(order);

        FeedingProviderProfile providerProfile = feedingProviderProfileRepository.findByProviderUserId(order.getProviderUserId())
            .orElse(null);
        if (providerProfile != null) {
            int completed = providerProfile.getCompletedOrderCount() == null ? 0 : providerProfile.getCompletedOrderCount();
            providerProfile.setCompletedOrderCount(completed + 1);
            feedingProviderProfileRepository.save(providerProfile);
        }

        return buildDetail(order, ownerUserId);
    }

    @Transactional
    public FeedingReviewDTO submitReview(Long orderId, SubmitFeedingReviewRequest request) {
        long ownerUserId = SecurityUtils.currentUserId();
        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getOwnerUserId(), ownerUserId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_OWNER, "Feeding order is not owned by current user");
        }

        if (order.getStatus() != FeedingOrderStatus.COMPLETED) {
            throw new BizException(ErrorCode.FEEDING_REVIEW_NOT_ALLOWED, "Review is only allowed after completion");
        }

        if (feedingOrderReviewRepository.existsByOrderId(orderId)) {
            throw new BizException(ErrorCode.FEEDING_REVIEW_ALREADY_EXISTS, "Review already exists for this order");
        }

        FeedingOrderReview review = new FeedingOrderReview();
        review.setOrderId(orderId);
        review.setOwnerUserId(ownerUserId);
        review.setProviderUserId(order.getProviderUserId());
        review.setRatingOverall(request.getRatingOverall());
        review.setRatingTimeliness(request.getRatingTimeliness());
        review.setRatingCleanliness(request.getRatingCleanliness());
        review.setRatingAttitude(request.getRatingAttitude());
        review.setContent(normalizeText(request.getContent()));

        try {
            review = feedingOrderReviewRepository.save(review);
        } catch (DataIntegrityViolationException ex) {
            throw new BizException(ErrorCode.FEEDING_REVIEW_ALREADY_EXISTS, "Review already exists for this order");
        }

        refreshProviderRating(order.getProviderUserId(), request.getRatingOverall());
        return toReviewDTO(review);
    }

    @Transactional(readOnly = true)
    public FeedingOrder requireOrderForProvider(Long orderId) {
        long providerUserId = SecurityUtils.currentUserId();
        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));
        if (!Objects.equals(order.getProviderUserId(), providerUserId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_PROVIDER, "Feeding order is not for current provider");
        }
        return order;
    }

    private FeedingOrderDetailDTO buildDetail(FeedingOrder order, long viewerUserId) {
        UserProfile ownerProfile = userProfileRepository.findByUserId(order.getOwnerUserId()).orElse(null);
        UserProfile providerUserProfile = userProfileRepository.findByUserId(order.getProviderUserId()).orElse(null);
        FeedingProviderProfile providerProfile = feedingProviderProfileRepository.findByProviderUserId(order.getProviderUserId())
            .orElse(null);

        List<FeedingOrderPet> petSnapshots = feedingOrderPetRepository.findByOrderIdOrderByIdAsc(order.getId());
        List<FeedingOrderPetSnapshotDTO> petDtos = petSnapshots.stream().map(snapshot -> new FeedingOrderPetSnapshotDTO(
            snapshot.getPetId(),
            snapshot.getPetNameSnapshot(),
            snapshot.getPetTypeSnapshot() != null ? snapshot.getPetTypeSnapshot().name() : null,
            snapshot.getPetGenderSnapshot() != null ? snapshot.getPetGenderSnapshot().name() : null,
            snapshot.getAgeMonthsSnapshot(),
            snapshot.getBreedSnapshot(),
            snapshot.getSpecialCareNoteSnapshot()
        )).toList();

        List<FeedingOrderVisit> visits = feedingOrderVisitRepository.findByOrderIdOrderByVisitIndexAsc(order.getId());
        List<Long> visitIds = visits.stream().map(FeedingOrderVisit::getId).toList();
        List<FeedingVisitMedia> visitMediaList = visitIds.isEmpty()
            ? Collections.emptyList()
            : feedingVisitMediaRepository.findByVisitIdInOrderByVisitIdAscSortOrderAsc(visitIds);

        Map<Long, List<FeedingVisitMedia>> visitMediaMap = visitMediaList.stream()
            .collect(Collectors.groupingBy(FeedingVisitMedia::getVisitId, LinkedHashMap::new, Collectors.toList()));

        Set<Long> photoFileIds = visitMediaList.stream()
            .map(FeedingVisitMedia::getFileObjectId)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, String> fileUrlMap = fileObjectRepository.findAllById(photoFileIds).stream()
            .collect(Collectors.toMap(FileObject::getId, FileObject::getPublicUrl));

        List<FeedingOrderVisitDTO> visitDtos = visits.stream().map(visit -> {
            List<VisitPhotoDTO> photos = visitMediaMap.getOrDefault(visit.getId(), Collections.emptyList())
                .stream()
                .map(media -> new VisitPhotoDTO(
                    media.getFileObjectId(),
                    fileUrlMap.get(media.getFileObjectId()),
                    media.getSortOrder()
                ))
                .toList();
            return new FeedingOrderVisitDTO(
                visit.getId(),
                visit.getVisitIndex(),
                visit.getPlannedStartAt(),
                visit.getPlannedEndAt(),
                visit.getStatus().name(),
                visit.getActualStartAt(),
                visit.getActualEndAt(),
                visit.isFoodDone(),
                visit.isWaterDone(),
                visit.isLitterDone(),
                visit.isPlayDone(),
                visit.getHealthObservation(),
                visit.getVisitNote(),
                photos
            );
        }).toList();

        FeedingOrderReview review = feedingOrderReviewRepository.findByOrderId(order.getId()).orElse(null);
        FeedingReviewDTO reviewDTO = review == null ? null : toReviewDTO(review);

        boolean isOwner = Objects.equals(order.getOwnerUserId(), viewerUserId);
        boolean isProvider = Objects.equals(order.getProviderUserId(), viewerUserId);

        FeedingOrderViewerContextDTO viewerContext = new FeedingOrderViewerContextDTO(
            isOwner,
            isProvider,
            isOwner && canCancelByOwner(order),
            isOwner && order.getStatus() == FeedingOrderStatus.WAITING_OWNER_CONFIRM,
            isOwner && order.getStatus() == FeedingOrderStatus.COMPLETED && review == null
        );

        return new FeedingOrderDetailDTO(
            order.getId(),
            order.getOrderNo(),
            order.getStatus().name(),
            order.getOwnerUserId(),
            ownerProfile != null ? ownerProfile.getNickname() : "用户" + order.getOwnerUserId(),
            order.getProviderUserId(),
            displayProviderName(providerProfile, providerUserProfile, order.getProviderUserId()),
            providerUserProfile != null ? providerUserProfile.getAvatarUrl() : null,
            providerProfile != null ? providerProfile.getRatingAvg() : null,
            order.getServiceCityCode(),
            order.getServiceCityName(),
            order.getServiceDistrictName(),
            order.getServiceAddressDetail(),
            order.getServiceAddressNote(),
            order.getContactName(),
            order.getContactMobileMasked(),
            fromJsonList(order.getServiceItemTags()),
            order.getOwnerNote(),
            order.getRequestedTotalAmount(),
            order.getQuotedTotalAmount(),
            order.getCurrency(),
            petDtos,
            visitDtos,
            reviewDTO,
            order.getCreatedAt(),
            order.getUpdatedAt(),
            viewerContext
        );
    }

    private void ensureRealNameVerified(long ownerUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(ownerUserId)
            .orElseThrow(() -> new BizException(
                ErrorCode.FEEDING_REAL_NAME_REQUIRED,
                "Real-name verification is required"
            ));
        if (!userProfile.isRealNameVerified()) {
            throw new BizException(
                ErrorCode.FEEDING_REAL_NAME_REQUIRED,
                "Real-name verification is required"
            );
        }
    }

    private void validateCity(String cityCode) {
        City city = cityRepository.findByCityCode(normalizeRequiredText(cityCode, "serviceCityCode"))
            .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "serviceCityCode is invalid"));
        if (!city.isEnabled()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "service city is disabled");
        }
    }

    private FeedingOrderStatus parseOrderStatus(String value, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "status is required");
            }
            return null;
        }
        try {
            return FeedingOrderStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "status is invalid");
        }
    }

    private List<String> normalizeServiceItemTags(List<String> values, boolean required) {
        if (values == null || values.isEmpty()) {
            if (required) {
                throw new BizException(ErrorCode.INVALID_PARAM, "serviceItemTags is required");
            }
            return Collections.emptyList();
        }

        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String raw : values) {
            if (raw == null || raw.isBlank()) {
                throw new BizException(ErrorCode.INVALID_PARAM, "serviceItemTags contains blank value");
            }
            try {
                normalized.add(FeedingServiceItemTag.valueOf(raw.trim().toUpperCase(Locale.ROOT)).name());
            } catch (IllegalArgumentException ex) {
                throw new BizException(ErrorCode.INVALID_PARAM, "serviceItemTags contains invalid value");
            }
        }

        return new ArrayList<>(normalized);
    }

    private List<Long> normalizePetIds(List<Long> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "petIds is required");
        }

        List<Long> normalized = petIds.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();

        if (normalized.size() != petIds.size()) {
            throw new BizException(ErrorCode.FEEDING_ORDER_PET_INVALID, "petIds contains invalid value");
        }

        return normalized;
    }

    private LocalDateTime parseDateTime(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " is required");
        }
        try {
            return LocalDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, fieldName + " is invalid datetime");
        }
    }

    private String generateOrderNo() {
        for (int i = 0; i < 8; i++) {
            String candidate = "F" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format(Locale.ROOT, "%04d", (int) (Math.random() * 10000));
            if (!feedingOrderRepository.existsByOrderNo(candidate)) {
                return candidate;
            }
        }
        return "F" + System.currentTimeMillis() + (int) (Math.random() * 1000);
    }

    private String displayProviderName(FeedingProviderProfile providerProfile,
                                       UserProfile providerUserProfile,
                                       Long providerUserId) {
        String displayName = providerProfile != null ? normalizeText(providerProfile.getDisplayName()) : null;
        if (displayName != null) {
            return displayName;
        }
        if (providerUserProfile != null && normalizeText(providerUserProfile.getNickname()) != null) {
            return providerUserProfile.getNickname();
        }
        return "服务者" + providerUserId;
    }

    private LocalDateTime nextVisitPlannedAt(Long orderId) {
        return feedingOrderVisitRepository.findNextPlannedStartAt(
            orderId,
            List.of(FeedingVisitStatus.PENDING, FeedingVisitStatus.STARTED)
        ).orElse(null);
    }

    private boolean canCancelByOwner(FeedingOrder order) {
        if (order.getStatus() != FeedingOrderStatus.PENDING_PROVIDER_ACCEPT
            && order.getStatus() != FeedingOrderStatus.CONFIRMED) {
            return false;
        }

        return !feedingOrderVisitRepository.existsByOrderIdAndStatusIn(
            order.getId(),
            List.of(FeedingVisitStatus.STARTED, FeedingVisitStatus.DONE)
        );
    }

    private void cancelPendingVisits(Long orderId) {
        List<FeedingOrderVisit> visits = feedingOrderVisitRepository.findByOrderIdOrderByVisitIndexAsc(orderId);
        boolean dirty = false;
        for (FeedingOrderVisit visit : visits) {
            if (visit.getStatus() == FeedingVisitStatus.PENDING) {
                visit.setStatus(FeedingVisitStatus.CANCELLED);
                dirty = true;
            }
        }
        if (dirty) {
            feedingOrderVisitRepository.saveAll(visits);
        }
    }

    private void refreshProviderRating(Long providerUserId, int newRatingOverall) {
        FeedingProviderProfile providerProfile = feedingProviderProfileRepository.findByProviderUserId(providerUserId)
            .orElse(null);
        if (providerProfile == null) {
            return;
        }

        int currentCount = providerProfile.getRatingCount() == null ? 0 : providerProfile.getRatingCount();
        BigDecimal currentAvg = providerProfile.getRatingAvg() == null
            ? BigDecimal.ZERO.setScale(2)
            : providerProfile.getRatingAvg().setScale(2, RoundingMode.HALF_UP);

        int updatedCount = currentCount + 1;
        BigDecimal updatedAvg = currentAvg
            .multiply(BigDecimal.valueOf(currentCount))
            .add(BigDecimal.valueOf(newRatingOverall))
            .divide(BigDecimal.valueOf(updatedCount), 2, RoundingMode.HALF_UP);

        providerProfile.setRatingCount(updatedCount);
        providerProfile.setRatingAvg(updatedAvg);
        feedingProviderProfileRepository.save(providerProfile);
    }

    private FeedingReviewDTO toReviewDTO(FeedingOrderReview review) {
        return new FeedingReviewDTO(
            review.getId(),
            review.getOrderId(),
            review.getRatingOverall(),
            review.getRatingTimeliness(),
            review.getRatingCleanliness(),
            review.getRatingAttitude(),
            review.getContent(),
            review.getCreatedAt()
        );
    }

    private String appendOwnerRemark(String providerResponseNote, ConfirmFeedingOrderCompleteRequest request) {
        String remark = request == null ? null : normalizeText(request.getRemark());
        if (remark == null) {
            return providerResponseNote;
        }
        if (providerResponseNote == null || providerResponseNote.isBlank()) {
            return "[OWNER_REMARK] " + remark;
        }
        return providerResponseNote + "\n[OWNER_REMARK] " + remark;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to serialize order JSON field");
        }
    }

    private List<String> fromJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.emptyList() : values;
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "Failed to parse order JSON field");
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

    private record VisitPlan(LocalDateTime startAt, LocalDateTime endAt) {
    }
}
