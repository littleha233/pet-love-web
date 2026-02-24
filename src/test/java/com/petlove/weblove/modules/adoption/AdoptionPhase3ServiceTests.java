package com.petlove.weblove.modules.adoption;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.adoption.dto.admin.ApproveAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.dto.admin.RejectAdoptionPostRequest;
import com.petlove.weblove.modules.adoption.dto.user.CreateRehomePostRequest;
import com.petlove.weblove.modules.adoption.dto.user.HandleAdoptionApplicationRequest;
import com.petlove.weblove.modules.adoption.dto.user.RehomePostDetailDTO;
import com.petlove.weblove.modules.adoption.dto.user.SubmitAdoptionApplicationRequest;
import com.petlove.weblove.modules.adoption.entity.AdoptionApplication;
import com.petlove.weblove.modules.adoption.entity.AdoptionPost;
import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationHandleAction;
import com.petlove.weblove.modules.adoption.enums.AdoptionApplicationStatus;
import com.petlove.weblove.modules.adoption.enums.AdoptionPostStatus;
import com.petlove.weblove.modules.adoption.enums.PetType;
import com.petlove.weblove.modules.adoption.repository.AdoptionApplicationRepository;
import com.petlove.weblove.modules.adoption.repository.AdoptionPostRepository;
import com.petlove.weblove.modules.adoption.service.AdoptionApplicationService;
import com.petlove.weblove.modules.adoption.service.AdoptionPostService;
import com.petlove.weblove.modules.adoption.service.AdminAdoptionService;
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
class AdoptionPhase3ServiceTests {

    @Autowired
    private AdoptionPostService adoptionPostService;

    @Autowired
    private AdoptionApplicationService adoptionApplicationService;

    @Autowired
    private AdminAdoptionService adminAdoptionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private FileObjectRepository fileObjectRepository;

    @Autowired
    private AdoptionPostRepository adoptionPostRepository;

    @Autowired
    private AdoptionApplicationRepository adoptionApplicationRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private AdminAuditLogRepository adminAuditLogRepository;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPost_requiresRealNameVerification() {
        User user = createUser("13800000001");
        createProfile(user.getId(), false, "未实名用户");
        setUserAuth(user.getId());

        CreateRehomePostRequest request = minimalCreateRequest(List.of(1L));

        BizException ex = assertThrows(BizException.class, () -> adoptionPostService.create(request));
        assertEquals(ErrorCode.ADOPTION_REAL_NAME_REQUIRED, ex.getErrorCode());
    }

    @Test
    void acceptApplication_closesPost_andRejectsOtherSubmittedApplications() {
        User publisher = createUser("13800000010");
        createProfile(publisher.getId(), true, "发布者");
        FileObject image = createReadyFile(publisher.getId());

        setUserAuth(publisher.getId());
        RehomePostDetailDTO createdPost = adoptionPostService.create(minimalCreateRequest(List.of(image.getId())));

        AdminUser auditor = createAdmin("auditor_phase3", AdminRole.AUDITOR);
        setAdminAuth(auditor.getId(), auditor.getRole());
        adminAdoptionService.approve(createdPost.postId(), new ApproveAdoptionPostRequest());

        User applicant1 = createUser("13800000011");
        createProfile(applicant1.getId(), true, "申请人1");
        User applicant2 = createUser("13800000012");
        createProfile(applicant2.getId(), true, "申请人2");

        setUserAuth(applicant1.getId());
        SubmitAdoptionApplicationRequest request1 = new SubmitAdoptionApplicationRequest();
        request1.setMessage("我想领养");
        long applicationId1 = adoptionApplicationService.submit(createdPost.postId(), request1).applicationId();

        setUserAuth(applicant2.getId());
        SubmitAdoptionApplicationRequest request2 = new SubmitAdoptionApplicationRequest();
        request2.setMessage("我也想领养");
        long applicationId2 = adoptionApplicationService.submit(createdPost.postId(), request2).applicationId();

        setUserAuth(publisher.getId());
        HandleAdoptionApplicationRequest handleRequest = new HandleAdoptionApplicationRequest();
        handleRequest.setAction(AdoptionApplicationHandleAction.ACCEPT);
        handleRequest.setDecisionNote("条件符合");
        adoptionApplicationService.handle(applicationId1, handleRequest);

        AdoptionPost post = adoptionPostRepository.findById(createdPost.postId()).orElseThrow();
        assertEquals(AdoptionPostStatus.CLOSED, post.getStatus());

        AdoptionApplication app1 = adoptionApplicationRepository.findById(applicationId1).orElseThrow();
        AdoptionApplication app2 = adoptionApplicationRepository.findById(applicationId2).orElseThrow();
        assertEquals(AdoptionApplicationStatus.ACCEPTED, app1.getStatus());
        assertEquals(AdoptionApplicationStatus.REJECTED, app2.getStatus());
        assertNotNull(app2.getHandledAt());
    }

    @Test
    void rejectPost_writesAdminAuditLog() {
        User publisher = createUser("13800000020");
        createProfile(publisher.getId(), true, "审核测试用户");
        FileObject image = createReadyFile(publisher.getId());

        setUserAuth(publisher.getId());
        RehomePostDetailDTO createdPost = adoptionPostService.create(minimalCreateRequest(List.of(image.getId())));

        AdminUser auditor = createAdmin("auditor_log", AdminRole.AUDITOR);
        setAdminAuth(auditor.getId(), auditor.getRole());

        RejectAdoptionPostRequest rejectRequest = new RejectAdoptionPostRequest();
        rejectRequest.setRejectReasonCode("PET_INFO_INCOMPLETE");
        rejectRequest.setRejectReasonText("宠物信息不完整");
        rejectRequest.setRemark("请补充疫苗信息");

        adminAdoptionService.reject(createdPost.postId(), rejectRequest);

        List<AdminAuditLog> logs = adminAuditLogRepository.findAll();
        AdminAuditLog matched = logs.stream()
            .filter(log -> "REJECT_ADOPTION_POST".equals(log.getAction()))
            .filter(log -> String.valueOf(createdPost.postId()).equals(log.getTargetId()))
            .findFirst()
            .orElse(null);

        assertNotNull(matched);
        assertEquals(auditor.getId(), matched.getAdminUserId());
    }

    private CreateRehomePostRequest minimalCreateRequest(List<Long> fileIds) {
        CreateRehomePostRequest request = new CreateRehomePostRequest();
        request.setTitle("待送养-测试宠物");
        request.setContent("健康活泼，期望认真负责的领养人。");
        request.setCityCode("310100");
        request.setCityName("上海");
        request.setPetType(PetType.CAT);
        request.setPetImageFileIds(fileIds);
        return request;
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

    private FileObject createReadyFile(Long ownerUserId) {
        FileObject file = new FileObject();
        file.setOwnerUserId(ownerUserId);
        file.setBucket("local");
        file.setObjectKey("tests/" + UUID.randomUUID() + ".jpg");
        file.setBizType(FileBizType.PET_MEDIA);
        file.setFileName("pet.jpg");
        file.setMimeType("image/jpeg");
        file.setFileSize(1024L);
        file.setStatus(FileStatus.READY);
        file.setPublicUrl("/api/v1/files/content/test");
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
