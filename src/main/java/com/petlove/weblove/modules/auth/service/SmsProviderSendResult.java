package com.petlove.weblove.modules.auth.service;

public record SmsProviderSendResult(
    boolean success,
    String provider,
    String templateCode,
    String signName,
    String providerMessageId,
    String responsePayload,
    String errorCode,
    String errorMessage
) {

    public static SmsProviderSendResult success(String provider,
                                                String templateCode,
                                                String signName,
                                                String providerMessageId,
                                                String responsePayload) {
        return new SmsProviderSendResult(true, provider, templateCode, signName, providerMessageId, responsePayload, null, null);
    }

    public static SmsProviderSendResult failed(String provider,
                                               String templateCode,
                                               String signName,
                                               String errorCode,
                                               String errorMessage,
                                               String responsePayload) {
        return new SmsProviderSendResult(false, provider, templateCode, signName, null, responsePayload, errorCode, errorMessage);
    }
}
