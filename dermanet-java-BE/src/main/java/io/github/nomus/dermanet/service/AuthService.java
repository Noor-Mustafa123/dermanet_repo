package io.github.nomus.dermanet.service;

import io.github.nomus.dermanet.dto.AuthRequest;
import io.github.nomus.dermanet.dto.AuthResponse;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.exception.ResourceNotFoundException;
import io.github.nomus.dermanet.exception.UnauthorizedException;
import io.github.nomus.dermanet.repository.UserRepository;
import io.github.nomus.dermanet.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Value("${app.jwt.accessTokenCookieName}")
    private String accessTokenCookieName;
    
    @Value("${app.jwt.refreshTokenCookieName}")
    private String refreshTokenCookieName;
    
    @Value("${app.jwt.cookieDomain}")
    private String cookieDomain;
    
    @Value("${app.jwt.secureCookie}")
    private boolean secureCookie;
    
    public AuthResponse signup(AuthRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UnauthorizedException("User already exists");
        }
        
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        // Store refresh token in Redis
        storeRefreshToken(user.getId(), refreshToken);
        
        // Set cookies
        setAuthCookies(response, accessToken, refreshToken);
        
        return AuthResponse.builder()
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }
    
    public AuthResponse login(AuthRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Generate tokens
            String accessToken = jwtUtil.generateToken(user.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());
            
            // Store refresh token in Redis
            storeRefreshToken(user.getId(), refreshToken);
            
            // Set cookies
            setAuthCookies(response, accessToken, refreshToken);
            
            return AuthResponse.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();
                    
        } catch (BadCredentialsException e) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }
    
    public void logout(HttpServletResponse response, String refreshToken) {
        if (refreshToken != null) {
            try {
                String username = jwtUtil.extractUsername(refreshToken);
                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                // Remove refresh token from Redis
                redisTemplate.delete("refresh_token:" + user.getId());
            } catch (Exception e) {
                // Log the error but don't throw exception for logout
                System.out.println("Error removing refresh token: " + e.getMessage());
            }
        }
        
        // Clear cookies
        clearAuthCookies(response);
    }
    
    public void refreshToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            throw new UnauthorizedException("No refresh token provided");
        }
        
        try {
            String username = jwtUtil.extractUsername(refreshToken);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
            // Check if refresh token exists in Redis
            String storedToken = redisTemplate.opsForValue().get("refresh_token:" + user.getId());
            if (!refreshToken.equals(storedToken)) {
                throw new UnauthorizedException("Invalid refresh token");
            }
            
            // Generate new access token
            String newAccessToken = jwtUtil.generateToken(username);
            
            // Set new access token cookie
            Cookie accessTokenCookie = new Cookie(accessTokenCookieName, newAccessToken);
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(secureCookie);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(15 * 60); // 15 minutes
            if (!"localhost".equals(cookieDomain)) {
                accessTokenCookie.setDomain(cookieDomain);
            }
            response.addCookie(accessTokenCookie);
            
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid refresh token");
        }
    }
    
    public AuthResponse getProfile(User currentUser) {
        return AuthResponse.builder()
                .id(currentUser.getId())
                .name(currentUser.getName())
                .email(currentUser.getEmail())
                .role(currentUser.getRole().name())
                .build();
    }
    
    private void storeRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                "refresh_token:" + userId, 
                refreshToken, 
                7, 
                TimeUnit.DAYS
        );
    }
    
    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        // Set access token cookie
        Cookie accessTokenCookie = new Cookie(accessTokenCookieName, accessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(secureCookie);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(15 * 60); // 15 minutes
        if (!"localhost".equals(cookieDomain)) {
            accessTokenCookie.setDomain(cookieDomain);
        }
        response.addCookie(accessTokenCookie);
        
        // Set refresh token cookie
        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(secureCookie);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        if (!"localhost".equals(cookieDomain)) {
            refreshTokenCookie.setDomain(cookieDomain);
        }
        response.addCookie(refreshTokenCookie);
    }
    
    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(accessTokenCookieName, "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(secureCookie);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        if (!"localhost".equals(cookieDomain)) {
            accessTokenCookie.setDomain(cookieDomain);
        }
        response.addCookie(accessTokenCookie);
        
        Cookie refreshTokenCookie = new Cookie(refreshTokenCookieName, "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(secureCookie);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        if (!"localhost".equals(cookieDomain)) {
            refreshTokenCookie.setDomain(cookieDomain);
        }
        response.addCookie(refreshTokenCookie);
    }
}