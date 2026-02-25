package com.petlove.weblove.modules.risk;

public final class RiskActionKeys {

    public static final String ADOPTION_POST_CREATE = "ADOPTION_POST_CREATE";
    public static final String ADOPTION_APPLICATION_SUBMIT = "ADOPTION_APPLICATION_SUBMIT";
    public static final String FEEDING_PROVIDER_PROFILE_ACTIVATE = "FEEDING_PROVIDER_PROFILE_ACTIVATE";
    public static final String FEEDING_ORDER_CREATE = "FEEDING_ORDER_CREATE";
    public static final String RESCUE_CLUE_SUBMIT = "RESCUE_CLUE_SUBMIT";

    private RiskActionKeys() {
    }

    public static String resolveModule(String actionKey) {
        if (actionKey == null || actionKey.isBlank()) {
            return "GLOBAL";
        }
        int idx = actionKey.indexOf('_');
        if (idx <= 0) {
            return actionKey.trim().toUpperCase();
        }
        return actionKey.substring(0, idx).trim().toUpperCase();
    }
}
