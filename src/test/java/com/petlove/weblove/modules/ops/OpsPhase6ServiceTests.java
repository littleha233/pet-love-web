package com.petlove.weblove.modules.ops;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.dto.user.CreateRehomePostRequest;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostDetailDTO;
import com.petlove.weblove.modules.adoption.entity.AdoptionPost;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import com.petlove.weblove.modules.adoption.repository.AdoptionPostRepository;
import com.petlove.weblove.modules.adoption.service.AdoptionPostService;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.enums.AdminRole;
import com.petlove.weblove.modules.admin.enums.AdminStatus;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.ops.dto.admin.AdminReplyComplaintTicketRequest;
import com.petlove.weblove.modules.ops.dto.admin.UpsertBlacklistEntryRequest;
import com.petlove.weblove.modules.ops.dto.admin.UpsertCityFeatureSwitchRequest;
import com.petlove.weblove.modules.ops.dto.user.ComplaintTicketDetailDTO;
import com.petlove.weblove.modules.ops.dto.user.SubmitComplaintTicketRequest;
import com.petlove.weblove.modules.ops.service.AdminComplaintService;
import com.petlove.weblove.modules.ops.service.CityFeatureSwitchService;
import com.petlove.weblove.modules.ops.service.ComplaintTicketService;
import com.petlove.weblove.modules.ops.service.RiskBlacklistService;
import com.petlove.weblove.modules.rescue.dto.user.SubmitRescueClueRequest;
import com.petlove.weblove.modules.rescue.service.RescueClueService;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.enums.LoginType;
import com.petlove.weblove.modules.user.enums.UserStatus;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.security.AuthPrincipal;
import com.petlove.weblove.security.AuthPrincipalType;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class OpsPhase6ServiceTests {

    @Autowired
    private ComplaintTicketService complaintTicketService;

    @Autowired
    private AdminComplaintService adminComplaintService;

    @Autowired
    private RiskBlacklistService riskBlacklistService;

    @Autowired
    private CityFeatureSwitchService cityFeatureSwitchService;

    @Autowired
    private AdoptionPostService adoptionPostService;

    @Autowired
    private AdoptionPostRepository adoptionPostRepository;

    @Autowired
    private RescueClueService rescueClueService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void complaintInternalNote_isHiddenFromUserDetail() {
        User reporter = createUser("13610000001");
        createProfile(reporter.getId(), true, "投诉用户");

        setUserAuth(reporter.getId());
        SubmitComplaintTicketRequest submitRequest = new SubmitComplaintTicketRequest();
        submitRequest.setTargetType("OTHER");
        submitRequest.setTitle("投诉标题");
        submitRequest.setContent("投诉内容");
        ComplaintTicketDetailDTO created = complaintTicketService.submit(submitRequest);

        AdminUser admin = createAdmin("ops_phase6_note", AdminRole.CS);
        setAdminAuth(admin.getId(), admin.getRole());
        AdminReplyComplaintTicketRequest replyRequest = new AdminReplyComplaintTicketRequest();
        replyRequest.setContent("这是内部备注");
        replyRequest.setIsInternalNote(true);
        adminComplaintService.reply(created.ticketId(), replyRequest);

        setUserAuth(reporter.getId());
        ComplaintTicketDetailDTO userView = complaintTicketService.myDetail(created.ticketId());
        assertTrue(userView.replies().isEmpty());
    }

    @Test
    void blacklistBlocks_adoptionPostCreate() {
        User user = createUser("13610000002");
        createProfile(user.getId(), true, "黑名单用户");
        FileObject image = createReadyFile(user.getId(), FileBizType.PET_MEDIA);

        AdminUser admin = createAdmin("ops_phase6_blacklist", AdminRole.OPS);
        setAdminAuth(admin.getId(), admin.getRole());

        UpsertBlacklistEntryRequest blacklistRequest = new UpsertBlacklistEntryRequest();
        blacklistRequest.setSubjectType("USER_ID");
        blacklistRequest.setSubjectValue(String.valueOf(user.getId()));
        blacklistRequest.setScopeType("ACTION");
        blacklistRequest.setScopeValue("ADOPTION_POST_CREATE");
        blacklistRequest.setActionMode("BLOCK");
        blacklistRequest.setReasonCode("RISK_MANUAL_BLOCK");
        blacklistRequest.setReasonNote("命中风控测试");
        blacklistRequest.setStartAt(LocalDateTime.now().minusHours(1).toString());
        blacklistRequest.setStatus("ACTIVE");
        riskBlacklistService.upsert(0L, blacklistRequest);

        setUserAuth(user.getId());
        CreateRehomePostRequest createRequest = new CreateRehomePostRequest();
        createRequest.setTitle("黑名单送养帖");
        createRequest.setContent("黑名单用户发布");
        createRequest.setCityCode("310100");
        createRequest.setCityName("上海");
        createRequest.setPetType(PetType.CAT);
        createRequest.setPetImageFileIds(List.of(image.getId()));

        BizException ex = assertThrows(BizException.class, () -> adoptionPostService.create(createRequest));
        assertEquals(ErrorCode.RISK_BLACKLIST_BLOCKED, ex.getErrorCode());
    }

    @Test
    void cityFeatureWriteDisabled_blocksRescueClueSubmit() {
        User user = createUser("13610000003");
        createProfile(user.getId(), true, "开关测试用户");

        AdminUser admin = createAdmin("ops_phase6_city_switch", AdminRole.OPS);
        setAdminAuth(admin.getId(), admin.getRole());

        UpsertCityFeatureSwitchRequest switchRequest = new UpsertCityFeatureSwitchRequest();
        switchRequest.setCityCode("310100");
        switchRequest.setCityName("上海");
        switchRequest.setFeatureKey("RESCUE_CLUE_SUBMIT");
        switchRequest.setIsEnabled(true);
        switchRequest.setAllowRead(true);
        switchRequest.setAllowWrite(false);
        switchRequest.setNoticeText("该城市暂不支持线索提报");
        cityFeatureSwitchService.upsert(0L, switchRequest);

        setUserAuth(user.getId());
        SubmitRescueClueRequest clueRequest = new SubmitRescueClueRequest();
        clueRequest.setCityCode("310100");
        clueRequest.setCityName("上海");
        clueRequest.setDistrictName("浦东新区");
        clueRequest.setLocationText("世纪大道 100 号");
        clueRequest.setPetType("CAT");
        clueRequest.setEstimatedCount(1);
        clueRequest.setUrgencyLevel("MEDIUM");
        clueRequest.setDescription("城市开关写入拦截测试");
        clueRequest.setContactName("测试联系人");
        clueRequest.setContactMobile("13612345678");

        BizException ex = assertThrows(BizException.class, () -> rescueClueService.submit(clueRequest));
        assertEquals(ErrorCode.CITY_FEATURE_WRITE_DISABLED, ex.getErrorCode());
        assertNotNull(ex.getMessage());
    }

    @Test
    void cityFeatureReadDisabled_filtersAdoptionListWithoutCityQuery() {
        User blockedCityPublisher = createUser("13610000004");
        createProfile(blockedCityPublisher.getId(), true, "上海发布者");
        FileObject blockedCityImage = createReadyFile(blockedCityPublisher.getId(), FileBizType.PET_MEDIA);

        User openCityPublisher = createUser("13610000005");
        createProfile(openCityPublisher.getId(), true, "北京发布者");
        FileObject openCityImage = createReadyFile(openCityPublisher.getId(), FileBizType.PET_MEDIA);

        setUserAuth(blockedCityPublisher.getId());
        RehomePostDetailDTO blockedCityPost = adoptionPostService.create(buildRehomePostRequest("310100", "上海", blockedCityImage.getId()));
        publishPost(blockedCityPost.postId());

        setUserAuth(openCityPublisher.getId());
        RehomePostDetailDTO openCityPost = adoptionPostService.create(buildRehomePostRequest("330100", "杭州", openCityImage.getId()));
        publishPost(openCityPost.postId());

        AdminUser admin = createAdmin("ops_phase6_city_read_filter", AdminRole.OPS);
        setAdminAuth(admin.getId(), admin.getRole());

        UpsertCityFeatureSwitchRequest switchRequest = new UpsertCityFeatureSwitchRequest();
        switchRequest.setCityCode("310100");
        switchRequest.setCityName("上海");
        switchRequest.setFeatureKey("ADOPTION");
        switchRequest.setIsEnabled(true);
        switchRequest.setAllowRead(false);
        switchRequest.setAllowWrite(true);
        switchRequest.setNoticeText("上海暂不开放领养浏览");
        cityFeatureSwitchService.upsert(0L, switchRequest);

        var listResult = adoptionPostService.listPublishedPosts(1, 20, null, null, null);

        assertTrue(listResult.items().stream().anyMatch(item -> "330100".equals(item.cityCode())));
        assertFalse(listResult.items().stream().anyMatch(item -> "310100".equals(item.cityCode())));
    }

    private User createUser(String mobile) {
        User user = new User();
        user.setMobile(mobile);
        user.setLoginType(LoginType.MOBILE_OTP);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private UserProfile createProfile(Long userId, boolean realNameVerified, String nickname) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNickname(nickname);
        profile.setRealNameVerified(realNameVerified);
        profile.setProviderVerified(false);
        return userProfileRepository.save(profile);
    }

    private FileObject createReadyFile(Long ownerUserId, FileBizType bizType) {
        FileObject file = new FileObject();
        file.setOwnerUserId(ownerUserId);
        file.setBucket("local");
        file.setObjectKey("tests/phase6-" + UUID.randomUUID() + ".jpg");
        file.setBizType(bizType);
        file.setFileName("phase6.jpg");
        file.setMimeType("image/jpeg");
        file.setFileSize(1024L);
        file.setStatus(FileStatus.READY);
        file.setPublicUrl("/api/v1/files/content/test-phase6");
        return fileObjectRepository.save(file);
    }

    private CreateRehomePostRequest buildRehomePostRequest(String cityCode, String cityName, Long imageFileId) {
        CreateRehomePostRequest request = new CreateRehomePostRequest();
        request.setTitle("测试送养帖-" + cityCode);
        request.setContent("用于城市读开关测试");
        request.setCityCode(cityCode);
        request.setCityName(cityName);
        request.setPetType(PetType.CAT);
        request.setPetImageFileIds(List.of(imageFileId));
        return request;
    }

    private void publishPost(Long postId) {
        AdoptionPost post = adoptionPostRepository.findById(postId).orElseThrow();
        post.setStatus(AdoptionPostStatus.PUBLISHED);
        post.setPublishedAt(LocalDateTime.now());
        adoptionPostRepository.save(post);
    }

    private AdminUser createAdmin(String username, AdminRole role) {
        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash("hash");
        admin.setDisplayName(username);
        admin.setRole(role);
        admin.setStatus(AdminStatus.ACTIVE);
        return adminUserRepository.save(admin);
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

    private void setAdminAuth(long adminId, AdminRole role) {
        AuthPrincipal principal = new AuthPrincipal(adminId, AuthPrincipalType.ADMIN, role.name());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_" + role.name())
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
