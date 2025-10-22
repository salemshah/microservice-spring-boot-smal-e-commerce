package com.ecommerce.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddToCartRequestDTO {
    @NotNull(message = "User Id is required")
    private Long userId;

    @NotNull(message = "Product Price is required")
    private BigDecimal price;

    @NotNull(message = "Product Id is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;
}
