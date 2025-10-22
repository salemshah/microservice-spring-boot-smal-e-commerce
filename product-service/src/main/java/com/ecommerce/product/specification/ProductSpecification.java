package com.ecommerce.product.specification;

import com.ecommerce.product.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> filter(
            String name,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String keyword) {

        Specification<Product> spec = Specification.unrestricted();

        if (name != null && !name.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
        }

        if (category != null && !category.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase()));
        }

        if (minPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }

        if (maxPrice != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String keywordPattern = "%" + keyword.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("name")), keywordPattern),
                            cb.like(cb.lower(root.get("description")), keywordPattern),
                            cb.like(cb.lower(root.get("category")), keywordPattern)
                    ));
        }

        return spec;
    }
}
