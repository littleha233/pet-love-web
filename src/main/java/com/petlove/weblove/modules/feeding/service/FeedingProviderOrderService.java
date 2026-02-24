package com.petlove.weblove.modules.feeding.service;

import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.feeding.dto.provider.CancelProviderOrderRequest;
import com.petlove.weblove.modules.feeding.dto.provider.ProviderOrderListItemDTO;
import com.petlove.weblove.modules.feeding.dto.provider.ProviderRespondOrderRequest;
import com.petlove.weblove.modules.feeding.dto.provider.SubmitVisitLogRequest;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderDetailDTO;
import com.petlove.weblove.modules.feeding.entity.FeedingOrder;
import com.petlove.weblove.modules.feeding.entity.FeedingOrderVisit;
import com.petlove.weblove.modules.feeding.entity.FeedingVisitMedia;
import com.petlove.weblove.modules.feeding.enums.FeedingOrderStatus;
import com.petlove.weblove.modules.feeding.enums.FeedingVisitStatus;
import com.petlove.weblove.modules.feeding.enums.ProviderRespondAction;
import com.petlove.weblove.modules.feeding.repository.FeedingOrderRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingOrderVisitRepository;
import com.petlove.weblove.modules.feeding.repository.FeedingVisitMediaRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedingProviderOrderService {

    private final FeedingOrderRepository feedingOrderRepository;
    private final FeedingOrderVisitRepository feedingOrderVisitRepository;
    private final FeedingVisitMediaRepository feedingVisitMediaRepository;
    private final FileObjectRepository fileObjectRepository;
    private final UserProfileRepository userProfileRepository;
    private final FeedingOrderService feedingOrderService;

    public FeedingProviderOrderService(FeedingOrderRepository feedingOrderRepository,
                                       FeedingOrderVisitRepository feedingOrderVisitRepository,
                                       FeedingVisitMediaRepository feedingVisitMediaRepository,
                                       FileObjectRepository fileObjectRepository,
                                       UserProfileRepository userProfileRepository,
                                       FeedingOrderService feedingOrderService) {
        this.feedingOrderRepository = feedingOrderRepository;
        this.feedingOrderVisitRepository = feedingOrderVisitRepository;
        this.feedingVisitMediaRepository = feedingVisitMediaRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.userProfileRepository = userProfileRepository;
        this.feedingOrderService = feedingOrderService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProviderOrderListItemDTO> myOrders(int page, int pageSize, String status) {
        long providerUserId = SecurityUtils.currentUserId();
        ensureProviderVerified(providerUserId);

        int normalizedPage = Math.max(page, 1);
        int normalizedPageSize = Math.min(Math.max(pageSize, 1), 100);
        FeedingOrderStatus parsedStatus = parseOrderStatus(status, false);

        Pageable pageable = PageRequest.of(
            normalizedPage - 1,
            normalizedPageSize,
            Sort.by(Sort.Direction.DESC, "updatedAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );

        Page<FeedingOrder> orderPage = parsedStatus == null
            ? feedingOrderRepository.findByProviderUserId(providerUserId, pageable)
            : feedingOrderRepository.findByProviderUserIdAndStatus(providerUserId, parsedStatus, pageable);

        if (orderPage.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), normalizedPage, normalizedPageSize, 0);
        }

        List<FeedingOrder> orders = orderPage.getContent();
        Set<Long> ownerUserIds = orders.stream().map(FeedingOrder::getOwnerUserId).collect(Collectors.toSet());
        Map<Long, UserProfile> ownerProfileMap = userProfileRepository.findByUserIdIn(ownerUserIds)
            .stream()
            .collect(Collectors.toMap(UserProfile::getUserId, Function.identity()));

        List<ProviderOrderListItemDTO> items = orders.stream().map(order -> {
            UserProfile ownerProfile = ownerProfileMap.get(order.getOwnerUserId());
            return new ProviderOrderListItemDTO(
                order.getId(),
                order.getOrderNo(),
                order.getStatus().name(),
                order.getOwnerUserId(),
                ownerProfile != null ? ownerProfile.getNickname() : "用户" + order.getOwnerUserId(),
                order.getServiceCityName(),
                order.getVisitCount(),
                order.getQuotedTotalAmount(),
                feedingOrderVisitRepository.findNextPlannedStartAt(
                    order.getId(),
                    List.of(FeedingVisitStatus.PENDING, FeedingVisitStatus.STARTED)
                ).orElse(null),
                order.getCreatedAt(),
                order.getUpdatedAt()
            );
        }).toList();

        return new PageResponse<>(items, normalizedPage, normalizedPageSize, orderPage.getTotalElements());
    }

    @Transactional
    public FeedingOrderDetailDTO respond(Long orderId, ProviderRespondOrderRequest request) {
        long providerUserId = SecurityUtils.currentUserId();
        ensureProviderVerified(providerUserId);

        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getProviderUserId(), providerUserId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_PROVIDER, "Feeding order is not for current provider");
        }
        if (order.getStatus() != FeedingOrderStatus.PENDING_PROVIDER_ACCEPT) {
            throw new BizException(ErrorCode.FEEDING_ORDER_STATUS_INVALID, "Feeding order status does not allow respond");
        }

        ProviderRespondAction action = parseRespondAction(request.getAction());
        order.setProviderResponseNote(normalizeText(request.getProviderResponseNote()));
        if (request.getQuotedTotalAmount() != null) {
            order.setQuotedTotalAmount(request.getQuotedTotalAmount());
        }

        if (action == ProviderRespondAction.ACCEPT) {
            order.setStatus(FeedingOrderStatus.CONFIRMED);
        } else {
            order.setStatus(FeedingOrderStatus.REJECTED_BY_PROVIDER);
        }
        feedingOrderRepository.save(order);

        return feedingOrderService.detail(orderId);
    }

    @Transactional
    public FeedingOrderDetailDTO startVisit(Long visitId) {
        long providerUserId = SecurityUtils.currentUserId();
        ensureProviderVerified(providerUserId);

        FeedingOrderVisit visit = feedingOrderVisitRepository.findById(visitId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_VISIT_NOT_FOUND, "Feeding visit not found"));

        FeedingOrder order = feedingOrderRepository.findById(visit.getOrderId())
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getProviderUserId(), providerUserId)) {
            throw new BizException(ErrorCode.FEEDING_VISIT_NOT_PROVIDER, "Feeding visit is not for current provider");
        }
        if (visit.getStatus() != FeedingVisitStatus.PENDING) {
            throw new BizException(ErrorCode.FEEDING_VISIT_STATUS_INVALID, "Feeding visit status does not allow start");
        }
        if (order.getStatus() != FeedingOrderStatus.CONFIRMED && order.getStatus() != FeedingOrderStatus.IN_SERVICE) {
            throw new BizException(ErrorCode.FEEDING_ORDER_STATUS_INVALID, "Feeding order status does not allow visit start");
        }

        visit.setStatus(FeedingVisitStatus.STARTED);
        visit.setActualStartAt(LocalDateTime.now());
        feedingOrderVisitRepository.save(visit);

        if (order.getStatus() == FeedingOrderStatus.CONFIRMED) {
            order.setStatus(FeedingOrderStatus.IN_SERVICE);
            feedingOrderRepository.save(order);
        }

        return feedingOrderService.detail(order.getId());
    }

    @Transactional
    public FeedingOrderDetailDTO submitVisitLog(Long visitId, SubmitVisitLogRequest request) {
        long providerUserId = SecurityUtils.currentUserId();
        ensureProviderVerified(providerUserId);

        FeedingOrderVisit visit = feedingOrderVisitRepository.findById(visitId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_VISIT_NOT_FOUND, "Feeding visit not found"));

        FeedingOrder order = feedingOrderRepository.findById(visit.getOrderId())
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getProviderUserId(), providerUserId)) {
            throw new BizException(ErrorCode.FEEDING_VISIT_NOT_PROVIDER, "Feeding visit is not for current provider");
        }
        if (visit.getStatus() != FeedingVisitStatus.STARTED) {
            throw new BizException(ErrorCode.FEEDING_VISIT_STATUS_INVALID, "Feeding visit status does not allow submit log");
        }

        List<Long> photoFileIds = normalizeFileIds(request.getPhotoFileIds(), 9);
        validateVisitPhotoFiles(photoFileIds, providerUserId);

        if (request.getFoodDone() != null) {
            visit.setFoodDone(request.getFoodDone());
        }
        if (request.getWaterDone() != null) {
            visit.setWaterDone(request.getWaterDone());
        }
        if (request.getLitterDone() != null) {
            visit.setLitterDone(request.getLitterDone());
        }
        if (request.getPlayDone() != null) {
            visit.setPlayDone(request.getPlayDone());
        }
        visit.setHealthObservation(normalizeText(request.getHealthObservation()));
        visit.setVisitNote(normalizeText(request.getVisitNote()));
        visit.setStatus(FeedingVisitStatus.DONE);
        visit.setActualEndAt(LocalDateTime.now());
        feedingOrderVisitRepository.save(visit);

        if (!photoFileIds.isEmpty()) {
            List<FeedingVisitMedia> visitMediaList = new ArrayList<>();
            for (int i = 0; i < photoFileIds.size(); i++) {
                FeedingVisitMedia media = new FeedingVisitMedia();
                media.setVisitId(visit.getId());
                media.setFileObjectId(photoFileIds.get(i));
                media.setSortOrder(i);
                visitMediaList.add(media);
            }
            feedingVisitMediaRepository.saveAll(visitMediaList);
        }

        if (!feedingOrderVisitRepository.existsByOrderIdAndStatusIn(
            order.getId(),
            List.of(FeedingVisitStatus.PENDING, FeedingVisitStatus.STARTED)
        )) {
            if (order.getStatus() == FeedingOrderStatus.CONFIRMED || order.getStatus() == FeedingOrderStatus.IN_SERVICE) {
                order.setStatus(FeedingOrderStatus.WAITING_OWNER_CONFIRM);
                feedingOrderRepository.save(order);
            }
        }

        return feedingOrderService.detail(order.getId());
    }

    @Transactional
    public FeedingOrderDetailDTO cancelByProvider(Long orderId, CancelProviderOrderRequest request) {
        long providerUserId = SecurityUtils.currentUserId();
        ensureProviderVerified(providerUserId);

        FeedingOrder order = feedingOrderRepository.findById(orderId)
            .orElseThrow(() -> new BizException(ErrorCode.FEEDING_ORDER_NOT_FOUND, "Feeding order not found"));

        if (!Objects.equals(order.getProviderUserId(), providerUserId)) {
            throw new BizException(ErrorCode.FEEDING_ORDER_NOT_PROVIDER, "Feeding order is not for current provider");
        }

        if (order.getStatus() != FeedingOrderStatus.PENDING_PROVIDER_ACCEPT
            && order.getStatus() != FeedingOrderStatus.CONFIRMED) {
            throw new BizException(ErrorCode.FEEDING_ORDER_CANNOT_CANCEL, "Feeding order cannot be cancelled");
        }

        boolean started = feedingOrderVisitRepository.existsByOrderIdAndStatusIn(
            order.getId(),
            List.of(FeedingVisitStatus.STARTED, FeedingVisitStatus.DONE)
        );
        if (started) {
            throw new BizException(ErrorCode.FEEDING_ORDER_CANNOT_CANCEL, "Feeding order cannot be cancelled");
        }

        order.setStatus(FeedingOrderStatus.CANCELLED_BY_PROVIDER);
        order.setCancelReason(normalizeRequiredText(request.getReason(), "reason"));
        feedingOrderRepository.save(order);

        List<FeedingOrderVisit> visits = feedingOrderVisitRepository.findByOrderIdOrderByVisitIndexAsc(order.getId());
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

        return feedingOrderService.detail(order.getId());
    }

    private UserProfile ensureProviderVerified(long providerUserId) {
        UserProfile userProfile = userProfileRepository.findByUserId(providerUserId)
            .orElseThrow(() -> new BizException(
                ErrorCode.FEEDING_PROVIDER_VERIFICATION_REQUIRED,
                "Provider verification is required"
            ));

        if (!userProfile.isProviderVerified()) {
            throw new BizException(
                ErrorCode.FEEDING_PROVIDER_VERIFICATION_REQUIRED,
                "Provider verification is required"
            );
        }
        return userProfile;
    }

    private ProviderRespondAction parseRespondAction(String value) {
        if (value == null || value.isBlank()) {
            throw new BizException(ErrorCode.INVALID_PARAM, "action is required");
        }
        try {
            return ProviderRespondAction.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.INVALID_PARAM, "action is invalid");
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

    private List<Long> normalizeFileIds(List<Long> ids, int maxCount) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        if (ids.size() > maxCount) {
            throw new BizException(ErrorCode.INVALID_PARAM, "photoFileIds size exceeds limit");
        }

        List<Long> normalized = ids.stream()
            .filter(id -> id != null && id > 0)
            .distinct()
            .toList();
        if (normalized.size() != ids.size()) {
            throw new BizException(ErrorCode.FEEDING_VISIT_FILE_INVALID, "photoFileIds contains invalid value");
        }
        return normalized;
    }

    private void validateVisitPhotoFiles(List<Long> photoFileIds, long providerUserId) {
        if (photoFileIds.isEmpty()) {
            return;
        }

        Map<Long, FileObject> fileMap = fileObjectRepository.findAllById(photoFileIds).stream()
            .collect(Collectors.toMap(FileObject::getId, Function.identity()));

        if (fileMap.size() != photoFileIds.size()) {
            throw new BizException(ErrorCode.FEEDING_VISIT_FILE_INVALID, "Visit photo file not found");
        }

        for (Long fileId : photoFileIds) {
            FileObject fileObject = fileMap.get(fileId);
            if (fileObject.getStatus() != FileStatus.READY
                || (fileObject.getBizType() != FileBizType.FEEDING_LOG && fileObject.getBizType() != FileBizType.OTHER)) {
                throw new BizException(ErrorCode.FEEDING_VISIT_FILE_INVALID, "Visit photo file is invalid");
            }

            if (!Objects.equals(fileObject.getOwnerUserId(), providerUserId)) {
                throw new BizException(ErrorCode.FEEDING_VISIT_FILE_NOT_OWNED, "Visit photo file is not owned by provider");
            }
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
