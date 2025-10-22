package com.ecommerce.order.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartResponseDTO {
    private Long id;
    private Long userId;
    private BigDecimal totalPrice;
    private List<CartItemResponseDTO> items;
}
