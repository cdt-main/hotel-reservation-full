package com.wd50.Hotel_Reservation.service;

import com.wd50.Hotel_Reservation.model.PromoCode;
import com.wd50.Hotel_Reservation.repository.PromoCodeRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class PromoCodeService extends BaseService<PromoCode> {

    private final PromoCodeRepository promoCodeRepository;

    public PromoCodeService(PromoCodeRepository promoCodeRepository) {
        super(promoCodeRepository);
        this.promoCodeRepository = promoCodeRepository;
    }

    public PromoCode update(Long id, PromoCode updated) {
        PromoCode existing = getById(id);
        if (updated.getCode() != null && !updated.getCode().isBlank()) {
            String newCode = updated.getCode().trim().toUpperCase();
            if (!newCode.equalsIgnoreCase(existing.getCode())) {
                Optional<PromoCode> conflict = promoCodeRepository.findByCodeIgnoreCase(newCode);
                if (conflict.isPresent() && !conflict.get().getId().equals(existing.getId())) {
                    throw new IllegalArgumentException("Promo code already exists: " + newCode);
                }
            }
            existing.setCode(newCode);
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription().trim());
        }
        if (updated.getDiscountType() != null) {
            existing.setDiscountType(updated.getDiscountType());
        }
        if (updated.getDiscountValue() != null) {
            existing.setDiscountValue(updated.getDiscountValue());
        }
        if (updated.getMinSpend() != null) {
            existing.setMinSpend(updated.getMinSpend());
        }
        if (updated.getIsActive() != null) {
            existing.setIsActive(updated.getIsActive());
        }
        if (updated.getValidFrom() != null) {
            existing.setValidFrom(updated.getValidFrom());
        }
        if (updated.getValidUntil() != null) {
            existing.setValidUntil(updated.getValidUntil());
        }
        if (updated.getMaxDiscountAmount() != null) {
            existing.setMaxDiscountAmount(updated.getMaxDiscountAmount());
        }
        if (updated.getUsageLimit() != null) {
            existing.setUsageLimit(updated.getUsageLimit());
        }
        return save(existing);
    }

    public void deleteByIdOrCode(String idOrCode) {
        if (idOrCode == null || idOrCode.isBlank()) {
            throw new IllegalArgumentException("Promo ID or code required");
        }
        try {
            Long id = Long.parseLong(idOrCode.trim());
            delete(id);
            return;
        } catch (NumberFormatException ignored) {}

        Optional<PromoCode> opt = promoCodeRepository.findByCodeIgnoreCase(idOrCode.trim());
        if (opt.isPresent()) {
            delete(opt.get().getId());
        } else {
            throw new IllegalArgumentException("Promo code not found: " + idOrCode);
        }
    }

    public Map<String, Object> validate(String code, Double subtotal) {
        Map<String, Object> result = new HashMap<>();
        if (code == null || code.isBlank()) {
            result.put("valid", false);
            result.put("message", "Promo code cannot be empty");
            return result;
        }

        Optional<PromoCode> promoOpt = promoCodeRepository.findByCodeIgnoreCaseAndIsActiveTrue(code.trim());
        if (promoOpt.isEmpty()) {
            result.put("valid", false);
            result.put("message", "Invalid promo code. Try 'SERENE10' for 10% off.");
            return result;
        }

        PromoCode promo = promoOpt.get();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (promo.getValidFrom() != null && now.isBefore(promo.getValidFrom())) {
            result.put("valid", false);
            result.put("message", "This promo code is not active yet.");
            return result;
        }
        if (promo.getValidUntil() != null && now.isAfter(promo.getValidUntil())) {
            result.put("valid", false);
            result.put("message", "This promo code has expired.");
            return result;
        }
        if (subtotal != null && promo.getMinSpend() != null && subtotal < promo.getMinSpend()) {
            result.put("valid", false);
            result.put("message", "Minimum spend of LKR " + promo.getMinSpend() + " required for this code.");
            return result;
        }

        double discountRate = promo.getDiscountValue() / 100.0;
        double discountAmount = promo.calculateDiscount(subtotal);

        result.put("valid", true);
        result.put("code", promo.getCode());
        result.put("discountRate", discountRate);
        result.put("discountAmount", discountAmount);
        result.put("description", promo.getDescription());
        return result;
    }
}
