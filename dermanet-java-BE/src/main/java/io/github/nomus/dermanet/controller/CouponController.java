package io.github.nomus.dermanet.controller;

import io.github.nomus.dermanet.dto.CouponResponse;
import io.github.nomus.dermanet.dto.ValidateCouponRequest;
import io.github.nomus.dermanet.dto.ValidateCouponResponse;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    @GetMapping
    public ResponseEntity<CouponResponse> getCoupon(@AuthenticationPrincipal User currentUser) {
        CouponResponse coupon = couponService.getCoupon(currentUser);
        return ResponseEntity.ok(coupon);
    }
    
    @PostMapping("/validate")
    public ResponseEntity<ValidateCouponResponse> validateCoupon(
            @RequestBody ValidateCouponRequest request,
            @AuthenticationPrincipal User currentUser) {
        ValidateCouponResponse response = couponService.validateCoupon(currentUser, request);
        return ResponseEntity.ok(response);
    }
}