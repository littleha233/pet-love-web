package com.petlove.weblove.modules.auth.service;

import com.petlove.weblove.modules.auth.enums.SmsBizType;

public interface SmsProviderClient {

    SmsProviderSendResult sendCode(String mobile, SmsBizType bizType, String code);
}
