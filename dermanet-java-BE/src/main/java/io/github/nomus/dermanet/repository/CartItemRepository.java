package io.github.nomus.dermanet.repository;

import io.github.nomus.dermanet.entity.CartItem;
import io.github.nomus.dermanet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    Optional<CartItem> findByUserAndProductId(User user, Long productId);
    
    @Transactional
    void deleteByUser(User user);
    
    @Transactional
    void deleteByUserAndProductId(User user, Long productId);
}