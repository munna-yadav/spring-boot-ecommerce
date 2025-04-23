package com.example.ecommerce.ecommerce.Repository;

import com.example.ecommerce.ecommerce.Entity.CartItem;
import com.example.ecommerce.ecommerce.Entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    Optional<Product> getProductById(Long id);
    Optional<CartItem> getItemByProductId(Long id);
}
