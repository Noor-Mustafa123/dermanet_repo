package io.github.nomus.dermanet.service;

import io.github.nomus.dermanet.dto.CartItemRequest;
import io.github.nomus.dermanet.dto.CartItemResponse;
import io.github.nomus.dermanet.entity.CartItem;
import io.github.nomus.dermanet.entity.Product;
import io.github.nomus.dermanet.entity.User;
import io.github.nomus.dermanet.exception.ResourceNotFoundException;
import io.github.nomus.dermanet.repository.CartItemRepository;
import io.github.nomus.dermanet.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public List<CartItemResponse> getCartProducts(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        
        return cartItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<CartItemResponse> addToCart(User user, CartItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if item already exists in cart
        var existingItem = cartItemRepository.findByUserAndProductId(user, request.getProductId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + (request.getQuantity() != null ? request.getQuantity() : 1));
        } else {
            cartItem = CartItem.builder()
                    .user(user)
                    .product(product)
                    .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                    .build();
        }
        
        cartItemRepository.save(cartItem);
        
        return getCartProducts(user);
    }
    
    public List<CartItemResponse> removeAllFromCart(User user, Long productId) {
        if (productId == null) {
            // Remove all items from cart
            cartItemRepository.deleteByUser(user);
        } else {
            // Remove specific item from cart
            cartItemRepository.deleteByUserAndProductId(user, productId);
        }
        
        return getCartProducts(user);
    }
    
    public List<CartItemResponse> updateQuantity(User user, Long productId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findByUserAndProductId(user, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found in cart"));
        
        if (quantity == 0) {
            cartItemRepository.deleteByUserAndProductId(user, productId);
            return getCartProducts(user);
        }
        
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        
        return getCartProducts(user);
    }
    
    private CartItemResponse convertToResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        return CartItemResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(product.getImage())
                .category(product.getCategory())
                .quantity(cartItem.getQuantity())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}