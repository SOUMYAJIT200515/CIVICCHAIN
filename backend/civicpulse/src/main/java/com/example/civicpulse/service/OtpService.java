package com.example.civicpulse.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public String generateAndSendOtp(String voterId) {
        String otp = String.format("%06d", random.nextInt(1000000)); // 6 digit OTP

        OtpData data = new OtpData(otp, LocalDateTime.now().plusMinutes(5));
        otpStorage.put(voterId, data);

        // TODO: In production, integrate SMS gateway here (Twilio, MSG91, etc.)
        System.out.println("🔐 OTP for " + voterId + " is: " + otp);

        return otp; // For development only
    }

    public boolean verifyOtp(String voterId, String inputOtp) {
        OtpData data = otpStorage.get(voterId);

        if (data == null) return false;
        if (LocalDateTime.now().isAfter(data.expiry)) {
            otpStorage.remove(voterId);
            return false;
        }

        boolean valid = data.otp.equals(inputOtp);
        if (valid) {
            otpStorage.remove(voterId); // OTP used - remove it
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