package com.registration.cart;

import com.registration.dto.AddToCartRequest;
import com.registration.dto.CartResponse;
import com.registration.dto.UpdateQuantityRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        cartService.addToCart(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        CartResponse cartResponse = cartService.getCart();
        return ResponseEntity.ok(cartResponse);
    }

    @PutMapping("/update")
    public ResponseEntity<Void> updateQuantity(@RequestBody UpdateQuantityRequest request) {
        cartService.updateQuantity(request.getProductId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer productId) {
        cartService.removeFromCart(productId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
