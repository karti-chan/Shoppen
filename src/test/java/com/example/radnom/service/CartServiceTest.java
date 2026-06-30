package com.example.radnom.service;

import com.example.radnom.entity.*;
import com.example.radnom.entity.dto.CartDTO;
import com.example.radnom.entity.dto.CartItemDTO;
import com.example.radnom.repository.*;
import com.example.radnom.service.CartService;
import com.example.radnom.service.mapper.CartMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;
    private CartDTO testCartDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");

        testCart = new Cart();
        testCart.setId(10L);
        testCart.setUser(testUser);
        testCart.setItems(new ArrayList<>());

        testProduct = new Product();
        testProduct.setId(100);
        testProduct.setProductName("Test Product");
        testProduct.setPrice(199);

        testCartItem = new CartItem();
        testCartItem.setId(55);
        testCartItem.setProduct(testProduct);
        testCartItem.setCart(testCart);
        testCartItem.setQuantity(1);

        testCartDTO = CartDTO.builder()
                .cartId(10L)
                .totalItems(0)
                .totalPrice(0.0)
                .items(new ArrayList<>())
                .build();
    }


    @Test
    @DisplayName("Should return cart item count")
    void shouldReturnCartItemCount() {
        testCart.getItems().add(testCartItem);
        when(cartRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testCart));

        int count = cartService.getCartItemCount("test@example.com");
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should add new product to cart and return DTO")
    void shouldAddNewProductToCart() {

        when(cartRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testCart));
        when(productRepository.findById(100)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(i -> i.getArgument(0));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        when(cartMapper.toDTO(any(Cart.class))).thenReturn(testCartDTO); // ✅ MOCK MAPOWANIA


        CartDTO result = cartService.addToCart("test@example.com", 100, 2);

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(10L);
        verify(cartMapper).toDTO(any(Cart.class));
    }

    @Test
    @DisplayName("Should remove product from cart and return DTO")
    void shouldRemoveProductFromCart() {

        testCart.getItems().add(testCartItem);

        when(cartRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(10L, 100)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(any(CartItem.class));
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));
        when(cartMapper.toDTO(any(Cart.class))).thenReturn(testCartDTO);


        CartDTO result = cartService.removeFromCart("test@example.com", 100);


        assertThat(result).isNotNull();
        verify(cartItemRepository).delete(testCartItem);
        verify(cartMapper).toDTO(any(Cart.class));
    }

    @Test
    @DisplayName("Should calculate correct cart total")
    void shouldCalculateCartTotal() {
        CartItem item1 = new CartItem();
        item1.setPrice(100);
        item1.setQuantity(2);

        testCart.getItems().add(item1);

        when(cartRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testCart));

        double total = cartService.calculateCartTotal("test@example.com");

        assertThat(total).isEqualTo(200.0);
    }

    @Test
    @DisplayName("Should return cart DTO")
    void shouldReturnCartDTO() {
        when(cartRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testCart));
        when(cartMapper.toDTO(testCart)).thenReturn(testCartDTO);

        CartDTO result = cartService.getCartDTO("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getCartId()).isEqualTo(10L);
        verify(cartMapper).toDTO(testCart);
    }

    @Test
    @DisplayName("Should return list of DTO items in cart")
    void shouldReturnCartItemsDTO() {
        testCart.getItems().add(testCartItem);

        CartItemDTO itemDTO = CartItemDTO.builder()
                .id(55)
                .productId(100)
                .productName("Test Product")
                .quantity(1)
                .price(199)
                .build();

        when(cartRepository.findByUserEmail("test@example.com")).thenReturn(Optional.of(testCart));
        when(cartMapper.toDTO(testCartItem)).thenReturn(itemDTO);

        var result = cartService.getCartItemsDTO("test@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductId()).isEqualTo(100);
        verify(cartMapper).toDTO(testCartItem);
    }
}