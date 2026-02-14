package io.github.nomus.dermanet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private AnalyticsData analyticsData;
    private List<DailySalesData> dailySalesData;
}