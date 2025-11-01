package com.ecommerce.order.client;

import com.ecommerce.common.libraray.product.dto.ProductResponseDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/api/products")
public interface ProductClient {
    @GetExchange("/{id}")
    ProductResponseDTO getProductById(@PathVariable Long id);
}
