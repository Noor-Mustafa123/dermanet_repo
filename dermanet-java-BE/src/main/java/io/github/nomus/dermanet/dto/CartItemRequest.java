package io.github.nomus.dermanet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {
    @JsonProperty("productId")
    private Long productId;
    
    @JsonProperty("id")
    private Long id;
    
    private Integer quantity;
    
    public Long getProductId() {
        return productId != null ? productId : id;
    }
}