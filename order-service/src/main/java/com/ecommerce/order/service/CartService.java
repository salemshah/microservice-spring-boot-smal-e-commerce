package com.ecommerce.order.service;

//import com.ecommerce.order.dto.ProductResponseDTO;
import com.ecommerce.commonconfig.product.dto.ProductResponseDTO;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final WebClient productWebClient;

    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantity, BigDecimal price) {

        ProductResponseDTO product = getProductById(productId).block();

        if(product == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setUserId(userId);
                    return cartRepository.save(c);
                });

        // find existing item
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setProductName(existingItem.getProductName());
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.calculateSubTotal();
        } else {

            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productId(productId)
                    .productName(product.getName())
                    .quantity(quantity)
                    .price(price)
                    .build();
            newItem.calculateSubTotal();
            cart.getItems().add(newItem);
        }

        cart.calculateTotalPrice();
        return cartRepository.save(cart);
    }

    public Cart getCartOf(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
    }

    @Transactional
    public void removeItemFromCart(Long userId, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart item not found"));

        // ownership check
        if (!item.getCart().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't modify another user's cart");
        }

        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        cart.calculateTotalPrice();
        cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartOf(userId);
        cart.getItems().clear();
        cart.setTotalPrice(java.math.BigDecimal.ZERO);
        cartRepository.save(cart);
    }

    private Mono<ProductResponseDTO> getProductById(Long productId) {
        return productWebClient
                .get()
                .uri("/products/{productId}", productId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
                )
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable"))
                )
                .bodyToMono(ProductResponseDTO.class);
    }
}
