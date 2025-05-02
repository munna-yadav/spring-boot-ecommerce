package com.example.ecommerce.ecommerce.Repository;

import com.example.ecommerce.ecommerce.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository< OrderItem, Integer > {

    void deleteByOrderId(Integer order);
}
