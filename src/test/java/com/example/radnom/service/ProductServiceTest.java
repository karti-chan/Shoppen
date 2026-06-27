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
                createProduct(1, "Laptop Gamingowy", 2500, "Electronics"),
                createProduct(2, "Myszka bezprzewodowa", 89, "Electronics"),
                createProduct(3, "Kawa ziarnista", 95, "Food")
        );
    }

    private Product createProduct(Integer id, String name, Integer price, String category) {
        Product p = new Product();
        p.setId(id);
        p.setProductName(name);
        p.setPrice(price);
        p.setCategory(category);
        p.setDescription("Opis produktu " + name);
        return p;
    }

    @Test
    @DisplayName("Powinien zwrócić wszystkie produkty")
    void shouldReturnAllProducts() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Product::getProductName)
                .containsExactly("Laptop Gamingowy", "Myszka bezprzewodowa", "Kawa ziarnista");
    }

    @Test
    @DisplayName("Powinien filtrować produkty po kategorii")
    void shouldFilterByCategory() {
        when(productRepository.findByCategory("Electronics"))
                .thenReturn(Arrays.asList(sampleProducts.get(0), sampleProducts.get(1)));

        List<Product> result = productService.getProductsByCategory("Electronics");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> "Electronics".equals(p.getCategory()));
    }

    @Test
    @DisplayName("Powinien wyszukiwać produkty po nazwie lub opisie")
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
    @DisplayName("Powinien zwrócić puste wyniki dla pustego zapytania wyszukiwania")
    void shouldReturnEmptyWhenQueryIsEmpty() {
        List<Product> result = productService.searchProducts("   ");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Powinien filtrować produkty w zakresie cenowym")
    void shouldFilterByPriceRange() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<Product> result = productService.getProductsByPriceRange(50, 2000);

        assertThat(result).hasSize(2); // Myszka i Kawa
        assertThat(result).extracting(Product::getPrice)
                .allMatch(price -> price >= 50 && price <= 2000);
    }

    @Test
    @DisplayName("Powinien sortować produkty po cenie rosnąco")
    void shouldSortByPriceAsc() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<Product> result = productService.getSortedProducts("price-asc");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPrice()).isLessThanOrEqualTo(result.get(1).getPrice());
    }

    @Test
    @DisplayName("Powinien zwrócić poprawne kategorie")
    void shouldReturnUniqueCategories() {
        when(productRepository.findAll()).thenReturn(sampleProducts);

        List<String> categories = productService.getAllCategories();

        assertThat(categories).containsExactlyInAnyOrder("Electronics", "Food");
    }

    @Test
    @DisplayName("Quick search powinien ograniczać liczbę wyników")
    void shouldLimitQuickSearchResults() {
        when(productRepository.searchByNameOrDescription("a"))
                .thenReturn(sampleProducts);

        List<QuickSearchResultDTO> result = productService.quickSearch("a", 2);

        assertThat(result).hasSize(2);
    }
}