package com.alberto.mpesa.api.store.Controller;

import com.alberto.mpesa.api.store.domain.model.Order;
import com.alberto.mpesa.api.store.Services.OrderService;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestParam Long cartId) {
        Order order = orderService.checkout(cartId);
        String paymentResponse = initiateMpesaPayment(order.getId(), order.getPrice());
        return ResponseEntity.ok(paymentResponse);
    }

    private String initiateMpesaPayment(Long orderId, java.math.BigDecimal amount) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://api.mpesa.vm.co.mz/c2b/payment"); // Replace with actual URL

            String apiKey = "aaaab09uz9f3asdcjyk7els777ihmwv8";
            String publicKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAszE+xAKVB9HRarr6/uHYYAX/RdD6KGVIGlHv98QKDIH26ldYJQ7zOuo9qEscO0M1psSPe/67AWYLEXh13fbtcSKGP6WFjT9OY6uV5ykw9508x1sW8UQ4ZhTRNrlNsKizE/glkBfcF2lwDXJGQennwgickWz7VN+AP/1c4DnMDfcl8iVIDlsbudFoXQh5aLCYl+XOMt/vls5a479PLMkPcZPOgMTCYTCE6ReX3KD2aGQ62uiu2T4mK+7Z6yvKvhPRF2fTKI+zOFWly//IYlyB+sde42cIU/588msUmgr3G9FYyN2vKPVy/MhIZpiFyVc3vuAAJ/mzue5p/G329wzgcz0ztyluMNAGUL9A4ZiFcKOebT6y6IgIMBeEkTwyhsxRHMFXlQRgTAufaO5hiR/usBMkoazJ6XrGJB8UadjH2m2+kdJIieI4FbjzCiDWKmuM58rllNWdBZK0XVHNsxmBy7yhYw3aAIhFS0fNEuSmKTfFpJFMBzIQYbdTgI28rZPAxVEDdRaypUqBMCq4OstCxgGvR3Dy1eJDjlkuiWK9Y9RGKF8HOI5a4ruHyLheddZxsUihziPF9jKTknsTZtF99eKTIjhV7qfTzxXq+8GGoCEABIyu26LZuL8X12bFqtwLAcjfjoB7HlRHtPszv6PJ0482ofWmeH0BE8om7VrSGxsCAwEAAQ==";

            String bearerToken = getBearerToken(apiKey, publicKey);
            httpPost.setHeader("Authorization", "Bearer " + bearerToken);
            httpPost.setHeader("Content-Type", "application/json");

            JSONObject requestBody = new JSONObject();
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount.toString());
            requestBody.put("phoneNumber", "258xxxxxxxxx"); // Replace with customer's phone
            requestBody.put("referenceId", "ORDER_" + orderId);

            httpPost.setEntity(new StringEntity(requestBody.toString()));

            // Rename the response variable in the lambda to avoid conflict
            String result = httpClient.execute(httpPost, response -> {
                return EntityUtils.toString(response.getEntity());
            });

            return result;
        } catch (Exception e) {
            return "Error initiating payment: " + e.getMessage();
        }
    }

    private String getBearerToken(String apiKey, String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Cipher cipher = Cipher.getInstance("RSA");
        byte[] encodedPublicKey = Base64.getDecoder().decode(publicKey);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
        PublicKey pk = keyFactory.generatePublic(publicKeySpec);

        cipher.init(Cipher.ENCRYPT_MODE, pk);
        byte[] encryptedApiKey = Base64.getEncoder().encode(cipher.doFinal(apiKey.getBytes("UTF-8")));

        return new String(encryptedApiKey, "UTF-8");
    }
}