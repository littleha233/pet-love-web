package com.petlove.weblove.modules.user.enums;

import java.util.EnumSet;
import java.util.Set;

public enum UserStatus {
    ACTIVE,
    DISABLED,
    BANNED;

    public boolean canTransitTo(UserStatus target, boolean isSuperAdmin) {
        if (this == target) {
            return true;
        }
        return switch (this) {
            case ACTIVE -> EnumSet.of(DISABLED, BANNED).contains(target);
            case DISABLED -> EnumSet.of(ACTIVE, BANNED).contains(target);
            case BANNED -> isSuperAdmin && target == ACTIVE;
        };
    }

    public static Set<UserStatus> blockedStatuses() {
        return EnumSet.of(DISABLED, BANNED);
    }
}
