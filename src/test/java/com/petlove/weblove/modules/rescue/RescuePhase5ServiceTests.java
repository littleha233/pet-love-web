package com.petlove.weblove.modules.rescue;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.admin.entity.AdminAuditLog;
import com.petlove.weblove.modules.admin.entity.AdminUser;
import com.petlove.weblove.modules.admin.enums.AdminRole;
import com.petlove.weblove.modules.admin.enums.AdminStatus;
import com.petlove.weblove.modules.admin.repository.AdminAuditLogRepository;
import com.petlove.weblove.modules.admin.repository.AdminUserRepository;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileBizType;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.rescue.dto.admin.UpdateRescueClueStatusRequest;
import com.petlove.weblove.modules.rescue.dto.user.MyRescueClueQuery;
import com.petlove.weblove.modules.rescue.dto.user.RescueClueDetailDTO;
import com.petlove.weblove.modules.rescue.dto.user.RescueGuideListQuery;
import com.petlove.weblove.modules.rescue.dto.user.RescueResourceListQuery;
import com.petlove.weblove.modules.rescue.dto.user.SubmitRescueClueRequest;
import com.petlove.weblove.modules.rescue.entity.RescueGuide;
import com.petlove.weblove.modules.rescue.entity.RescueResource;
import com.petlove.weblove.modules.rescue.enums.RescueGuideScenarioCode;
import com.petlove.weblove.modules.rescue.enums.RescueGuideStatus;
import com.petlove.weblove.modules.rescue.enums.RescueResourceStatus;
import com.petlove.weblove.modules.rescue.enums.RescueResourceType;
import com.petlove.weblove.modules.rescue.repository.RescueGuideRepository;
import com.petlove.weblove.modules.rescue.repository.RescueResourceRepository;
import com.petlove.weblove.modules.rescue.service.AdminRescueService;
import com.petlove.weblove.modules.rescue.service.RescueClueService;
import com.petlove.weblove.modules.rescue.service.RescueGuideService;
import com.petlove.weblove.modules.rescue.service.RescueResourceService;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.enums.LoginType;
import com.petlove.weblove.modules.user.enums.UserStatus;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.security.AuthPrincipal;
import com.petlove.weblove.security.AuthPrincipalType;
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
class RescuePhase5ServiceTests {

    @Autowired
    private RescueGuideService rescueGuideService;

    @Autowired
    private RescueResourceService rescueResourceService;

    @Autowired
    private RescueClueService rescueClueService;

    @Autowired
    private AdminRescueService adminRescueService;

    @Autowired
    private RescueGuideRepository rescueGuideRepository;

    @Autowired
    private RescueResourceRepository rescueResourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicList_onlyReturnsPublishedGuideAndActiveResource() {
        RescueGuide publishedGuide = new RescueGuide();
        publishedGuide.setScenarioCode(RescueGuideScenarioCode.FOUND_STRAY_CAT);
        publishedGuide.setTitle("已发布指引");
        publishedGuide.setContentMd("content");
        publishedGuide.setSortOrder(1);
        publishedGuide.setStatus(RescueGuideStatus.PUBLISHED);
        publishedGuide.setVersion(1);
        rescueGuideRepository.save(publishedGuide);

        RescueGuide draftGuide = new RescueGuide();
        draftGuide.setScenarioCode(RescueGuideScenarioCode.FOUND_STRAY_DOG);
        draftGuide.setTitle("草稿指引");
        draftGuide.setContentMd("content");
        draftGuide.setSortOrder(2);
        draftGuide.setStatus(RescueGuideStatus.DRAFT);
        draftGuide.setVersion(1);
        rescueGuideRepository.save(draftGuide);

        RescueGuideListQuery guideQuery = new RescueGuideListQuery();
        guideQuery.setPage(1);
        guideQuery.setPageSize(20);

        var guidePage = rescueGuideService.list(guideQuery);
        assertTrue(guidePage.items().stream().anyMatch(item -> item.guideId().equals(publishedGuide.getId())));
        assertTrue(guidePage.items().stream().noneMatch(item -> item.guideId().equals(draftGuide.getId())));

        RescueResource activeResource = new RescueResource();
        activeResource.setResourceType(RescueResourceType.ANIMAL_HOSPITAL);
        activeResource.setName("启用资源");
        activeResource.setCityCode("310100");
        activeResource.setCityName("上海");
        activeResource.setStatus(RescueResourceStatus.ACTIVE);
        activeResource.setSortOrder(1);
        rescueResourceRepository.save(activeResource);

        RescueResource draftResource = new RescueResource();
        draftResource.setResourceType(RescueResourceType.NGO);
        draftResource.setName("草稿资源");
        draftResource.setCityCode("310100");
        draftResource.setCityName("上海");
        draftResource.setStatus(RescueResourceStatus.DRAFT);
        draftResource.setSortOrder(2);
        rescueResourceRepository.save(draftResource);

        RescueResourceListQuery resourceQuery = new RescueResourceListQuery();
        resourceQuery.setPage(1);
        resourceQuery.setPageSize(20);

        var resourcePage = rescueResourceService.list(resourceQuery);
        assertTrue(resourcePage.items().stream().anyMatch(item -> item.resourceId().equals(activeResource.getId())));
        assertTrue(resourcePage.items().stream().noneMatch(item -> item.resourceId().equals(draftResource.getId())));
    }

    @Test
    void submitClue_validatesPhotoOwnership_andCreatesSubmitted() {
        User owner = createUser("13700000001");
        createProfile(owner.getId(), "线索用户");

        User other = createUser("13700000002");
        createProfile(other.getId(), "其他用户");

        setUserAuth(owner.getId());

        SubmitRescueClueRequest request = minimalClueRequest();
        FileObject otherFile = createReadyFile(other.getId(), FileBizType.RESCUE_CLUE);
        request.setPhotoFileIds(List.of(otherFile.getId()));

        BizException ownershipEx = assertThrows(BizException.class, () -> rescueClueService.submit(request));
        assertEquals(ErrorCode.RESCUE_CLUE_FILE_NOT_OWNED, ownershipEx.getErrorCode());

        FileObject ownerFile = createReadyFile(owner.getId(), FileBizType.RESCUE_CLUE);
        request.setPhotoFileIds(List.of(ownerFile.getId()));

        RescueClueDetailDTO detail = rescueClueService.submit(request);
        assertEquals("SUBMITTED", detail.status());
        assertEquals(1, detail.photos().size());

        MyRescueClueQuery myQuery = new MyRescueClueQuery();
        myQuery.setPage(1);
        myQuery.setPageSize(20);
        var myPage = rescueClueService.myClues(myQuery);
        assertTrue(myPage.items().stream().anyMatch(item -> item.clueId().equals(detail.clueId())));
    }

    @Test
    void adminUpdateClueStatus_enforcesTransition_andWritesAuditLog() {
        User reporter = createUser("13700000010");
        createProfile(reporter.getId(), "提报用户");

        setUserAuth(reporter.getId());
        RescueClueDetailDTO clue = rescueClueService.submit(minimalClueRequest());

        AdminUser auditor = createAdmin("rescue_auditor", AdminRole.AUDITOR);
        setAdminAuth(auditor.getId(), auditor.getRole());

        UpdateRescueClueStatusRequest invalidRequest = new UpdateRescueClueStatusRequest();
        invalidRequest.setStatus("RESOLVED");
        BizException ex = assertThrows(
            BizException.class,
            () -> adminRescueService.updateClueStatus(clue.clueId(), invalidRequest)
        );
        assertEquals(ErrorCode.RESCUE_CLUE_STATUS_INVALID, ex.getErrorCode());

        RescueResource sameCityResource = new RescueResource();
        sameCityResource.setResourceType(RescueResourceType.ANIMAL_HOSPITAL);
        sameCityResource.setName("同城医院");
        sameCityResource.setCityCode("310100");
        sameCityResource.setCityName("上海");
        sameCityResource.setStatus(RescueResourceStatus.ACTIVE);
        sameCityResource.setSortOrder(1);
        rescueResourceRepository.save(sameCityResource);

        UpdateRescueClueStatusRequest triageRequest = new UpdateRescueClueStatusRequest();
        triageRequest.setStatus("TRIAGED");
        triageRequest.setTriageNote("建议先联系同城医院");
        triageRequest.setSuggestedResourceIds(List.of(sameCityResource.getId()));

        var updated = adminRescueService.updateClueStatus(clue.clueId(), triageRequest);
        assertEquals("TRIAGED", updated.status());
        assertEquals(1, updated.suggestedResourceIds().size());

        AdminAuditLog matched = adminAuditLogRepository.findAll().stream()
            .filter(log -> "UPDATE_RESCUE_CLUE_STATUS".equals(log.getAction()))
            .filter(log -> String.valueOf(clue.clueId()).equals(log.getTargetId()))
            .findFirst()
            .orElse(null);

        assertNotNull(matched);
        assertEquals(auditor.getId(), matched.getAdminUserId());
    }

    private SubmitRescueClueRequest minimalClueRequest() {
        SubmitRescueClueRequest request = new SubmitRescueClueRequest();
        request.setCityCode("310100");
        request.setCityName("上海");
        request.setDistrictName("浦东新区");
        request.setLocationText("世纪大道 100 号附近");
        request.setPetType("CAT");
        request.setEstimatedCount(1);
        request.setUrgencyLevel("MEDIUM");
        request.setConditionTags(List.of("INJURED"));
        request.setDescription("发现一只受伤流浪猫，行动较慢");
        request.setContactName("测试联系人");
        request.setContactMobile("13712345678");
        return request;
    }

    private User createUser(String mobile) {
        User user = new User();
        user.setMobile(mobile);
        user.setLoginType(LoginType.MOBILE_OTP);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private UserProfile createProfile(Long userId, String nickname) {
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setNickname(nickname);
        profile.setRealNameVerified(true);
        profile.setProviderVerified(false);
        return userProfileRepository.save(profile);
    }

    private FileObject createReadyFile(Long ownerUserId, FileBizType bizType) {
        FileObject file = new FileObject();
        file.setOwnerUserId(ownerUserId);
        file.setBucket("local");
        file.setObjectKey("tests/rescue-" + UUID.randomUUID() + ".jpg");
        file.setBizType(bizType);
        file.setFileName("rescue.jpg");
        file.setMimeType("image/jpeg");
        file.setFileSize(1024L);
        file.setStatus(FileStatus.READY);
        file.setPublicUrl("/api/v1/files/content/test-rescue");
        return fileObjectRepository.save(file);
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
