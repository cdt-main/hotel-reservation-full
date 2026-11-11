package com.wd50.Hotel_Reservation.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
public class PromoCode extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    private String description;

    @Column(name = "discount_type", length = 30)
    private String discountType = "percentage";

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "min_spend")
    private Double minSpend = 0.0;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "valid_from")
    private LocalDateTime validFrom = LocalDateTime.now();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "valid_until")
    private LocalDateTime validUntil = LocalDateTime.now().plusYears(2);

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count")
    private Integer usedCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    public PromoCode() {
        this.validFrom = LocalDateTime.now();
        this.validUntil = LocalDateTime.now().plusYears(2);
        this.usedCount = 0;
        this.isActive = true;
    }

    public PromoCode(String code, String description, String discountType, Double discountValue, Double minSpend) {
        this();
        this.code = code;
        this.description = description;
        this.discountType = normalizeDiscountType(discountType);
        this.discountValue = discountValue;
        this.minSpend = minSpend != null ? minSpend : 0.0;
        this.isActive = true;
    }

    public PromoCode(String code, String description, String discountType, Double discountValue, Double minSpend, LocalDateTime validFrom, LocalDateTime validUntil) {
        this(code, description, discountType, discountValue, minSpend);
        if (validFrom != null) this.validFrom = validFrom;
        if (validUntil != null) this.validUntil = validUntil;
    }

    @PrePersist
    @PreUpdate
    public void prepareDefaults() {
        if (validFrom == null) {
            validFrom = LocalDateTime.now();
        }
        if (validUntil == null) {
            validUntil = LocalDateTime.now().plusYears(2);
        }
        if (usedCount == null) {
            usedCount = 0;
        }
        if (minSpend == null) {
            minSpend = 0.0;
        }
        if (isActive == null) {
            isActive = true;
        }
        this.discountType = normalizeDiscountType(this.discountType);
    }

    private String normalizeDiscountType(String type) {
        if (type == null || type.isBlank()) {
            return "percentage";
        }
        if ("fixed".equalsIgnoreCase(type.trim())) {
            return "fixed_amount";
        }
        return type.trim().toLowerCase();
    }

    @Override
    public void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Promo code is required.");
        }
        if (discountValue == null || discountValue <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than zero.");
        }
        prepareDefaults();
    }

    public double calculateDiscount(Double subtotal) {
        if (subtotal == null || (minSpend != null && subtotal < minSpend)) {
            return 0.0;
        }
        if ("fixed".equalsIgnoreCase(discountType) || "fixed_amount".equalsIgnoreCase(discountType)) {
            return Math.min(subtotal, discountValue != null ? discountValue : 0.0);
        }
        double discount = Math.round(subtotal * ((discountValue != null ? discountValue : 0.0) / 100.0));
        if (maxDiscountAmount != null && maxDiscountAmount > 0) {
            return Math.min(discount, maxDiscountAmount);
        }
        return discount;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = normalizeDiscountType(discountType); }

    public Double getDiscountValue() { return discountValue; }
    public void setDiscountValue(Double discountValue) { this.discountValue = discountValue; }

    public Double getMinSpend() { return minSpend; }
    public void setMinSpend(Double minSpend) { this.minSpend = minSpend; }

    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }

    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
