package com.example.civicpulse.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // In-memory OTP storage
    private final Map<String, OtpData> otpStorage =
            new ConcurrentHashMap<>();

    // Secure OTP generator
    private final SecureRandom random = new SecureRandom();

    // API key from Render Environment Variables
    @Value("${SMS_API_KEY:}")
    private String smsApiKey;

    // ─────────────────────────────────────────────
    // GENERATE + SEND OTP
    // ─────────────────────────────────────────────
    public String generateAndSendOtp(String mobileNumber) {

        // Clean mobile number
        String cleanMobile = sanitizeMobile(mobileNumber);

        // Generate 6-digit OTP
        String otp = String.format(
                "%06d",
                random.nextInt(1000000)
        );

        // Store OTP for 5 minutes
        OtpData data = new OtpData(
                otp,
                LocalDateTime.now().plusMinutes(5)
        );

        otpStorage.put(cleanMobile, data);

        // Console fallback
        System.out.println(
                "🔐 Generated OTP for "
                        + cleanMobile
                        + " : "
                        + otp
        );

        // Check API key
        if (smsApiKey == null || smsApiKey.isBlank()) {

            System.err.println(
                    "❌ SMS_API_KEY missing from environment variables."
            );

            return otp;
        }

        // Send SMS
        try {

            String message =
                    "Your CivicChain OTP is "
                            + otp
                            + ". Valid for 5 minutes.";

            String encodedMessage =
                    URLEncoder.encode(message, "UTF-8");

            String urlString =
                    "https://www.fast2sms.com/dev/bulkV2"
                            + "?authorization=" + smsApiKey.trim()
                            + "&route=q"
                            + "&message=" + encodedMessage
                            + "&language=english"
                            + "&flash=0"
                            + "&numbers=" + cleanMobile;

            URL url = new URL(urlString);

            HttpURLConnection conn =
                    (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");

            conn.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0"
            );

            conn.setRequestProperty(
                    "Accept",
                    "application/json"
            );

            int responseCode = conn.getResponseCode();

            BufferedReader reader;

            // SUCCESS RESPONSE
            if (responseCode >= 200 && responseCode < 300) {

                reader = new BufferedReader(
                        new InputStreamReader(
                                conn.getInputStream()
                        )
                );

            } else {

                // ERROR RESPONSE
                reader = new BufferedReader(
                        new InputStreamReader(
                                conn.getErrorStream()
                        )
                );
            }

            StringBuilder response = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            System.out.println(
                    "📩 Fast2SMS Response: "
                            + response
            );

        } catch (Exception e) {

            System.err.println(
                    "❌ Failed to send SMS"
            );

            e.printStackTrace();
        }

        return otp;
    }

    // ─────────────────────────────────────────────
    // VERIFY OTP
    // ─────────────────────────────────────────────
    public boolean verifyOtp(
            String mobileNumber,
            String inputOtp
    ) {

        String cleanMobile =
                sanitizeMobile(mobileNumber);

        OtpData data =
                otpStorage.get(cleanMobile);

        // OTP missing
        if (data == null) {
            return false;
        }

        // OTP expired
        if (LocalDateTime.now().isAfter(data.expiry)) {

            otpStorage.remove(cleanMobile);

            return false;
        }

        // Compare OTP
        boolean valid =
                data.otp.equals(inputOtp.trim());

        // Consume OTP after success
        if (valid) {

            otpStorage.remove(cleanMobile);

            System.out.println(
                    "✅ OTP verified for "
                            + cleanMobile
            );
        }

        return valid;
    }

    // ─────────────────────────────────────────────
    // MOBILE SANITIZER
    // ─────────────────────────────────────────────
    private String sanitizeMobile(String mobile) {

        String clean =
                mobile.trim().replaceAll("\\s+", "");

        if (clean.startsWith("+91")) {
            clean = clean.substring(3);
        }

        if (clean.startsWith("0")) {
            clean = clean.substring(1);
        }

        return clean;
    }

    // ─────────────────────────────────────────────
    // OTP DATA MODEL
    // ─────────────────────────────────────────────
    private static class OtpData {

        String otp;

        LocalDateTime expiry;

        OtpData(
                String otp,
                LocalDateTime expiry
        ) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
