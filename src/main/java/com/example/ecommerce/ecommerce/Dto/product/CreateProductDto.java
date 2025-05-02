package com.example.ecommerce.ecommerce.Dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductDto {
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private MultipartFile image;

}
