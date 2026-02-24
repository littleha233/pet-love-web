package com.petlove.weblove.modules.admin.service;

import com.petlove.weblove.common.api.PageResponse;
import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.common.util.MaskUtil;
import com.petlove.weblove.modules.admin.dto.AdminUserListItemDTO;
import com.petlove.weblove.modules.admin.dto.UpdateUserStatusRequest;
import com.petlove.weblove.modules.user.entity.User;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.enums.UserStatus;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.modules.user.repository.UserRepository;
import com.petlove.weblove.security.AuthPrincipal;
import com.petlove.weblove.security.SecurityUtils;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AdminAuditService adminAuditService;

    public AdminUserService(UserRepository userRepository,
                            UserProfileRepository userProfileRepository,
                            AdminAuditService adminAuditService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.adminAuditService = adminAuditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserListItemDTO> listUsers(int page, int pageSize, String status, String keyword) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), Math.min(Math.max(pageSize, 1), 100), Sort.by(Sort.Direction.DESC, "id"));

        Page<User> userPage;
        if (status != null && !status.isBlank() && keyword != null && !keyword.isBlank()) {
            UserStatus userStatus = UserStatus.valueOf(status);
            userPage = userRepository.findByStatusAndMobileContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCase(
                userStatus,
                keyword,
                userStatus,
                keyword,
                pageable
            );
        } else if (status != null && !status.isBlank()) {
            userPage = userRepository.findByStatus(UserStatus.valueOf(status), pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            userPage = userRepository.findByMobileContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        Map<Long, UserProfile> profileMap = userProfileRepository.findByUserIdIn(
                userPage.getContent().stream().map(User::getId).toList())
            .stream().collect(Collectors.toMap(UserProfile::getUserId, p -> p));

        return new PageResponse<>(
            userPage.getContent().stream().map(user -> {
                UserProfile profile = profileMap.get(user.getId());
                return new AdminUserListItemDTO(
                    user.getId(),
                    MaskUtil.maskMobile(user.getMobile()),
                    MaskUtil.maskEmail(user.getEmail()),
                    user.getStatus().name(),
                    profile != null ? profile.getNickname() : null,
                    profile != null ? profile.getCityName() : null
                );
            }).toList(),
            page,
            pageSize,
            userPage.getTotalElements()
        );
    }

    @PreAuthorize("hasAnyAuthority('ROLE_SUPER_ADMIN','ROLE_CS')")
    @Transactional
    public void updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User not found"));

        AuthPrincipal principal = SecurityUtils.currentPrincipal();
        boolean isSuperAdmin = Objects.equals(principal.role(), "SUPER_ADMIN");

        if (!user.getStatus().canTransitTo(request.getStatus(), isSuperAdmin)) {
            throw new BizException(ErrorCode.STATE_TRANSITION_INVALID,
                "Invalid status transition from %s to %s".formatted(user.getStatus(), request.getStatus()));
        }

        UserStatus before = user.getStatus();
        user.setStatus(request.getStatus());
        userRepository.save(user);

        adminAuditService.writeAuditLog(
            principal.id(),
            "UPDATE_USER_STATUS",
            "USER",
            String.valueOf(userId),
            before,
            request.getStatus(),
            request.getReason()
        );
    }
}
