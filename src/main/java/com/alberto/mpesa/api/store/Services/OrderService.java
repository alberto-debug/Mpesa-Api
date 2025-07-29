package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.IOrderRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.model.Cart;
import com.alberto.mpesa.api.store.domain.model.CartItem;
import com.alberto.mpesa.api.store.domain.model.Order;
import com.alberto.mpesa.api.store.domain.Enums.CartStatus;
import com.alberto.mpesa.api.store.domain.model.Product;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private final IOrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    // M-Pesa credentials (replace with actual values)
    private static final String CONSUMER_KEY = "your_consumer_key";
    private static final String CONSUMER_SECRET = "your_consumer_secret";
    private static final String SHORTCODE = "your_shortcode";
    private static final String PASSKEY = "your_passkey";
    private static final String MPESA_API_URL = "https://sandbox.safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    private static final String TOKEN_URL = "https://sandbox.safaricom.co.ke/oauth/v1/generate?grant_type=client_credentials";

    @Transactional
    public Map<String, String> checkout(Long cartId, String phoneNumber, String paymentMethod) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));

        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cannot checkout an inactive cart");
        }

        if (paymentMethod.equals("mpesa") && (phoneNumber == null || !phoneNumber.matches("^254\\d{9}$"))) {
            throw new IllegalArgumentException("Invalid phone number format. Use 254xxxxxxxxx");
        }

        BigDecimal total = calculateTotal(cart.getCartItems());
        String checkoutRequestId = "N/A"; // Initialize to avoid undefined variable

        if (paymentMethod.equals("mpesa")) {
            // Initiate M-Pesa STK push
            checkoutRequestId = initiateMpesaPayment(phoneNumber, total);
            if (checkoutRequestId == null) {
                throw new RuntimeException("Failed to initiate M-Pesa payment");
            }
        }

        // Create order
        Order order = new Order();
        order.setItems(cart.getCartItems().stream().collect(Collectors.toList()));
        order.setPrice(total);
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        order.setPhoneNumber(paymentMethod.equals("mpesa") ? phoneNumber : null);

        // Update stock quantities
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }

        // Clear cart and set status to COMPLETED
        cart.getCartItems().clear();
        cart.setTotal(BigDecimal.ZERO);
        cart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(cart);

        orderRepository.save(order);

        return Map.of(
                "message", "Payment initiated successfully",
                "checkoutRequestId", checkoutRequestId
        );
    }

    private BigDecimal calculateTotal(Set<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String initiateMpesaPayment(String phoneNumber, BigDecimal amount) {
        try {
            // Get OAuth token
            String token = getMpesaAccessToken();

            // Prepare STK push request
            String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String password = Base64.getEncoder().encodeToString((SHORTCODE + PASSKEY + timestamp).getBytes());

            Map<String, Object> request = new HashMap<>();
            request.put("BusinessShortCode", SHORTCODE);
            request.put("Password", password);
            request.put("Timestamp", timestamp);
            request.put("TransactionType", "CustomerPayBillOnline");
            request.put("Amount", amount.intValue());
            request.put("PartyA", phoneNumber);
            request.put("PartyB", SHORTCODE);
            request.put("PhoneNumber", phoneNumber);
            request.put("CallBackURL", "https://your-callback-url.com/callback"); // Replace with actual callback URL
            request.put("AccountReference", "StoreCheckout");
            request.put("TransactionDesc", "Payment for store order");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(MPESA_API_URL, HttpMethod.POST, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && "0".equals(responseBody.get("ResponseCode"))) {
                return (String) responseBody.get("CheckoutRequestID");
            } else {
                throw new RuntimeException("M-Pesa API error: " + responseBody);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate M-Pesa payment: " + e.getMessage());
        }
    }

    private String getMpesaAccessToken() {
        String credentials = CONSUMER_KEY + ":" + CONSUMER_SECRET;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedCredentials);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(TOKEN_URL, HttpMethod.GET, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("access_token")) {
            return (String) responseBody.get("access_token");
        }
        throw new RuntimeException("Failed to obtain M-Pesa access token");
    }
}