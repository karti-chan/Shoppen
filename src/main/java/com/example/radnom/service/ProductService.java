package com.example.radnom.service;

import com.example.radnom.entity.Product;
import com.example.radnom.entity.dto.QuickSearchResultDTO;
import com.example.radnom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;


    public List<Product> getAllProducts() {
        log.info("Getting all products");
        return productRepository.findAll();
    }

    public Product getProductById(Integer id) {
        log.info("Getting product by id: {}", id);
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    public List<Product> getProductsByCategory(String category) {
        log.info("Getting products by category: {}", category);
        return productRepository.findByCategory(category);
    }


    public List<Product> getSortedProducts(String sortType) {
        log.info("Getting sorted products by: {}", sortType);
        List<Product> products = productRepository.findAll();

        switch (sortType.toLowerCase()) {
            case "price-asc":
                products.sort(Comparator.comparing(Product::getPrice));
                break;
            case "price-desc":
                products.sort(Comparator.comparing(Product::getPrice).reversed());
                break;
            case "name-asc":
                products.sort(Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER));
                break;
            case "name-desc":
                products.sort(Comparator.comparing(Product::getProductName, String.CASE_INSENSITIVE_ORDER).reversed());
                break;
            default:
                log.warn("Unknown sort type: {}, using default", sortType);
        }
        return products;
    }


    public List<Product> getProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        log.info("Filtering products by price range: {} - {}", minPrice, maxPrice);

        return productRepository.findAll().stream()
                .filter(p -> {
                    boolean passMin = (minPrice == null) || (p.getPrice() >= minPrice);
                    boolean passMax = (maxPrice == null) || (p.getPrice() <= maxPrice);
                    return passMin && passMax;
                })
                .toList();
    }


    public List<Product> searchProducts(String query) {
        log.info("Searching products with query: '{}'", query);

        if (query == null || query.trim().isEmpty()) {
            return getAllProducts();
        }

        String searchQuery = query.trim().toLowerCase();
        return productRepository.searchByNameOrDescription(searchQuery);
    }

    public List<Product> searchByCategory(String category) {
        log.info("Searching products in category: '{}'", category);
        if (category == null || category.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByCategory(category.toLowerCase().trim());
    }

    public List<Product> searchByName(String name) {
        log.info("Searching products by name: '{}'", name);
        if (name == null || name.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchByName(name.trim().toLowerCase());
    }


    public List<Product> advancedSearch(String query, Integer minPrice, Integer maxPrice,
                                        String category, String sort) {
        log.info("Advanced search with query={}, minPrice={}, maxPrice={}, category={}, sort={}",
                query, minPrice, maxPrice, category, sort);

        List<Product> products;

        if (query != null && !query.trim().isEmpty()) {
            products = searchProducts(query);
        } else {
            products = getAllProducts();
        }

        if (category != null && !category.trim().isEmpty()) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null &&
                            p.getCategory().equalsIgnoreCase(category.trim()))
                    .toList();
        }

        if (minPrice != null || maxPrice != null) {
            products = products.stream()
                    .filter(p -> {
                        boolean passMin = (minPrice == null) || (p.getPrice() >= minPrice);
                        boolean passMax = (maxPrice == null) || (p.getPrice() <= maxPrice);
                        return passMin && passMax;
                    })
                    .toList();
        }

        if (sort != null && !sort.isEmpty()) {
            products = getSortedProducts(sort);
        }

        log.info("Advanced search found {} products", products.size());
        return products;
    }


    public List<QuickSearchResultDTO> quickSearch(String query, int limit) {
        log.info("Quick search for: {}, limit: {}", query, limit);

        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        return searchProducts(query).stream()
                .limit(limit)
                .map(p -> QuickSearchResultDTO.builder()
                        .id(p.getProductId())
                        .name(p.getProductName())
                        .price(p.getPrice())
                        .imageUrl(p.getImageUrl() != null ? p.getImageUrl() : "")
                        .category(p.getCategory() != null ? p.getCategory() : "")
                        .build())
                .toList();
    }


    public List<String> getAllCategories() {
        log.info("Getting all unique categories");

        return productRepository.findAll().stream()
                .map(Product::getCategory)
                .distinct()
                .filter(category -> category != null && !category.trim().isEmpty())
                .sorted()
                .toList();
    }


    public List<Product> getPopularProducts(int limit) {
        log.info("Getting {} popular products", limit);
        return productRepository.findAll().stream()
                .limit(limit)
                .toList();
    }

    public List<Product> getNewProducts(int limit) {
        log.info("Getting {} new products", limit);
        return productRepository.findAll().stream()
                .limit(limit)
                .toList();
    }


    public Map<String, Object> getProductStatistics() {
        log.info("Getting product statistics");

        List<Product> allProducts = getAllProducts();
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalProducts", allProducts.size());
        stats.put("totalCategories", getAllCategories().size());

        if (!allProducts.isEmpty()) {
            Map<String, Integer> priceRange = new HashMap<>();
            priceRange.put("min", allProducts.stream()
                    .mapToInt(Product::getPrice)
                    .min().orElse(0));
            priceRange.put("max", allProducts.stream()
                    .mapToInt(Product::getPrice)
                    .max().orElse(0));
            stats.put("priceRange", priceRange);

            stats.put("averagePrice", allProducts.stream()
                    .mapToInt(Product::getPrice)
                    .average().orElse(0));
        } else {
            Map<String, Integer> priceRange = new HashMap<>();
            priceRange.put("min", 0);
            priceRange.put("max", 0);
            stats.put("priceRange", priceRange);
            stats.put("averagePrice", 0);
        }

        return stats;
    }
}