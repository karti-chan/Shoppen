package com.example.radnom.controller;

import com.example.radnom.controller.ProductController;
import com.example.radnom.entity.Product;
import com.example.radnom.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private List<Product> sampleProducts;

    @BeforeEach
    void setUp() {
        sampleProducts = List.of(
                createProduct(1, "Laptop", 2999, "Electronics"),
                createProduct(2, "Myszka", 129, "Electronics")
        );
    }

    private Product createProduct(int id, String name, int price, String category) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setPrice(price);
        p.setCategory(category);
        return p;
    }

    @Test
    @DisplayName("GET /api/products - powinien zwrócić listę produktów")
    void shouldReturnAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(sampleProducts);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Laptop"));
    }
}