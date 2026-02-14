package io.github.nomus.dermanet.repository;

import io.github.nomus.dermanet.entity.Order;
import io.github.nomus.dermanet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    
    @Query("SELECT COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    List<Object[]> getTotalSalesAndRevenue();
    
    @Query("SELECT CAST(o.createdAt AS date), COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(o.createdAt AS date) ORDER BY CAST(o.createdAt AS date)")
    List<Object[]> getDailySalesData(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
}