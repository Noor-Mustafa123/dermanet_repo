package io.github.nomus.dermanet.controller;

import io.github.nomus.dermanet.dto.ProductRequest;
import io.github.nomus.dermanet.dto.ProductResponse;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, List<ProductResponse>>> getAllProducts(@AuthenticationPrincipal User currentUser) {
        List<ProductResponse> products = productService.getAllProducts();
        return ResponseEntity.ok(java.util.Map.of("products", products));
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        List<ProductResponse> products = productService.getFeaturedProducts();
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<java.util.Map<String, List<ProductResponse>>> getProductsByCategory(@PathVariable String category) {
        List<ProductResponse> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(java.util.Map.of("products", products));
    }
    
    @GetMapping("/recommendations")
    public ResponseEntity<List<ProductResponse>> getRecommendedProducts() {
        List<ProductResponse> products = productService.getRecommendedProducts();
        return ResponseEntity.ok(products);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(201).body(product);
    }
    
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> toggleFeaturedProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        ProductResponse product = productService.toggleFeaturedProduct(id);
        return ResponseEntity.ok(product);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().body("{\"message\": \"Product deleted successfully\"}");
    }
}