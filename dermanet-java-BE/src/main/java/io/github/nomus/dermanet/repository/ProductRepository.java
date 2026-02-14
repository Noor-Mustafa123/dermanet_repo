package io.github.nomus.dermanet.repository;

import io.github.nomus.dermanet.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByIsFeaturedTrue();
    List<Product> findByCategory(String category);
}