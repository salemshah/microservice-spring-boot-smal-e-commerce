package com.ecommerce.product.service;

import com.ecommerce.product.entity.Product;
import com.ecommerce.product.exception.DatabaseOperationException;
import com.ecommerce.product.exception.EntityNotFoundException;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Page<Product> filterProducts(
            String name, String category, BigDecimal minPrice, BigDecimal maxPrice, String keyword, Pageable pageable) {
        try {
            return productRepository.findAll(
                    ProductSpecification.filter(name, category, minPrice, maxPrice, keyword),
                    pageable
            );
        } catch (DataAccessException ex) {
            log.error("Error filtering products", ex);
            throw new DatabaseOperationException("Failed to fetch products", ex);
        }
    }

    public Product fetchProductById(Long productId) {
        return productRepository.findById(productId).
                orElseThrow(() -> new EntityNotFoundException("Product", productId));
    }

    public Product createProduct(Product product) {
        try {
            return productRepository.save(product);
        } catch (DataAccessException exception) {
            log.error("Error saving product", exception);
            throw new DatabaseOperationException("Failed to create product", exception);
        }
    }

    public Product updateProduct(Product updatedProduct, Long productId) {
        Product existingProduct = fetchProductById(productId);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());

        try {
            return productRepository.save(existingProduct);
        } catch (DataAccessException exception) {
            log.error("Error updating product {}", productId, exception);
            throw new DatabaseOperationException("Failed to create product", exception);
        }
    }

    public String deleteProductById(Long productId) {
        fetchProductById(productId);
        try {
            productRepository.deleteById(productId);
            return "Product deleted successfully (ID: " + productId + ")";
        } catch (DataAccessException exception) {
            log.error("Error deleting product {}", productId, exception);
            throw new DatabaseOperationException("Failed to delete product", exception);
        }
    }

}
