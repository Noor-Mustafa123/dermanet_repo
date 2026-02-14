package io.github.nomus.dermanet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCouponResponse {
    private String message;
    private String code;
    private Integer discountPercentage;
}