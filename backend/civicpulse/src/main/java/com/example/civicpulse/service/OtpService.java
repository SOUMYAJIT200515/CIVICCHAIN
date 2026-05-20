package com.example.civicpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    // Pulls your secret API key securely from Render Environment Variables
    @Value("${SMS_API_KEY:}")
    private String smsApiKey;

    public String generateAndSendOtp(String mobileNumber) {
        // 1. Generate a secure 6-digit OTP
        String otp = String.format("%06d", random.nextInt(1000000)); 

        OtpData data = new OtpData(otp, LocalDateTime.now().plusMinutes(5));
        otpStorage.put(mobileNumber, data);

        // 2. Print to Render log as a safe backup
        System.out.println("🔐 Securely generated OTP for " + mobileNumber + " is: " + otp);

        // 3. Real-Life SMS Gateway API Dispatch
        if (smsApiKey == null || smsApiKey.isEmpty()) {
            System.err.println("❌ SMS Gateway Error: SMS_API_KEY is missing in Render Environment Settings.");
            return otp;
        }

        try {
            // Construct the message text safely encoded for URLs
            String message = URLEncoder.encode("Your CivicChain security verification code is: " + otp + ". Valid for 5 minutes.", StandardCharsets.UTF_8);
            
            // Fast2SMS API Authorization URL endpoint
            String urlString = "https://www.fast2sms.com/dev/bulkV2?authorization=" + smsApiKey 
                             + "&variables_values=" + otp 
                             + "&route=otp&numbers=" + mobileNumber;

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Read the gateway server's response code
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                System.out.println("🚀 SMS Gateway Response: " + response.toString());
            } else {
                System.err.println("❌ SMS Gateway HTTP Error Code: " + responseCode);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to reach SMS network gateway: " + e.getMessage());
        }

        return otp; 
    }

    public boolean verifyOtp(String mobileNumber, String inputOtp) {
        OtpData data = otpStorage.get(mobileNumber);

        if (data == null) return false;
        if (LocalDateTime.now().isAfter(data.expiry)) {
            otpStorage.remove(mobileNumber);
            return false;
        }

        boolean valid = data.otp.equals(inputOtp);
        if (valid) {
            otpStorage.remove(mobileNumber); // Prevent reuse attacks
        }
        return valid;
    }

    private static class OtpData {
        String otp;
        LocalDateTime expiry;

        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
