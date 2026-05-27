package com.example.radnom.controller;

import com.example.radnom.entity.dto.CartDTO;
import com.example.radnom.entity.dto.CartItemDTO;
import com.example.radnom.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDTO> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        CartDTO cart = cartService.getCartDTO(email);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> getCartItemCount(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.getCartItemCount(email));
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<CartItemDTO> items = cartService.getCartItemsDTO(email);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/add")
    public ResponseEntity<CartDTO> addToCart(@AuthenticationPrincipal UserDetails userDetails,
                                             @RequestParam Integer productId,
                                             @RequestParam(defaultValue = "1") Integer quantity) {
        String email = userDetails.getUsername();
        CartDTO cart = cartService.addToCart(email, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/remove")
    public ResponseEntity<CartDTO> removeFromCart(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam Integer productId) {
        String email = userDetails.getUsername();
        CartDTO cart = cartService.removeFromCart(email, productId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/update")
    public ResponseEntity<CartDTO> updateQuantity(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestParam Integer productId,
                                                  @RequestParam Integer quantity) {
        String email = userDetails.getUsername();
        CartDTO cart = cartService.updateQuantity(email, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        cartService.clearCart(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Double> getCartTotal(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.calculateCartTotal(email));
    }

    @GetMapping("/contains/{productId}")
    public ResponseEntity<Boolean> isProductInCart(@AuthenticationPrincipal UserDetails userDetails,
                                                   @PathVariable Integer productId) {
        String email = userDetails.getUsername();
        return ResponseEntity.ok(cartService.isProductInCart(email, productId));
    }
}