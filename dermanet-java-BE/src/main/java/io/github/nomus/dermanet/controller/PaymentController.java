package io.github.nomus.dermanet.controller;

import io.github.nomus.dermanet.dto.CheckoutRequest;
import io.github.nomus.dermanet.dto.CheckoutResponse;
import io.github.nomus.dermanet.dto.CheckoutSuccessRequest;
import io.github.nomus.dermanet.dto.CheckoutSuccessResponse;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/create-checkout-session")
    public ResponseEntity<CheckoutResponse> createCheckoutSession(
            @RequestBody CheckoutRequest request,
            @AuthenticationPrincipal User currentUser) {
        CheckoutResponse response = paymentService.createCheckoutSession(request, currentUser);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/checkout-success")
    public ResponseEntity<CheckoutSuccessResponse> checkoutSuccess(
            @RequestBody CheckoutSuccessRequest request,
            @AuthenticationPrincipal User currentUser) {
        CheckoutSuccessResponse response = paymentService.checkoutSuccess(request);
        return ResponseEntity.ok(response);
    }
}
