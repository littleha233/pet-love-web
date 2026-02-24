package com.petlove.weblove.modules.feeding;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.entity.Pet;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.repository.PetRepository;
import com.petlove.weblove.modules.feeding.dto.provider.ProviderRespondOrderRequest;
import com.petlove.weblove.modules.feeding.dto.provider.SubmitVisitLogRequest;
import com.petlove.weblove.modules.feeding.dto.user.CreateFeedingOrderRequest;
import com.petlove.weblove.modules.feeding.dto.user.CreateFeedingOrderVisitDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingOrderDetailDTO;
import com.petlove.weblove.modules.feeding.dto.user.FeedingReviewDTO;
import com.petlove.weblove.modules.feeding.dto.user.SubmitFeedingReviewRequest;
import com.petlove.weblove.modules.feeding.entity.FeedingProviderProfile;
import com.petlove.weblove.modules.feeding.enums.FeedingOrderStatus;
import com.petlove.weblove.modules.feeding.enums.FeedingProviderProfileStatus;
import com.petlove.weblove.modules.feeding.repository.FeedingProviderProfileRepository;
import com.petlove.weblove.modules.feeding.service.FeedingOrderService;
import com.petlove.weblove.modules.feeding.service.FeedingProviderOrderService;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.enums.LoginType;
import com.petlove.weblove.modules.user.enums.UserStatus;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.security.AuthPrincipal;
import com.petlove.weblove.security.AuthPrincipalType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeedingPhase4ServiceTests {

    @Autowired
    private FeedingOrderService feedingOrderService;

    @Autowired
    private FeedingProviderOrderService feedingProviderOrderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private FeedingProviderProfileRepository feedingProviderProfileRepository;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createOrder_requiresRealNameVerification() {
        User owner = createUser("13900010001");
        createUserProfile(owner.getId(), false, false, "未实名主人");
        User provider = createUser("13900010002");
        createUserProfile(provider.getId(), true, true, "服务者");
        createActiveProviderProfile(provider.getId());
        Pet pet = createPet(owner.getId(), "咪咪");

        setUserAuth(owner.getId());

        BizException ex = assertThrows(BizException.class, () -> feedingOrderService.create(minimalCreateOrderRequest(
            provider.getId(),
            pet.getId()
        )));
        assertEquals(ErrorCode.FEEDING_REAL_NAME_REQUIRED, ex.getErrorCode());
    }

    @Test
    void provider_submitVisitLog_transitionsOrderToWaitingOwnerConfirm() {
        User owner = createUser("13900010010");
        createUserProfile(owner.getId(), true, false, "主人A");

        User provider = createUser("13900010011");
        createUserProfile(provider.getId(), true, true, "服务者A");
        createActiveProviderProfile(provider.getId());

        Pet pet = createPet(owner.getId(), "糯米");

        setUserAuth(owner.getId());
        FeedingOrderDetailDTO created = feedingOrderService.create(minimalCreateOrderRequest(provider.getId(), pet.getId()));
        assertEquals("PENDING_PROVIDER_ACCEPT", created.status());

        setUserAuth(provider.getId());
        ProviderRespondOrderRequest respondRequest = new ProviderRespondOrderRequest();
        respondRequest.setAction("ACCEPT");
        respondRequest.setQuotedTotalAmount(new BigDecimal("88.00"));
        FeedingOrderDetailDTO confirmed = feedingProviderOrderService.respond(created.orderId(), respondRequest);
        assertEquals("CONFIRMED", confirmed.status());

        Long visitId = confirmed.visits().getFirst().visitId();
        FeedingOrderDetailDTO started = feedingProviderOrderService.startVisit(visitId);
        assertEquals("IN_SERVICE", started.status());
        assertEquals("STARTED", started.visits().getFirst().status());

        FileObject visitPhoto = createReadyFile(provider.getId(), FileBizType.FEEDING_LOG);
        SubmitVisitLogRequest submitVisitLogRequest = new SubmitVisitLogRequest();
        submitVisitLogRequest.setFoodDone(true);
        submitVisitLogRequest.setWaterDone(true);
        submitVisitLogRequest.setVisitNote("食盆已清理，状态正常");
        submitVisitLogRequest.setPhotoFileIds(List.of(visitPhoto.getId()));

        FeedingOrderDetailDTO waiting = feedingProviderOrderService.submitVisitLog(visitId, submitVisitLogRequest);
        assertEquals("WAITING_OWNER_CONFIRM", waiting.status());
        assertEquals("DONE", waiting.visits().getFirst().status());
    }

    @Test
    void review_updatesProviderRating_andPreventsDuplicate() {
        User owner = createUser("13900010020");
        createUserProfile(owner.getId(), true, false, "主人B");

        User provider = createUser("13900010021");
        createUserProfile(provider.getId(), true, true, "服务者B");
        createActiveProviderProfile(provider.getId());

        Pet pet = createPet(owner.getId(), "团团");

        setUserAuth(owner.getId());
        FeedingOrderDetailDTO created = feedingOrderService.create(minimalCreateOrderRequest(provider.getId(), pet.getId()));

        setUserAuth(provider.getId());
        ProviderRespondOrderRequest respondRequest = new ProviderRespondOrderRequest();
        respondRequest.setAction("ACCEPT");
        feedingProviderOrderService.respond(created.orderId(), respondRequest);

        Long visitId = feedingOrderService.detail(created.orderId()).visits().getFirst().visitId();
        feedingProviderOrderService.startVisit(visitId);
        SubmitVisitLogRequest submitVisitLogRequest = new SubmitVisitLogRequest();
        submitVisitLogRequest.setFoodDone(true);
        feedingProviderOrderService.submitVisitLog(visitId, submitVisitLogRequest);

        setUserAuth(owner.getId());
        feedingOrderService.confirmComplete(created.orderId(), null);

        SubmitFeedingReviewRequest reviewRequest = new SubmitFeedingReviewRequest();
        reviewRequest.setRatingOverall(5);
        reviewRequest.setRatingTimeliness(5);
        reviewRequest.setRatingCleanliness(5);
        reviewRequest.setRatingAttitude(5);
        reviewRequest.setContent("非常负责");
        FeedingReviewDTO review = feedingOrderService.submitReview(created.orderId(), reviewRequest);
        assertNotNull(review.reviewId());

        FeedingProviderProfile profile = feedingProviderProfileRepository.findByProviderUserId(provider.getId()).orElseThrow();
        assertEquals(1, profile.getRatingCount());
        assertEquals(0, profile.getRatingAvg().compareTo(new BigDecimal("5.00")));

        BizException ex = assertThrows(BizException.class, () -> feedingOrderService.submitReview(created.orderId(), reviewRequest));
        assertEquals(ErrorCode.FEEDING_REVIEW_ALREADY_EXISTS, ex.getErrorCode());
    }

    private CreateFeedingOrderRequest minimalCreateOrderRequest(Long providerUserId, Long petId) {
        CreateFeedingOrderRequest request = new CreateFeedingOrderRequest();
        request.setProviderUserId(providerUserId);
        request.setServiceCityCode("310100");
        request.setServiceCityName("上海");
        request.setServiceDistrictName("浦东新区");
        request.setServiceAddressDetail("测试路 1 号");
        request.setContactName("测试联系人");
        request.setContactMobile("13912345678");
        request.setPetIds(List.of(petId));
        request.setServiceItemTags(List.of("FEED", "WATER"));

        CreateFeedingOrderVisitDTO visit = new CreateFeedingOrderVisitDTO();
        LocalDateTime start = LocalDateTime.now().plusDays(1).withSecond(0).withNano(0);
        visit.setPlannedStartAt(start.toString());
        visit.setPlannedEndAt(start.plusHours(1).toString());
        request.setVisits(List.of(visit));

        return request;
    }

    private User createUser(String mobile) {
        User user = new User();
        user.setMobile(mobile);
        user.setLoginType(LoginType.MOBILE_OTP);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private UserProfile createUserProfile(Long userId, boolean realNameVerified, boolean providerVerified, String nickname) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNickname(nickname);
        profile.setRealNameVerified(realNameVerified);
        profile.setProviderVerified(providerVerified);
        return userProfileRepository.save(profile);
    }

    private Pet createPet(Long ownerUserId, String name) {
        Pet pet = new Pet();
        pet.setOwnerUserId(ownerUserId);
        pet.setPetType(PetType.CAT);
        pet.setName(name);
        pet.setAgeMonths(12);
        return petRepository.save(pet);
    }

    private void createActiveProviderProfile(Long providerUserId) {
        FeedingProviderProfile profile = new FeedingProviderProfile();
        profile.setProviderUserId(providerUserId);
        profile.setStatus(FeedingProviderProfileStatus.ACTIVE);
        profile.setDisplayName("服务者" + providerUserId);
        profile.setIntro("可上门喂养");
        profile.setServiceCityCode("310100");
        profile.setServiceCityName("上海");
        profile.setServiceDistricts(toJson(List.of("浦东新区")));
        profile.setServicePetTypes(toJson(List.of("CAT")));
        profile.setServiceItemTags(toJson(List.of("FEED", "WATER", "PHOTO_REPORT")));
        profile.setRatingAvg(BigDecimal.ZERO.setScale(2));
        profile.setRatingCount(0);
        profile.setCompletedOrderCount(0);
        feedingProviderProfileRepository.save(profile);
    }

    private FileObject createReadyFile(Long ownerUserId, FileBizType bizType) {
        FileObject file = new FileObject();
        file.setOwnerUserId(ownerUserId);
        file.setBucket("local");
        file.setObjectKey("tests/feeding-" + UUID.randomUUID() + ".jpg");
        file.setBizType(bizType);
        file.setFileName("feeding.jpg");
        file.setMimeType("image/jpeg");
        file.setFileSize(1024L);
        file.setStatus(FileStatus.READY);
        file.setPublicUrl("/api/v1/files/content/test-feeding");
        return fileObjectRepository.save(file);
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setUserAuth(long userId) {
        AuthPrincipal principal = new AuthPrincipal(userId, AuthPrincipalType.USER, "USER");
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
