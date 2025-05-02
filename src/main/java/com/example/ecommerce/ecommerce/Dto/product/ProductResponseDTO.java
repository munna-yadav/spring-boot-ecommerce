package com.example.ecommerce.ecommerce.Dto.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductResponseDTO {
    private long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String image;
}
