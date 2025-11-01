package com.ecommerce.order.controller;

import com.ecommerce.order.dto.AddToCartRequestDTO;
import com.ecommerce.order.dto.CartResponseDTO;
import com.ecommerce.order.entity.Cart;
import com.ecommerce.order.mapper.CartMapper;
import com.ecommerce.order.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;
    private final CartMapper cartMapper;

    @PostMapping("/add")
    public ResponseEntity<CartResponseDTO> addToCart(@Valid @RequestBody AddToCartRequestDTO request) {
        Cart cart = cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.status(HttpStatus.CREATED).body(cartMapper.toResponseDTO(cart));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDTO> getMyCart(@PathVariable Long userId) {
        Cart cart = cartService.getCartOf(userId);
        return ResponseEntity.ok(cartMapper.toResponseDTO(cart));
    }

    @DeleteMapping("/item/{itemId}/{userId}")
    public ResponseEntity<String> removeItem(@PathVariable Long itemId, @PathVariable Long userId) {
        cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok("Item removed successfully");
    }

    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<String> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok("Cart cleared successfully");
    }
}
