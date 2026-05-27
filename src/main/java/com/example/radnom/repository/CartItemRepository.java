package com.example.radnom.repository;

import com.example.radnom.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {



    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Integer productId);

    default Optional<CartItem> findByCartIdAndProductProductId(Long cartId, Integer productId) {
        return findByCartIdAndProductId(cartId, productId);
    }

    void deleteByCartId(Long cartId);


    int countByCartId(Long cartId);

    boolean existsByCartIdAndProductId(Long cartId, Integer productId);


    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer countItemsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    Optional<CartItem> findByCartIdAndProductIdCustom(@Param("cartId") Long cartId,
                                                      @Param("productId") Integer productId);

    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END " +
            "FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId")
    boolean existsByCartIdAndProductIdCustom(@Param("cartId") Long cartId,
                                             @Param("productId") Integer productId);


    @Query("SELECT SUM(ci.quantity * ci.price) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Double calculateCartTotal(@Param("cartId") Long cartId);


    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.product WHERE ci.cart.id = :cartId")
    List<CartItem> findByCartIdWithProduct(@Param("cartId") Long cartId);
}