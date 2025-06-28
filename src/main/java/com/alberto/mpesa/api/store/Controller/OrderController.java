package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.Services.OrderService;
import com.alberto.mpesa.api.store.domain.model.Order;
import com.fc.sdk.APIContext;
import com.fc.sdk.APIRequest;
import com.fc.sdk.APIResponse;
import com.fc.sdk.APIMethodType;
import io.github.cdimascio.dotenv.Dotenv; // Library to load .env variables
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private static final Dotenv dotenv = Dotenv.load(); // Static initialization to avoid reloading

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam Long cartId) {
        // Process checkout for the given cart ID
        Order order = orderService.checkout(cartId);
        return ResponseEntity.ok(order != null ? order : new Order()); // Return default if null
    }

    @PostMapping("/initiate-payment")
    public ResponseEntity<String> initiatePayment(
            @RequestParam String transactionReference, // Unique transaction reference
            @RequestParam String customerMSISDN,       // Customer's phone number
            @RequestParam String amount,              // Transaction amount
            @RequestParam String thirdPartyReference) { // Unique third-party reference
        // Validate inputs (basic check)
        if (transactionReference == null || customerMSISDN == null || amount == null || thirdPartyReference == null) {
            return ResponseEntity.badRequest().body("Missing required parameters");
        }
        // Initiate M-Pesa payment with provided parameters
        String paymentResponse = initiateMpesaPayment(transactionReference, customerMSISDN, amount, thirdPartyReference);

        return ResponseEntity.ok(paymentResponse != null ? paymentResponse : "Error: No response");
    }

    private String initiateMpesaPayment(String transactionReference, String customerMSISDN, String amount, String thirdPartyReference) {

        // Set up API context using M-Pesa SDK
        APIContext context = new APIContext();
        context.setApiKey(dotenv.get("MpesaApiKey")); // Load API key from .env
        context.setPublicKey(dotenv.get("MpesaPublicKey")); // Load public key from .env
        context.setSsl(true); // Enable SSL for secure communication
        context.setMethodType(APIMethodType.POST); // Use POST method
        context.setAddress("api.sandbox.vm.co.mz"); // Sandbox API address
        context.setPort(18352); // Sandbox port
        context.setPath("/ipg/v1x/c2bPayment/singleStage/"); // API endpoint path
        context.addHeader("Origin", "developer.mpesa.vm.co.mz"); // Set CORS origin
        context.addParameter("input_TransactionReference", transactionReference); // Add transaction reference
        context.addParameter("input_CustomerMSISDN", customerMSISDN); // Add customer phone
        context.addParameter("input_Amount", amount); // Add amount
        context.addParameter("input_ThirdPartyReference", thirdPartyReference); // Add third-party reference
        context.addParameter("input_ServiceProviderCode", dotenv.get("MpesaServiceProviderCode", "171717")); // Configurable shortcode

        // Execute the API request
        APIRequest request = new APIRequest(context);
        APIResponse response = request.execute();
        return response != null ? response.getResult() : "Error: No response"; // Return result or error
    }

    @PostMapping("/payment-callback")
    public ResponseEntity<String> paymentCallback(@RequestBody String callbackData) {
        // Log callback data for testing and debugging
        System.out.println("Callback received: " + callbackData);
        // Add logic here to process the callback (e.g., update order status)
        return ResponseEntity.ok("Callback received");
    }
}