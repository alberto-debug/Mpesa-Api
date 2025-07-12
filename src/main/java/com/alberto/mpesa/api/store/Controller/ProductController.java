package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.DTO.ProductRequestDTO;
import com.alberto.mpesa.api.store.DTO.ProductResponseDTO;
import com.alberto.mpesa.api.store.Services.ProductService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/create")
    public ResponseEntity<ProductResponseDTO> createProduct(@RequestBody ProductRequestDTO productRequestDTO){
        ProductResponseDTO created = productService.createProduct(productRequestDTO);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts(){
        return ResponseEntity.ok(productService.listProducts());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            Long id,
            @RequestBody ProductRequestDTO dto){

        ProductResponseDTO updated = productService.updateProduct(id, dto);

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(Long id, @RequestBody ProductRequestDTO dto){

        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }



}
