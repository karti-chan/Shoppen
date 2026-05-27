package com.example.radnom.service;

import com.example.radnom.entity.*;
import com.example.radnom.entity.dto.CartDTO;
import com.example.radnom.entity.dto.CartItemDTO;
import com.example.radnom.repository.*;
import com.example.radnom.service.mapper.CartMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;


    public CartDTO getCartDTO(String email) {
        Cart cart = getOrCreateCart(email);
        return cartMapper.toDTO(cart);
    }

    public List<CartItemDTO> getCartItemsDTO(String email) {
        Cart cart = getOrCreateCart(email);
        return cart.getItems().stream()
                .map(cartMapper::toDTO)
                .toList();
    }

    public CartDTO addToCart(String email, Integer productId, Integer quantity) {
        log.debug("Adding product {} (quantity: {}) to cart for user: {}",
                productId, quantity, email);

        validateAddToCartRequest(email, productId, quantity);
        Cart cart = getOrCreateCart(email);
        Product product = getProductById(productId);

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            updateExistingItem(existingItem.get(), quantity);
            cartItemRepository.save(existingItem.get());
        } else {
            CartItem newItem = createCartItem(cart, product, quantity);
            cartItemRepository.save(newItem);
            cart.addItem(newItem);
        }

        log.info("Successfully added product {} (quantity: {}) to cart for user {}",
                productId, quantity, email);

        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);
    }

    public CartDTO removeFromCart(String email, Integer productId) {
        log.debug("Removing product {} from cart for user: {}", productId, email);

        validateRemoveFromCartRequest(email, productId);
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

        cartItemRepository.delete(item);
        cart.removeItem(item);

        log.info("Successfully removed product {} from cart for user {}", productId, email);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);
    }

    public CartDTO updateQuantity(String email, Integer productId, Integer quantity) {
        log.debug("Updating product {} quantity to {} for user: {}",
                productId, quantity, email);

        validateUpdateQuantityRequest(email, productId, quantity);
        Cart cart = getOrCreateCart(email);

        if (quantity == 0) {
            cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                    .ifPresent(item -> {
                        cartItemRepository.delete(item);
                        cart.getItems().removeIf(ci -> ci.getId().equals(item.getId()));
                    });
        } else {
            CartItem item = cartItemRepository
                    .findByCartIdAndProductId(cart.getId(), productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found in cart"));

            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        log.info("Updated quantity of product {} to {} for user {}", productId, quantity, email);
        Cart savedCart = cartRepository.save(cart);
        return cartMapper.toDTO(savedCart);
    }

    public int getCartItemCount(String email) {
        log.debug("Calculating cart item count for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);
        return cart.getTotalItemsCount();
    }

    public Cart getCart(String email) {
        log.debug("Getting cart for user: {}", email);
        validateUserEmail(email);
        return getOrCreateCart(email);
    }

    public List<CartItem> getCartItems(String email) {
        log.debug("Getting cart items for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);
        return cart.getItems();
    }

    public void clearCart(String email) {
        log.debug("Clearing cart for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);
        cartItemRepository.deleteByCartId(cart.getId());
        cart.clear();

        log.info("Cleared cart for user {}", email);
    }

    public double calculateCartTotal(String email) {
        log.debug("Calculating cart total for user: {}", email);
        validateUserEmail(email);

        Cart cart = getOrCreateCart(email);
        return cart.getTotalPrice();
    }

    public boolean isProductInCart(String email, Integer productId) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            return false;
        }

        Cart cart = getOrCreateCart(email);
        return cartItemRepository.existsByCartIdAndProductId(cart.getId(), productId);
    }

    private Cart getOrCreateCart(String email) {
        return cartRepository.findByUserEmail(email)
                .orElseGet(() -> {
                    User user = getUserByEmail(email);
                    Cart newCart = createNewCart(user);
                    log.debug("Created new cart for user: {}", email);
                    return cartRepository.save(newCart);
                });
    }

    private CartItem createCartItem(Cart cart, Product product, Integer quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .price(product.getPrice())
                .productName(product.getProductName())
                .imageUrl(product.getImageUrl())
                .build();
    }

    private void updateExistingItem(CartItem item, Integer additionalQuantity) {
        item.setQuantity(item.getQuantity() + additionalQuantity);
        item.updatePriceFromProduct();
        log.debug("Increased quantity of product {} to {}",
                item.getProduct().getId(), item.getQuantity());
    }

    private Cart createNewCart(User user) {
        return Cart.builder()
                .user(user)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new IllegalArgumentException("User not found");
                });
    }

    private Product getProductById(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", productId);
                    return new IllegalArgumentException("Product not found");
                });
    }

    private void validateUserEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private void validateAddToCartRequest(String email, Integer productId, Integer quantity) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }

    private void validateRemoveFromCartRequest(String email, Integer productId) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
    }

    private void validateUpdateQuantityRequest(String email, Integer productId, Integer quantity) {
        validateUserEmail(email);

        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Invalid product ID");
        }
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
    }
}