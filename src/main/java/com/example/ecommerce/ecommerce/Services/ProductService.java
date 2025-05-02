package com.example.ecommerce.ecommerce.Services;

import com.cloudinary.Cloudinary;
import com.example.ecommerce.ecommerce.Dto.product.CreateProductDto;
import com.example.ecommerce.ecommerce.Dto.product.ProductResponseDTO;
import com.example.ecommerce.ecommerce.Entity.Product;
import com.example.ecommerce.ecommerce.Repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired Cloudinary cloudinary;

    @Autowired UserService userService;

    @Autowired ProductRepository productRepository;

    @Autowired ModelMapper modelMapper;

    public ResponseEntity<?> add(CreateProductDto dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setStockQuantity(dto.getStock());
        product.setPrice(dto.getPrice());
        MultipartFile image = dto.getImage();

        if (image != null && !image.isEmpty()) {
            try{
                Map<?, ?> result = cloudinary.uploader().upload(image.getBytes(), Map.of());
                product.setImage(result.get("secure_url").toString());
            }catch (IOException e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Error uploading image"));
            }

        }

        Product savedProduct = productRepository.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @Transactional
    public ResponseEntity<?> fetchProduct(){
        List<Product> products = productRepository.findAll();
        List<ProductResponseDTO> responseDTOS = new ArrayList<>();

        for (Product item:products){
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getStockQuantity());
            dto.setImage(item.getImage());
            responseDTOS.add(dto);
        }
        return ResponseEntity.ok(responseDTOS);
    }

    @Transactional
    public ResponseEntity<?> searchProduct(String keyword){
        List<Product> products = productRepository.searchProducts(keyword);
        if (products.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","No products found"));
        }
        List<ProductResponseDTO> responseDTOS = new ArrayList<>();

        for (Product item:products){
            ProductResponseDTO dto = new ProductResponseDTO();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setPrice(item.getPrice());
            dto.setQuantity(item.getStockQuantity());
            dto.setImage(item.getImage());
            responseDTOS.add(dto);
        }
        return ResponseEntity.ok(responseDTOS);
    }

    @Transactional
    public ResponseEntity<?>getProduct(Integer id){
        if (id == null ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message","please pass id"));
        }
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","product not found"));
        }
        Product product = optionalProduct.get();
        return ResponseEntity.ok(modelMapper.map(product,ProductResponseDTO.class));
    }

    @Transactional
    public ResponseEntity<?> deleteById(Integer id){
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","product with id not found"));
        }
        Product product = optionalProduct.get();
        productRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message","product delted!"));
    }

    @Transactional
    public ResponseEntity<?> updateProduct(CreateProductDto dto, Integer productId){

       Optional<Product> optionalProduct = productRepository.findById(productId);

       if (optionalProduct.isEmpty()){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Product with id not found"));
       }

       // get the product
        Product product = optionalProduct.get();

       if (!(dto.getStock() == null)){
           product.setStockQuantity(dto.getStock());
       }
       if (dto.getPrice() != null){
           product.setPrice(dto.getPrice());
       }
       if (dto.getName() != null){
           product.setName(dto.getName());
       }
       if (dto.getDescription() != null){
           product.setDescription(dto.getDescription());
       }

       if (dto.getImage()!= null && !dto.getImage().isEmpty()){
           try{
               Map<?,?> result = cloudinary.uploader().upload(dto.getImage().getBytes(),Map.of());
               product.setImage(result.get("secure_url").toString());
           } catch (IOException e) {
                   return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message","Error while uploading image"));
           }
       }
       Product updatedProduct = productRepository.save(product);
        System.out.println(updatedProduct.getStockQuantity());

       return ResponseEntity.status(HttpStatus.CREATED).body(modelMapper.map(updatedProduct,ProductResponseDTO.class));
    }
}
