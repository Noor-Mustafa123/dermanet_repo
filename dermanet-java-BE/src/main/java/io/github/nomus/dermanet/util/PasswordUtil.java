package io.github.nomus.dermanet.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generate hash for admin password
        String adminPassword = "admin123";
        String hashedPassword = encoder.encode(adminPassword);
        
        System.out.println("Password: " + adminPassword);
        System.out.println("Hashed: " + hashedPassword);
        
        // You can use this hash in your SQL insert statements
    }
}
