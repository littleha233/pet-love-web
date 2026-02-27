package com.petlove.weblove.modules.auth.entity;

import com.petlove.weblove.common.util.CreatedOnlyEntity;
import com.petlove.weblove.modules.auth.enums.SmsBizType;
import com.petlove.weblove.modules.auth.enums.SmsSendStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sms_send_logs")
public class SmsSendLog extends CreatedOnlyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "biz_type", nullable = false, length = 32)
    private SmsBizType bizType;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "template_code", length = 64)
    private String templateCode;

    @Column(name = "sign_name", length = 64)
    private String signName;

    @Column(name = "request_payload", columnDefinition = "json")
    private String requestPayload;

    @Column(name = "response_payload", columnDefinition = "json")
    private String responsePayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "send_status", nullable = false, length = 16)
    private SmsSendStatus sendStatus;

    @Column(name = "error_code", length = 64)
    private String errorCode;

    @Column(name = "error_message", length = 255)
    private String errorMessage;

    @Column(name = "provider_message_id", length = 128)
    private String providerMessageId;

    @Column(name = "client_ip", length = 64)
    private String clientIp;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    public Long getId() {
        return id;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public SmsBizType getBizType() {
        return bizType;
    }

    public void setBizType(SmsBizType bizType) {
        this.bizType = bizType;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public SmsSendStatus getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(SmsSendStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getProviderMessageId() {
        return providerMessageId;
    }

    public void setProviderMessageId(String providerMessageId) {
        this.providerMessageId = providerMessageId;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
