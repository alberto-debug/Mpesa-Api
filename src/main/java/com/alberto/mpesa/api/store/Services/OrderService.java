package com.alberto.mpesa.api.store.Services;

import com.alberto.mpesa.api.store.Repository.CartRepository;
import com.alberto.mpesa.api.store.Repository.IOrderRepository;
import com.alberto.mpesa.api.store.Repository.ProductRepository;
import com.alberto.mpesa.api.store.domain.model.Cart;
import com.alberto.mpesa.api.store.domain.model.CartItem;
import com.alberto.mpesa.api.store.domain.model.Order;
import com.alberto.mpesa.api.store.domain.Enums.CartStatus;
import com.alberto.mpesa.api.store.domain.model.Product;
import com.fc.sdk.APIContext;
import com.fc.sdk.APIRequest;
import com.fc.sdk.APIResponse;
import com.fc.sdk.APIMethodType;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private final IOrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private static final Dotenv dotenv = Dotenv.load();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public Order checkout(Long cartId, String phoneNumber, String paymentMethod) {
        System.out.println("Starting checkout process for cartId: " + cartId);

        // Fetch cart
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));

        System.out.println("Cart found with " + cart.getCartItems().size() + " items");

        // Validate cart
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException("Cannot checkout an empty cart");
        }

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalStateException("Cannot checkout an inactive cart");
        }

        // Validate payment method and phone number
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }

        if (paymentMethod.equals("mpesa")) {
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                throw new IllegalArgumentException("Phone number is required for M-Pesa payments");
            }
            if (!phoneNumber.matches("^258\\d{9}$")) {
                throw new IllegalArgumentException("Invalid phone number format. Use 258xxxxxxxxx for Mozambique");
            }
        }

        // Calculate total
        BigDecimal total = calculateTotal(cart.getCartItems());
        System.out.println("Cart total calculated: " + total);

        // Validate stock availability (but don't update yet for M-Pesa)
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
        }

        // Initialize transaction reference
        String transactionReference = generateTransactionReference();

        // Create order
        Order order = new Order();
        order.setItems(cart.getCartItems().stream().collect(Collectors.toList()));
        order.setPrice(total);
        order.setCreatedAt(LocalDateTime.now());
        order.setPaymentMethod(paymentMethod);
        order.setPhoneNumber(paymentMethod.equals("mpesa") ? phoneNumber : null);
        order.setCheckoutRequestId(transactionReference);

        // Set initial payment status
        if (paymentMethod.equals("cash")) {
            order.setPaymentStatus("COMPLETED");
        } else {
            order.setPaymentStatus("PENDING");
        }

        // Save order
        Order savedOrder = orderRepository.save(order);
        System.out.println("Order created with ID: " + savedOrder.getId() + " and CheckoutRequestID: " + transactionReference);

        // Process payment based on method
        if (paymentMethod.equals("mpesa")) {
            System.out.println("Initiating M-Pesa payment");

            MpesaPaymentResult result = initiateMpesaPayment(
                    transactionReference,
                    phoneNumber,
                    total.toString(),
                    generateThirdPartyReference()
            );

            System.out.println("M-Pesa response: " + result.getFullResponse());

            if (!result.isSuccess()) {
                // If M-Pesa initiation fails, mark order as failed
                savedOrder.setPaymentStatus("FAILED");
                orderRepository.save(savedOrder);
                throw new RuntimeException("Failed to initiate M-Pesa payment: " + result.getErrorMessage());
            }

            // Update order with M-Pesa CheckoutRequestID
            if (result.getCheckoutRequestId() != null) {
                savedOrder.setCheckoutRequestId(result.getCheckoutRequestId());
                orderRepository.save(savedOrder);
                System.out.println("Order updated with CheckoutRequestID: " + result.getCheckoutRequestId());
            }

            // Start transaction status query as fallback
            if (result.getCheckoutRequestId() != null) {
                new Thread(() -> queryTransactionStatus(result.getCheckoutRequestId(), savedOrder)).start();
            }

        } else if (paymentMethod.equals("cash")) {
            // For cash payments, complete the transaction immediately
            completeOrder(savedOrder, cart);
        }

        return savedOrder;
    }

    @Transactional
    public void processPaymentCallback(Map<String, Object> callbackData) {
        System.out.println("=== PROCESSING PAYMENT CALLBACK ===");
        try {
            // Log the entire callback payload
            String rawPayload = objectMapper.writeValueAsString(callbackData);
            System.out.println("Raw callback payload: " + rawPayload);

            // Try different possible keys for transaction reference
            String transactionReference = null;
            String[] possibleRefKeys = {
                    "CheckoutRequestID",
                    "input_TransactionReference",
                    "TransactionReference",
                    "transactionReference",
                    "transaction_reference"
            };

            for (String key : possibleRefKeys) {
                if (callbackData.containsKey(key)) {
                    transactionReference = String.valueOf(callbackData.get(key));
                    System.out.println("Found transaction reference with key '" + key + "': " + transactionReference);
                    break;
                }
            }

            // Try different possible keys for status
            String status = null;
            String[] possibleStatusKeys = {
                    "output_ResponseCode",
                    "ResultCode",
                    "input_TransactionStatus",
                    "TransactionStatus",
                    "transactionStatus",
                    "status"
            };

            for (String key : possibleStatusKeys) {
                if (callbackData.containsKey(key)) {
                    status = String.valueOf(callbackData.get(key));
                    System.out.println("Found status with key '" + key + "': " + status);
                    break;
                }
            }

            if (transactionReference == null || status == null) {
                System.err.println("ERROR: Missing required callback data");
                System.err.println("Transaction reference: " + transactionReference);
                System.err.println("Status: " + status);
                System.err.println("Available keys: " + callbackData.keySet());
                throw new IllegalArgumentException("Invalid callback data: missing transactionReference or status");
            }

            // Find the order by CheckoutRequestID
            String finalTransactionReference = transactionReference;
            Order order = orderRepository.findByCheckoutRequestId(transactionReference)
                    .orElseThrow(() -> {
                        System.err.println("ERROR: Order not found for transaction: " + finalTransactionReference);
                        // List all pending orders for debugging
                        System.err.println("Available pending orders:");
                        orderRepository.findAll().stream()
                                .filter(o -> "PENDING".equals(o.getPaymentStatus()))
                                .forEach(o -> System.err.println("Order ID: " + o.getId() + ", CheckoutRequestID: " + o.getCheckoutRequestId()));
                        return new RuntimeException("Order not found for transaction: " + finalTransactionReference);
                    });

            System.out.println("Found order: " + order.getId() + " with current status: " + order.getPaymentStatus());

            // Determine if payment was successful
            boolean isSuccess = isPaymentSuccessful(status);
            System.out.println("Payment successful: " + isSuccess + " (status: " + status + ")");

            if (isSuccess) {
                // Payment successful - complete the order
                System.out.println("Processing successful payment for order: " + order.getId());

                // Find the original cart
                Long cartId = null;
                if (!order.getItems().isEmpty()) {
                    cartId = order.getItems().get(0).getCart().getId();
                }

                if (cartId != null) {
                    Cart cart = cartRepository.findById(cartId).orElse(null);
                    if (cart != null && cart.getStatus() == CartStatus.ACTIVE) {
                        completeOrder(order, cart);
                        System.out.println("Order completed and cart cleared for order: " + order.getId());
                    } else {
                        System.out.println("Cart not found or already processed for order: " + order.getId());
                    }
                }

                order.setPaymentStatus("COMPLETED");
                System.out.println("Order " + order.getId() + " marked as COMPLETED");

            } else {
                // Payment failed
                order.setPaymentStatus("FAILED");
                System.out.println("Order " + order.getId() + " marked as FAILED");
            }

            orderRepository.save(order);
            System.out.println("=== CALLBACK PROCESSING COMPLETE ===");
        } catch (Exception e) {
            System.err.println("Callback processing failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Callback processing failed: " + e.getMessage());
        }
    }

    private boolean isPaymentSuccessful(String status) {
        if (status == null) return false;

        // Common success indicators for M-Pesa Mozambique
        String statusUpper = status.toUpperCase();
        return statusUpper.equals("SUCCESS") ||
                statusUpper.equals("SUCCESSFUL") ||
                statusUpper.equals("COMPLETED") ||
                statusUpper.equals("INS-0") ||
                statusUpper.equals("0");
    }

    private void completeOrder(Order order, Cart cart) {
        System.out.println("Completing order: " + order.getId());

        // Update product stock
        for (CartItem item : cart.getCartItems()) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                System.err.println("WARNING: Insufficient stock for product: " + product.getName());
                // Still proceed but log the issue
            }
            product.setStockQuantity(Math.max(0, newStock)); // Ensure stock doesn't go negative
            productRepository.save(product);
            System.out.println("Updated stock for " + product.getName() + " to " + newStock);
        }

        // Clear and complete cart
        cart.getCartItems().clear();
        cart.setTotal(BigDecimal.ZERO);
        cart.setStatus(CartStatus.COMPLETED);
        cartRepository.save(cart);

        System.out.println("Cart cleared and marked as completed");
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
    }

    private BigDecimal calculateTotal(Set<CartItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateTransactionReference() {
        return "TXN" + System.currentTimeMillis();
    }

    private String generateThirdPartyReference() {
        return "TPR" + System.currentTimeMillis();
    }

    private MpesaPaymentResult initiateMpesaPayment(String transactionReference, String customerMSISDN,
                                                    String amount, String thirdPartyReference) {
        try {
            System.out.println("=== INITIATING M-PESA PAYMENT ===");
            System.out.println("Transaction Reference: " + transactionReference);
            System.out.println("Customer MSISDN: " + customerMSISDN);
            System.out.println("Amount: " + amount);
            System.out.println("Third Party Reference: " + thirdPartyReference);

            // Validate M-Pesa credentials
            String apiKey = dotenv.get("MpesaApiKey");
            String publicKey = dotenv.get("MpesaPublicKey");
            String serviceProviderCode = dotenv.get("MpesaServiceProviderCode", "171717");
            String callbackUrl = "https://c75114c33d45.ngrok-free.app/api/orders/payment-callback";

            if (apiKey == null || publicKey == null) {
                return new MpesaPaymentResult(false, "M-Pesa credentials not configured", "", null);
            }

            System.out.println("Using Service Provider Code: " + serviceProviderCode);
            System.out.println("Callback URL: " + callbackUrl);

            APIContext context = new APIContext();
            context.setApiKey(apiKey);
            context.setPublicKey(publicKey);
            context.setSsl(true);
            context.setMethodType(APIMethodType.POST);
            context.setAddress("api.sandbox.vm.co.mz");
            context.setPort(18352);
            context.setPath("/ipg/v1x/c2bPayment/singleStage/");
            context.addHeader("Origin", "developer.mpesa.vm.co.mz");

            // Add parameters with proper validation
            context.addParameter("input_TransactionReference", transactionReference);
            context.addParameter("input_CustomerMSISDN", customerMSISDN);
            context.addParameter("input_Amount", amount);
            context.addParameter("input_ThirdPartyReference", thirdPartyReference);
            context.addParameter("input_ServiceProviderCode", serviceProviderCode);
            context.addParameter("input_CallbackUrl", callbackUrl);

            APIRequest request = new APIRequest(context);
            APIResponse response = request.execute();

            if (response == null) {
                return new MpesaPaymentResult(false, "No response from M-Pesa API", "", null);
            }

            String result = response.getResult();
            System.out.println("M-Pesa API raw response: " + result);

            // Parse the response
            try {
                JsonNode jsonResponse = objectMapper.readTree(result);
                String responseCode = jsonResponse.get("output_ResponseCode").asText();
                String responseDesc = jsonResponse.get("output_ResponseDesc").asText();
                String checkoutRequestId = jsonResponse.has("output_CheckoutRequestID") ? jsonResponse.get("output_CheckoutRequestID").asText() : null;

                System.out.println("M-Pesa Response Code: " + responseCode);
                System.out.println("M-Pesa Response Description: " + responseDesc);
                System.out.println("CheckoutRequestID: " + checkoutRequestId);

                if ("INS-0".equals(responseCode)) {
                    System.out.println("M-Pesa payment initiated successfully");
                    return new MpesaPaymentResult(true, "Payment initiated successfully", result, checkoutRequestId);
                } else {
                    String errorMessage = "M-Pesa Error " + responseCode + ": " + responseDesc;
                    System.err.println("M-Pesa Error: " + errorMessage);

                    if ("INS-21".equals(responseCode)) {
                        errorMessage = "Parameter validation failed. Please check your phone number and amount.";
                    } else if ("INS-6".equals(responseCode)) {
                        errorMessage = "Transaction failed. Please try again.";
                    }

                    return new MpesaPaymentResult(false, errorMessage, result, checkoutRequestId);
                }
            } catch (Exception e) {
                System.err.println("Error parsing M-Pesa response: " + e.getMessage());
                return new MpesaPaymentResult(false, "Invalid response format from M-Pesa", result, null);
            }

        } catch (Exception e) {
            System.err.println("Error initiating M-Pesa payment: " + e.getMessage());
            e.printStackTrace();
            return new MpesaPaymentResult(false, "Technical error: " + e.getMessage(), "", null);
        }
    }

    private void queryTransactionStatus(String checkoutRequestId, Order order) {
        try {
            System.out.println("=== QUERYING M-PESA TRANSACTION STATUS ===");
            System.out.println("CheckoutRequestID: " + checkoutRequestId);

            String apiKey = dotenv.get("MpesaApiKey");
            String publicKey = dotenv.get("MpesaPublicKey");
            String initiator = dotenv.get("MpesaInitiator", "testapi");
            String securityCredential = dotenv.get("MpesaSecurityCredential");

            if (apiKey == null || publicKey == null || securityCredential == null) {
                System.err.println("Missing M-Pesa credentials for transaction status query");
                return;
            }

            APIContext context = new APIContext();
            context.setApiKey(apiKey);
            context.setPublicKey(publicKey);
            context.setSsl(true);
            context.setMethodType(APIMethodType.POST);
            context.setAddress("api.sandbox.vm.co.mz");
            context.setPort(18345); // Adjust port for Transaction Status API
            context.setPath("/mpesa/transactionstatus/v1/query");
            context.addHeader("Origin", "developer.mpesa.vm.co.mz");

            context.addParameter("Initiator", initiator);
            context.addParameter("SecurityCredential", securityCredential);
            context.addParameter("CommandID", "TransactionStatusQuery");
            context.addParameter("TransactionID", checkoutRequestId);
            context.addParameter("PartyA", dotenv.get("MpesaServiceProviderCode", "171717"));
            context.addParameter("IdentifierType", "4"); // Shortcode
            context.addParameter("ResultURL", "https://c75114c33d45.ngrok-free.app/api/orders/payment-callback");
            context.addParameter("QueueTimeOutURL", "https://c75114c33d45.ngrok-free.app/api/orders/timeout");
            context.addParameter("Remarks", "Status query");
            context.addParameter("Occasion", "Status query");

            APIRequest request = new APIRequest(context);
            APIResponse response = request.execute();

            if (response == null) {
                System.err.println("No response from Transaction Status API");
                return;
            }

            String result = response.getResult();
            System.out.println("Transaction Status API response: " + result);

            JsonNode jsonResponse = objectMapper.readTree(result);
            String responseCode = jsonResponse.get("ResponseCode").asText();
            String responseDesc = jsonResponse.get("ResponseDescription").asText();
            String transactionStatus = jsonResponse.has("TransactionStatus") ? jsonResponse.get("TransactionStatus").asText() : null;

            System.out.println("Response Code: " + responseCode);
            System.out.println("Response Description: " + responseDesc);
            System.out.println("Transaction Status: " + transactionStatus);

            // Process the status
            if (order != null) {
                if ("0".equals(responseCode) && "Completed".equalsIgnoreCase(transactionStatus)) {
                    order.setPaymentStatus("COMPLETED");
                    Cart cart = cartRepository.findById(order.getItems().get(0).getCart().getId()).orElse(null);
                    if (cart != null) {
                        completeOrder(order, cart);
                    }
                } else {
                    order.setPaymentStatus("FAILED");
                }
                orderRepository.save(order);
                System.out.println("Order " + order.getId() + " updated to " + order.getPaymentStatus() + " via transaction status query");
            }

        } catch (Exception e) {
            System.err.println("Error querying transaction status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper class for M-Pesa payment results
    private static class MpesaPaymentResult {
        private final boolean success;
        private final String errorMessage;
        private final String fullResponse;
        private final String checkoutRequestId;

        public MpesaPaymentResult(boolean success, String errorMessage, String fullResponse,
                                  String checkoutRequestId) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.fullResponse = fullResponse;
            this.checkoutRequestId = checkoutRequestId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public String getFullResponse() {
            return fullResponse;
        }

        public String getCheckoutRequestId() {
            return checkoutRequestId;
        }
    }
}