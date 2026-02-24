package com.petlove.weblove.modules.feeding.dto.provider;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProviderRespondOrderRequest {

    @NotBlank
    private String action;

    @DecimalMin("0.00")
    @DecimalMax("99999999.99")
    private BigDecimal quotedTotalAmount;

    @Size(max = 255)
    private String providerResponseNote;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public BigDecimal getQuotedTotalAmount() {
        return quotedTotalAmount;
    }

    public void setQuotedTotalAmount(BigDecimal quotedTotalAmount) {
        this.quotedTotalAmount = quotedTotalAmount;
    }

    public String getProviderResponseNote() {
        return providerResponseNote;
    }

    public void setProviderResponseNote(String providerResponseNote) {
        this.providerResponseNote = providerResponseNote;
    }
}
