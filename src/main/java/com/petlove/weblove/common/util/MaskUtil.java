package com.petlove.weblove.common.util;

public final class MaskUtil {

    private MaskUtil() {
    }

    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 7) {
            return mobile;
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 4);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@", 2);
        String name = parts[0];
        if (name.length() <= 2) {
            return "**@" + parts[1];
        }
        return name.substring(0, 2) + "***@" + parts[1];
    }
}
