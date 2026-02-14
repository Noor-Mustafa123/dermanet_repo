package io.github.nomus.dermanet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsData {
    private Long users;
    private Long products;
    private Long totalSales;
    private Double totalRevenue;
}