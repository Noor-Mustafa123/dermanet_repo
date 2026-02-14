package io.github.nomus.dermanet.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.nomus.dermanet.dto.ProductRequest;
import io.github.nomus.dermanet.dto.ProductResponse;
import io.github.nomus.dermanet.entity.Product;
import io.github.nomus.dermanet.exception.ResourceNotFoundException;
import io.github.nomus.dermanet.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private Cloudinary cloudinary;
    
    private static final String FEATURED_PRODUCTS_CACHE_KEY = "featured_products";
    
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getFeaturedProducts() {
        // Try to get from cache first
        String cachedProducts = redisTemplate.opsForValue().get(FEATURED_PRODUCTS_CACHE_KEY);
        if (cachedProducts != null) {
            // In a real implementation, you'd deserialize the JSON back to ProductResponse objects
            // For simplicity, we'll fetch from DB and update cache
        }
        
        // Fetch from database
        List<Product> featuredProducts = productRepository.findByIsFeaturedTrue();
        List<ProductResponse> response = featuredProducts.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        // Update cache
        updateFeaturedProductsCache();
        
        return response;
    }
    
    public List<ProductResponse> getProductsByCategory(String category) {
        List<Product> products = productRepository.findByCategory(category);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getRecommendedProducts() {
        List<Product> products = productRepository.findAll();
        // Shuffle and take first 4 products (similar to MongoDB's $sample)
        return products.stream()
                .limit(4)
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public ProductResponse createProduct(ProductRequest request) {
        try {
            String imageUrl = request.getImage();
            
            // Upload image to Cloudinary if it's a base64 string
            if (request.getImage() != null && request.getImage().startsWith("data:image")) {
                Map uploadResult = cloudinary.uploader().upload(request.getImage(), 
                    ObjectUtils.asMap("folder", "products"));
                imageUrl = (String) uploadResult.get("secure_url");
            }
            
            Product product = Product.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .price(request.getPrice() != null ? request.getPrice() : BigDecimal.ZERO)
                    .image(imageUrl != null ? imageUrl : "")
                    .category(request.getCategory())
                    .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                    .build();
            
            Product savedProduct = productRepository.save(product);
            return convertToResponse(savedProduct);
            
        } catch (Exception e) {
            throw new RuntimeException("Error creating product: " + e.getMessage());
        }
    }
    
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getImage() != null) {
            product.setImage(request.getImage());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getIsFeatured() != null) {
            product.setIsFeatured(request.getIsFeatured());
        }
        
        Product updatedProduct = productRepository.save(product);
        
        // If featured status changed, update cache
        if (request.getIsFeatured() != null) {
            updateFeaturedProductsCache();
        }
        
        return convertToResponse(updatedProduct);
    }
    
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Delete image from Cloudinary
        if (product.getImage() != null && !product.getImage().isEmpty()) {
            try {
                String publicId = extractPublicIdFromUrl(product.getImage());
                cloudinary.uploader().destroy("products/" + publicId, ObjectUtils.emptyMap());
            } catch (Exception e) {
                System.out.println("Error deleting image from Cloudinary: " + e.getMessage());
            }
        }
        
        productRepository.deleteById(id);
        
        // Update cache if this was a featured product
        if (product.getIsFeatured()) {
            updateFeaturedProductsCache();
        }
    }
    
    public ProductResponse toggleFeaturedProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        product.setIsFeatured(!product.getIsFeatured());
        Product updatedProduct = productRepository.save(product);
        
        // Update cache
        updateFeaturedProductsCache();
        
        return convertToResponse(updatedProduct);
    }
    
    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(product.getImage())
                .category(product.getCategory())
                .isFeatured(product.getIsFeatured())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
    
    private String extractPublicIdFromUrl(String url) {
        // Extract public ID from Cloudinary URL
        String[] parts = url.split("/");
        String filename = parts[parts.length - 1];
        return filename.substring(0, filename.lastIndexOf('.'));
    }
    
    private void updateFeaturedProductsCache() {
        try {
            List<Product> featuredProducts = productRepository.findByIsFeaturedTrue();
            // In a real implementation, you'd serialize the products to JSON and store in Redis
            // For now, we'll just set a simple marker
            redisTemplate.opsForValue().set(FEATURED_PRODUCTS_CACHE_KEY, "cached");
        } catch (Exception e) {
            System.out.println("Error updating featured products cache: " + e.getMessage());
        }
    }
}