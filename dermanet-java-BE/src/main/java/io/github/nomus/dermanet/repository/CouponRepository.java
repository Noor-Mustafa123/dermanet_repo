package io.github.nomus.dermanet.repository;

import io.github.nomus.dermanet.entity.Coupon;
import io.github.nomus.dermanet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByUserIdAndIsActiveTrue(Long userId);
    Optional<Coupon> findByCodeAndUserIdAndIsActiveTrue(String code, Long userId);
    List<Coupon> findByUser(User user);
}