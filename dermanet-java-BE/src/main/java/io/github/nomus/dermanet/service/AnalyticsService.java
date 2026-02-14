package io.github.nomus.dermanet.service;

import io.github.nomus.dermanet.dto.AnalyticsData;
import io.github.nomus.dermanet.dto.AnalyticsResponse;
import io.github.nomus.dermanet.dto.DailySalesData;
import io.github.nomus.dermanet.repository.OrderRepository;
import io.github.nomus.dermanet.repository.ProductRepository;
import io.github.nomus.dermanet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public AnalyticsResponse getAnalyticsData() {
        Long totalUsers = userRepository.count();
        Long totalProducts = productRepository.count();
        
        List<Object[]> salesData = orderRepository.getTotalSalesAndRevenue();
        Long totalSales = 0L;
        Double totalRevenue = 0.0;
        
        if (!salesData.isEmpty()) {
            Object[] data = salesData.get(0);
            totalSales = ((Number) data[0]).longValue();
            totalRevenue = ((Number) data[1]).doubleValue();
        }
        
        AnalyticsData analyticsData = AnalyticsData.builder()
                .users(totalUsers)
                .products(totalProducts)
                .totalSales(totalSales)
                .totalRevenue(totalRevenue)
                .build();
        
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        List<DailySalesData> dailySalesData = getDailySalesData(startDate, endDate);
        
        return AnalyticsResponse.builder()
                .analyticsData(analyticsData)
                .dailySalesData(dailySalesData)
                .build();
    }
    
    private List<DailySalesData> getDailySalesData(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> rawData = orderRepository.getDailySalesData(startDate, endDate);
        
        Map<String, DailySalesData> salesMap = rawData.stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> DailySalesData.builder()
                                .date(row[0].toString())
                                .sales(((Number) row[1]).longValue())
                                .revenue(((Number) row[2]).doubleValue())
                                .build()
                ));
        
        List<DailySalesData> result = new ArrayList<>();
        LocalDate current = startDate.toLocalDate();
        LocalDate end = endDate.toLocalDate();
        
        while (!current.isAfter(end)) {
            String dateStr = current.toString();
            result.add(salesMap.getOrDefault(dateStr, 
                    DailySalesData.builder()
                            .date(dateStr)
                            .sales(0L)
                            .revenue(0.0)
                            .build()));
            current = current.plusDays(1);
        }
        
        return result;
    }
}
