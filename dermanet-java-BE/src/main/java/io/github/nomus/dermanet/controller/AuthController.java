package io.github.nomus.dermanet.controller;

import io.github.nomus.dermanet.dto.AuthRequest;
import io.github.nomus.dermanet.dto.AuthResponse;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.signup(request, response);
        return ResponseEntity.status(201).body(authResponse);
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request, HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        authService.logout(response, refreshToken);
        return ResponseEntity.ok().body("{\"message\": \"Logged out successfully\"}");
    }
    
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }
        authService.refreshToken(refreshToken, response);
        return ResponseEntity.ok().body("{\"message\": \"Token refreshed successfully\"}");
    }
    
    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile(@AuthenticationPrincipal User currentUser) {
        AuthResponse profile = authService.getProfile(currentUser);
        return ResponseEntity.ok(profile);
    }
}