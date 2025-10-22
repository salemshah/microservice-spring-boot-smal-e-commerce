

package com.ecommerce.order.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Link to the product
    @Column(nullable = false)
    private Long productId;

    // Link back to the cart
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal subTotal;

    @Column(nullable = false)
    private BigDecimal price;

    // Helper method to recalc subtotal
    public void calculateSubTotal() {
        if (productId != null && quantity != null) {
            this.subTotal = price.multiply(BigDecimal.valueOf(quantity));
        }
    }
}
