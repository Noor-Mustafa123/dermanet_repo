package io.github.nomus.dermanet.controller;

import io.github.nomus.dermanet.dto.CartItemRequest;
import io.github.nomus.dermanet.dto.CartItemResponse;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartProducts(@AuthenticationPrincipal User currentUser) {
        List<CartItemResponse> cartItems = cartService.getCartProducts(currentUser);
        return ResponseEntity.ok(cartItems);
    }
    
    @PostMapping
    public ResponseEntity<List<CartItemResponse>> addToCart(
            @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User currentUser) {
        List<CartItemResponse> cartItems = cartService.addToCart(currentUser, request);
        return ResponseEntity.ok(cartItems);
    }
    
    @DeleteMapping
    public ResponseEntity<List<CartItemResponse>> removeAllFromCart(
            @RequestParam(required = false) Long productId,
            @AuthenticationPrincipal User currentUser) {
        List<CartItemResponse> cartItems = cartService.removeAllFromCart(currentUser, productId);
        return ResponseEntity.ok(cartItems);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<List<CartItemResponse>> updateQuantity(
            @PathVariable Long id,
            @RequestBody CartItemRequest request,
            @AuthenticationPrincipal User currentUser) {
        List<CartItemResponse> cartItems = cartService.updateQuantity(currentUser, id, request.getQuantity());
        return ResponseEntity.ok(cartItems);
    }
}