package com.example.radnom.controller;

import com.example.radnom.entity.dto.CartDTO;
import com.example.radnom.service.CartService;
import com.example.radnom.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@WithMockUser(username = "kaszanek@example.com", roles = "USER")
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtService jwtService;

    private CartDTO mockCartDTO;

    @BeforeEach
    void setUp() {
        mockCartDTO = new CartDTO();
        mockCartDTO.setCartId(1L);
        mockCartDTO.setTotalItems(3);
        mockCartDTO.setTotalPrice(199.99);
        mockCartDTO.setFormattedTotalPrice("199.99 zł");
        mockCartDTO.setItems(List.of());
    }

    @Test
    @DisplayName("GET /api/cart - powinien zwrócić koszyk zalogowanego użytkownika")
    void shouldReturnUserCart() throws Exception {
        when(cartService.getCartDTO(anyString())).thenReturn(mockCartDTO);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPrice").value(199.99))
                .andExpect(jsonPath("$.formattedTotalPrice").value("199.99 zł"))
                .andExpect(jsonPath("$.items").isArray());

        verify(cartService, timeout(1000).times(1)).getCartDTO(anyString());
        verify(cartService).getCartDTO(eq("kaszanek@example.com"));
    }

    @Test
    @DisplayName("GET /api/cart - publiczny endpoint działa bez autoryzacji")
    void shouldReturn200WhenNotAuthenticated() throws Exception {
        when(cartService.getCartDTO(anyString())).thenReturn(mockCartDTO);

        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L));
    }
}