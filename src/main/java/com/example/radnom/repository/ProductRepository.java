package com.example.radnom.repository;

import com.example.radnom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {


    List<Product> findByCategory(String category);

    List<Product> findByPriceBetween(Integer minPrice, Integer maxPrice);


    List<Product> findByProductNameContainingIgnoreCase(String name);


    List<Product> findByBrand(String brand);


    @Override
    Optional<Product> findById(Integer id);

    List<Product> findByOrderByPriceAsc();


    List<Product> findByOrderByPriceDesc();


    List<Product> findByStockGreaterThan(Integer stock);


    @Query("SELECT p FROM Product p WHERE LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByName(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameOrDescription(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByCategory(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.category) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Product> searchByNameOrDescriptionOrCategory(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY p.price ASC")
    List<Product> searchByNameOrderByPriceAsc(@Param("query") String query);

    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.productName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "p.price BETWEEN :minPrice AND :maxPrice")
    List<Product> searchByNameWithPriceRange(
            @Param("query") String query,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice);


    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL")
    List<String> findAllDistinctCategories();


    @Query("SELECT COUNT(p), AVG(p.price), MIN(p.price), MAX(p.price) FROM Product p")
    Object[] getProductStatistics();


}