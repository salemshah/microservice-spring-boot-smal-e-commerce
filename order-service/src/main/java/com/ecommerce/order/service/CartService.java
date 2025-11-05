package com.ecommerce.order.service;

import com.ecommerce.common.libraray.product.dto.ProductResponseDTO;
import com.ecommerce.order.client.ProductClient;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;


    @Transactional
    public Cart addToCart(Long userId, Long productId, int quantity) {

        // Retrieve product safely
        ProductResponseDTO product = fetchProductOrThrow(productId);

        // Retrieve or create cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        // Find or create item (thread-safe with synchronized block on cart)
        synchronized (cart) {
            CartItem item = cart.getItems().stream()
                    .filter(i -> i.getProductId().equals(productId))
                    .findFirst()
                    .orElseGet(() -> createNewItem(cart, product));

            // Always refresh product info to reflect current data
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(item.getQuantity() + quantity);
            item.calculateSubTotal();

            cart.calculateTotalPrice();
        }

        // Persist changes (cascade should handle items)
        return cartRepository.saveAndFlush(cart);
    }

    private ProductResponseDTO fetchProductOrThrow(Long productId) {
        try {
            return productClient.getProductById(productId);
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found for ID: " + productId);
        } catch (WebClientResponseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to contact product service: " + ex.getStatusText());
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error while fetching product", ex);
        }
    }

    private Cart createNewCart(Long userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        return cartRepository.save(cart);
    }

    private CartItem createNewItem(Cart cart, ProductResponseDTO product) {
        CartItem item = CartItem.builder()
                .cart(cart)
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(0)
                .build();
        cart.getItems().add(item);
        return item;
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

//    private Mono<ProductResponseDTO> getProductById(Long productId) {
//        return productWebClient
//                .get()
//                .uri("/products/{productId}", productId)
//                .retrieve()
//                .onStatus(HttpStatusCode::is4xxClientError, response ->
//                        Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"))
//                )
//                .onStatus(HttpStatusCode::is5xxServerError, response ->
//                        Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Product service unavailable"))
//                )
//                .bodyToMono(ProductResponseDTO.class);
//    }
}
