package com.petlove.weblove.modules.user.service;

import com.petlove.weblove.common.error.BizException;
import com.petlove.weblove.common.error.ErrorCode;
import com.petlove.weblove.modules.file.entity.FileObject;
import com.petlove.weblove.modules.file.enums.FileStatus;
import com.petlove.weblove.modules.file.repository.FileObjectRepository;
import com.petlove.weblove.modules.system.entity.City;
import com.petlove.weblove.modules.system.repository.CityRepository;
import com.petlove.weblove.modules.user.dto.UpdateUserProfileRequest;
import com.petlove.weblove.modules.user.dto.UserProfileDTO;
import com.petlove.weblove.modules.user.entity.UserProfile;
import com.petlove.weblove.modules.user.repository.UserProfileRepository;
import com.petlove.weblove.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final FileObjectRepository fileObjectRepository;
    private final CityRepository cityRepository;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              FileObjectRepository fileObjectRepository,
                              CityRepository cityRepository) {
        this.userProfileRepository = userProfileRepository;
        this.fileObjectRepository = fileObjectRepository;
        this.cityRepository = cityRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileDTO getMyProfile() {
        long userId = SecurityUtils.currentUserId();
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User profile not found"));
        return toDto(profile);
    }

    @Transactional
    public UserProfileDTO updateMyProfile(UpdateUserProfileRequest request) {
        long userId = SecurityUtils.currentUserId();
        UserProfile profile = userProfileRepository.findByUserId(userId)
            .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "User profile not found"));

        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            profile.setNickname(request.getNickname().trim());
        }

        if (request.getAvatarFileId() != null) {
            FileObject fileObject = fileObjectRepository.findById(request.getAvatarFileId())
                .orElseThrow(() -> new BizException(ErrorCode.NOT_FOUND, "Avatar file not found"));
            if (fileObject.getStatus() != FileStatus.READY) {
                throw new BizException(ErrorCode.FILE_NOT_READY, "Avatar file is not ready");
            }
            profile.setAvatarFileId(fileObject.getId());
            profile.setAvatarUrl(fileObject.getPublicUrl());
        }

        if (request.getCityCode() != null && !request.getCityCode().isBlank()) {
            City city = cityRepository.findByCityCode(request.getCityCode())
                .orElseThrow(() -> new BizException(ErrorCode.INVALID_PARAM, "City code is invalid"));
            profile.setCityCode(city.getCityCode());
            profile.setCityName(city.getCityName());
        }

        if (request.getCityName() != null && !request.getCityName().isBlank() && profile.getCityCode() == null) {
            profile.setCityName(request.getCityName().trim());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio().trim());
        }

        return toDto(userProfileRepository.save(profile));
    }

    private UserProfileDTO toDto(UserProfile profile) {
        return new UserProfileDTO(
            profile.getUserId(),
            profile.getNickname(),
            profile.getAvatarUrl(),
            profile.getCityCode(),
            profile.getCityName(),
            profile.getBio()
        );
    }
}
