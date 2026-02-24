package com.petlove.weblove.modules.auth.dto;

import com.petlove.weblove.modules.user.dto.UserProfileDTO;
import java.util.List;

public record CurrentUserDTO(
    long id,
    String mobileMasked,
    String emailMasked,
    String status,
    List<String> roles,
    UserProfileDTO profile
) {
}
