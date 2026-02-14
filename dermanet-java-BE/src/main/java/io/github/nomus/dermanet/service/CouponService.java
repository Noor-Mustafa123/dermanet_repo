package io.github.nomus.dermanet.service;

import io.github.nomus.dermanet.dto.CouponResponse;
import io.github.nomus.dermanet.dto.ValidateCouponRequest;
import io.github.nomus.dermanet.dto.ValidateCouponResponse;
import io.github.nomus.dermanet.entity.Coupon;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.exception.ResourceNotFoundException;
import io.github.nomus.dermanet.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CouponService {
    
    @Autowired
    private CouponRepository couponRepository;
    
    public CouponResponse getCoupon(User user) {
        return couponRepository.findByUserIdAndIsActiveTrue(user.getId())
                .map(this::convertToResponse)
                .orElse(null);
    }
    
    public ValidateCouponResponse validateCoupon(User user, ValidateCouponRequest request) {
        Coupon coupon = couponRepository.findByCodeAndUserIdAndIsActiveTrue(request.getCode(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
        
        // Check if coupon is expired
        if (coupon.getExpirationDate().isBefore(LocalDateTime.now())) {
            coupon.setIsActive(false);
            couponRepository.save(coupon);
            throw new ResourceNotFoundException("Coupon expired");
        }
        
        return ValidateCouponResponse.builder()
                .message("Coupon is valid")
                .code(coupon.getCode())
                .discountPercentage(coupon.getDiscountPercentage())
                .build();
    }
    
    public Coupon createNewCoupon(User user) {
        // Delete existing coupon for user
        couponRepository.findByUserIdAndIsActiveTrue(user.getId())
                .ifPresent(existingCoupon -> {
                    existingCoupon.setIsActive(false);
                    couponRepository.save(existingCoupon);
                });
        
        // Create new coupon
        String code = "GIFT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
        
        Coupon newCoupon = Coupon.builder()
                .code(code)
                .discountPercentage(10)
                .expirationDate(expirationDate)
                .isActive(true)
                .user(user)
                .build();
        
        Coupon savedCoupon = couponRepository.save(newCoupon);
        return savedCoupon;
    }
    
    private CouponResponse convertToResponse(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .discountPercentage(coupon.getDiscountPercentage())
                .isActive(coupon.getIsActive())
                .build();
    }
}