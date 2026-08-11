package com.registration.cart;

import com.registration.dto.AddToCartRequest;
import com.registration.dto.CartItemResponse;
import com.registration.dto.CartResponse;
import com.registration.entity.CartItem;
import com.registration.entity.User;
import com.registration.exception.BadRequestException;
import com.registration.exception.ResourceNotFoundException;
import com.registration.product.Product;
import com.registration.product.ProductRepository;
import com.registration.repository.CartItemRepository;
import com.registration.security.CustomAuthentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    private User getCurrentUser() {
        CustomAuthentication auth =
                (CustomAuthentication) SecurityContextHolder.getContext().getAuthentication();
        return auth.getUser();
    }

    public void addToCart(AddToCartRequest request) {
        User user = getCurrentUser();
        Integer productId = request.getProductId();
        Integer quantity = request.getQuantity();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (quantity > product.getStock()) {
            throw new BadRequestException(
                    "Requested quantity (" + quantity + ") exceeds available stock (" + product.getStock() + ")"
            );
        }

        Optional<CartItem> existing = cartItemRepository
                .findByUser_UserIdAndProduct_ProductId(user.getUserId(), productId);

        if (existing.isPresent()) {
            CartItem cartItem = existing.get();
            int newQuantity = cartItem.getQuantity() + quantity;
            if (newQuantity > product.getStock()) {
                throw new BadRequestException(
                        "Requested quantity exceeds available stock"
                );
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }
    }

    public CartResponse getCart() {
        User user = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser_UserId(user.getUserId());

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            BigDecimal price = product.getPrice();
            int quantity = cartItem.getQuantity();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
            totalAmount = totalAmount.add(subtotal);

            String productImage = null;
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                productImage = product.getImages().get(0).getImageUrl();
            }

            itemResponses.add(new CartItemResponse(
                    cartItem.getId(),
                    product.getProductId(),
                    product.getName(),
                    productImage,
                    price,
                    quantity,
                    subtotal
            ));
        }

        return new CartResponse(itemResponses, totalAmount);
    }

    @Transactional
    public void updateQuantity(Integer productId, Integer newQuantity) {
        User user = getCurrentUser();

        if (newQuantity <= 0) {
            cartItemRepository.deleteByUser_UserIdAndProduct_ProductId(user.getUserId(), productId);
            return;
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (newQuantity > product.getStock()) {
            throw new BadRequestException(
                    "Requested quantity (" + newQuantity + ") exceeds available stock (" + product.getStock() + ")"
            );
        }

        CartItem cartItem = cartItemRepository
                .findByUser_UserIdAndProduct_ProductId(user.getUserId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found for product id: " + productId));

        cartItem.setQuantity(newQuantity);
        cartItemRepository.save(cartItem);
    }

    @Transactional
    public void removeFromCart(Integer productId) {
        User user = getCurrentUser();
        cartItemRepository.deleteByUser_UserIdAndProduct_ProductId(user.getUserId(), productId);
    }

    @Transactional
    public void clearCart() {
        User user = getCurrentUser();
        cartItemRepository.deleteByUser_UserId(user.getUserId());
    }
}
