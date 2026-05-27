package com.example.radnom.controller;

import com.example.radnom.entity.dto.PriceFilterDTO;
import com.example.radnom.entity.Product;
import com.example.radnom.service.ProductService;
import com.example.radnom.entity.dto.QuickSearchResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.origin:http://localhost:5173}")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.info("GET /api/products");
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        log.info("GET /api/products/{}", id);

        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            log.warn("Product not found: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        log.info("GET /api/products/category/{}", category);
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/sorted/{sortType}")
    public ResponseEntity<List<Product>> getSortedProducts(@PathVariable String sortType) {
        log.info("GET /api/products/sorted/{}", sortType);
        return ResponseEntity.ok(productService.getSortedProducts(sortType));
    }


    @PostMapping("/filter/price")
    public ResponseEntity<List<Product>> getProductsByPriceRange(@RequestBody PriceFilterDTO filterDTO) {
        log.info("POST /api/products/filter/price - min: {}, max: {}",
                filterDTO.getMinPrice(), filterDTO.getMaxPrice());

        List<Product> products = productService.getProductsByPriceRange(
                filterDTO.getMinPrice(), filterDTO.getMaxPrice());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/price-old")
    public ResponseEntity<List<Product>> getProductsByPriceRangeOld(
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice) {

        log.info("GET /api/products/filter/price-old?min={}&max={}", minPrice, maxPrice);

        List<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam(required = false) String q) {
        log.info("GET /api/products/search?q={}", q);
        return ResponseEntity.ok(productService.searchProducts(q));
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<List<Product>> advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "name-asc") String sort) {

        log.info("GET /api/products/search/advanced");

        List<Product> products = productService.advancedSearch(query, minPrice, maxPrice, category, sort);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/search")
    public ResponseEntity<List<Product>> searchProductsPost(@RequestBody SearchRequest request) {
        log.info("POST /api/products/search - query: '{}'", request.getQuery());
        return ResponseEntity.ok(productService.searchProducts(request.getQuery()));
    }

    @GetMapping("/search/name")
    public ResponseEntity<List<Product>> searchByName(@RequestParam String name) {
        log.info("GET /api/products/search/name?name={}", name);
        return ResponseEntity.ok(productService.searchByName(name));
    }

    @GetMapping("/search/quick")
    public ResponseEntity<List<QuickSearchResultDTO>> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int limit) {

        log.info("GET /api/products/search/quick?q={}&limit={}", q, limit);

        List<QuickSearchResultDTO> results = productService.quickSearch(q, limit);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("GET /api/products/categories");
        return ResponseEntity.ok(productService.getAllCategories());
    }


    @GetMapping("/popular")
    public ResponseEntity<List<Product>> getPopularProducts(@RequestParam(defaultValue = "8") int limit) {
        log.info("GET /api/products/popular?limit={}", limit);
        return ResponseEntity.ok(productService.getPopularProducts(limit));
    }

    @GetMapping("/new")
    public ResponseEntity<List<Product>> getNewProducts(@RequestParam(defaultValue = "8") int limit) {
        log.info("GET /api/products/new?limit={}", limit);
        return ResponseEntity.ok(productService.getNewProducts(limit));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProductStats() {
        log.info("GET /api/products/stats");
        return ResponseEntity.ok(productService.getProductStatistics());
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class SearchRequest {
        private String query;
    }
}