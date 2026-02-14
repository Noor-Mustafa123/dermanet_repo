package io.github.nomus.dermanet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private List<CheckoutProduct> products;
    private String couponCode;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutProduct {
        private Long id;
        private String name;
        private BigDecimal price;
        private String image;
        private Integer quantity;
    }
}