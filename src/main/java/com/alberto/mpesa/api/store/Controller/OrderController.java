package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.Services.OrderService;
import com.alberto.mpesa.api.store.domain.model.Order;
import com.fc.sdk.APIContext;
import com.fc.sdk.APIRequest;
import com.fc.sdk.APIResponse;
import com.fc.sdk.APIMethodType;
import io.github.cdimascio.dotenv.Dotenv; // Library to load .env variables
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/order")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final Dotenv dotenv = Dotenv.load(); // Initialize Dotenv to load .env file

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@RequestParam Long cartId) {
        // Process checkout for the given cart ID
        Order order = orderService.checkout(cartId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/initiate-payment")
    public ResponseEntity<String> initiatePayment(
            @RequestParam String transactionReference, // Unique transaction reference
            @RequestParam String customerMSISDN,       // Customer's phone number
            @RequestParam String amount,              // Transaction amount
            @RequestParam String thirdPartyReference) { // Unique third-party reference
        // Initiate M-Pesa payment with provided parameters
        String paymentResponse = initiateMpesaPayment(transactionReference, customerMSISDN, amount, thirdPartyReference);
        return ResponseEntity.ok(paymentResponse);
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
        context.addParameter("input_ServiceProviderCode", "171717"); // Add business shortcode

        // Execute the API request
        APIRequest request = new APIRequest(context);
        APIResponse response = request.execute();
        return response != null ? response.getResult() : "Error: No response"; // Return result or error
    }
}