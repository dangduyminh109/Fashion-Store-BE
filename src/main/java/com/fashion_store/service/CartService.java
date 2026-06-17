package com.fashion_store.service;

import com.fashion_store.Utils.SecurityUtils;
import com.fashion_store.dto.cart.request.CartItemRequest;
import com.fashion_store.dto.cart.response.CartItemResponse;
import com.fashion_store.entity.Cart;
import com.fashion_store.entity.CartItem;
import com.fashion_store.entity.Customer;
import com.fashion_store.entity.Variant;
import com.fashion_store.exception.AppException;
import com.fashion_store.exception.ErrorCode;
import com.fashion_store.mapper.CartItemMapper;
import com.fashion_store.repository.CartItemRepository;
import com.fashion_store.repository.CartRepository;
import com.fashion_store.repository.CustomerRepository;
import com.fashion_store.repository.VariantRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {
    CartRepository cartRepository;
    CartItemMapper cartItemMapper;
    CustomerRepository customerRepository;
    CartItemRepository cartItemRepository;
    VariantRepository variantRepository;

    public CartItemResponse add(CartItemRequest request) {
        String customerId = SecurityUtils.getCurrentUserId();
        if (customerId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(customer);
            cart = cartRepository.save(cart);
            customer.setCart(cart);
            customerRepository.save(customer);
        }

        CartItem cartItem = cartItemMapper.toCartItem(request);
        Variant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (variant.getStatus() == false && variant.getIsDeleted() != true)
            throw new AppException(ErrorCode.VARIANT_NOT_AVAILABLE);
        if (variant.getInventory() < request.getQuantity())
            throw new AppException(ErrorCode.INVALID_QUANTITY_UPDATE);

        Optional<CartItem> itemExisted = cart.getCartItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (itemExisted.isPresent()) {
            cartItem = itemExisted.get();
            cartItem.setQuantity(request.getQuantity() + cartItem.getQuantity());
        } else {
            cartItem.setVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }
        cartItem.setCart(cart);

        cartItem = cartItemRepository.save(cartItem);
        cartRepository.save(cart);
        return cartItemMapper.toCartItemResponse(cartItem);
    }

    public CartItemResponse update(CartItemRequest request) {
        String customerId = SecurityUtils.getCurrentUserId();
        if (customerId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(customer);
            cart = cartRepository.save(cart);
            customer.setCart(cart);
            customerRepository.save(customer);
        }

        CartItem cartItem = cartItemMapper.toCartItem(request);
        Variant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));
        if (variant.getStatus() == false && variant.getIsDeleted() != true)
            throw new AppException(ErrorCode.VARIANT_NOT_AVAILABLE);
        if (variant.getInventory() < request.getQuantity())
            throw new AppException(ErrorCode.INVALID_QUANTITY_UPDATE);

        Optional<CartItem> itemExisted = cart.getCartItems().stream()
                .filter(item -> item.getVariant().getId().equals(variant.getId()))
                .findFirst();

        if (itemExisted.isPresent()) {
            cartItem = itemExisted.get();
            cartItem.setQuantity(request.getQuantity());
        } else {
            cartItem.setVariant(variant);
            cartItem.setQuantity(request.getQuantity());
        }
        cartItem.setCart(cart);

        cartItem = cartItemRepository.save(cartItem);
        cartRepository.save(cart);
        return cartItemMapper.toCartItemResponse(cartItem);
    }

    public List<CartItemResponse> getCart() {
        String customerId = SecurityUtils.getCurrentUserId();
        if (customerId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        Cart cart = customer.getCart();
        if (cart == null) {
            cart = new Cart();
            cart.setCustomer(customer);
            cart = cartRepository.save(cart);
            customer.setCart(cart);
            customerRepository.save(customer);
        }

        return cart.getCartItems().stream()
                .filter(item -> item.getVariant().getStatus() == true
                        && item.getVariant().getIsDeleted() != true)
                .map(cartItemMapper::toCartItemResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long variantId) {
        String customerId = SecurityUtils.getCurrentUserId();
        if (customerId == null)
            throw new AppException(ErrorCode.UNAUTHORIZED);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_EXIST));

        Cart cart = customer.getCart();
        if (cart == null) {
            return;
        }

        cartItemRepository.findByCartIdAndVariantId(cart.getId(), variantId)
                .ifPresent(cartItem -> {
                    cart.getCartItems().removeIf(item -> Objects.equals(item.getId(), cartItem.getId()));
                    cartItem.setCart(null);
                    cartItemRepository.delete(cartItem);
                });
    }
}
