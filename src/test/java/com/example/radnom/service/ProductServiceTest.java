package com.example.radnom.service;

import com.example.radnom.entity.Product;
import com.example.radnom.entity.dto.QuickSearchResultDTO;
import com.example.radnom.repository.ProductRepository;
import com.example.radnom.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private List<Product> sampleProducts;

    @BeforeEach
    void setUp() {
        sampleProducts = Arrays.asList(
                createProduct(1, "Gaming Laptop", 2500, "Electronics"),
                createProduct(2, "Wireless Mouse", 89, "Electronics"),
                createProduct(3, "Coffee Beans", 45, "Food")
        );
    }

    private Product createProduct(Integer id, String name, Integer price, String category) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setPrice(price);
        p.setCategory(category);
        p.setDescription("Product description: " + name);
        return p;
    }

    @Test
    @DisplayName("Should return all products")
    void shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::getProductName)
                .containsExactly("Gaming Laptop", "Wireless Mouse", "Coffee Beans");
    }

    @Test
    @DisplayName("Should filter products by category")
    void shouldFilterByCategory() {
        when(productRepository.findByCategory("Electronics"))
                .thenReturn(Arrays.asList(sampleProducts.get(0), sampleProducts.get(1)));

        List<Product> result = productService.getProductsByCategory("Electronics");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> "Electronics".equals(p.getCategory()));
    }

    @Test
    @DisplayName("Should search products by name or description")
    void shouldSearchByName() {
        // given
        when(productRepository.searchByNameOrDescription("laptop"))
                .thenReturn(List.of(sampleProducts.get(0)));

        // when
        List<Product> result = productService.searchProducts("laptop");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).containsIgnoringCase("Laptop");
    }

    @Test
    @DisplayName("Should return empty list for empty search query")
    void shouldReturnEmptyWhenQueryIsEmpty() {
        List<Product> result = productService.searchProducts("   ");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should filter products in price range")
    void shouldFilterByPriceRange() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<Product> result = productService.getProductsByPriceRange(50, 2000);

        assertThat(result).hasSize(2); // Mouse and Coffee
        assertThat(result).extracting(Product::getPrice)
                .allMatch(price -> price >= 50 && price <= 2000);
    }

    @Test
    @DisplayName("Should sort products by price ascending")
    void shouldSortByPriceAsc() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<Product> result = productService.getSortedProducts("price-asc");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPrice()).isLessThanOrEqualTo(result.get(1).getPrice());
    }

    @Test
    @DisplayName("Should return unique categories")
    void shouldReturnUniqueCategories() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<String> categories = productService.getAllCategories();

        assertThat(categories).containsExactlyInAnyOrder("Electronics", "Food");
    }

    @Test
    @DisplayName("Quick search should limit the number of results")
    void shouldLimitQuickSearchResults() {
        when(productRepository.searchByNameOrDescription("a"))
                .thenReturn(sampleProducts);

        List<QuickSearchResultDTO> result = productService.quickSearch("a", 2);

        assertThat(result).hasSize(2);
    }
}