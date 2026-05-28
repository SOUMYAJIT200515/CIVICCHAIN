package com.example.civicpulse.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    // ─────────────────────────────────────────────
    // LOCAL OTP STORAGE (FOR NON-TWILIO NUMBERS)
    // ─────────────────────────────────────────────
    private final Map<String, OtpData> otpStorage =
            new ConcurrentHashMap<>();

    private final SecureRandom random =
            new SecureRandom();

    // ─────────────────────────────────────────────
    // TWILIO CONFIG
    // ─────────────────────────────────────────────
    @Value("${TWILIO_ACCOUNT_SID}")
    private String accountSid;

    @Value("${TWILIO_AUTH_TOKEN}")
    private String authToken;

    @Value("${TWILIO_VERIFY_SID}")
    private String verifySid;

    // YOUR REAL TEST NUMBER (digits only, no country code)
    private static final String REAL_SMS_NUMBER = "6290148614";

    // ─────────────────────────────────────────────
    // GENERATE + SEND OTP
    // ─────────────────────────────────────────────
    public String generateAndSendOtp(String mobileNumber) {

        String cleanMobile = sanitizeMobile(mobileNumber);

        // ── DEBUG LOGS ──────────────────────────────
        System.out.println("INPUT MOBILE        = [" + mobileNumber + "]");
        System.out.println("CLEAN MOBILE        = [" + cleanMobile + "]");
        System.out.println("CLEAN MOBILE LENGTH = " + cleanMobile.length());
        System.out.println("REAL MOBILE         = [" + REAL_SMS_NUMBER + "]");
        System.out.println("REAL MOBILE LENGTH  = " + REAL_SMS_NUMBER.length());
        System.out.println("MATCH               = " + cleanMobile.equals(REAL_SMS_NUMBER));
        System.out.println("CLEAN MOBILE BYTES  = " + Arrays.toString(cleanMobile.getBytes()));

        // ==================================================
        // REAL TWILIO OTP — sent only to the registered number
        // ==================================================
        if (cleanMobile.equals(REAL_SMS_NUMBER)) {

            System.out.println(">>> ENTERED TWILIO BLOCK — sending real SMS");

            try {

                Twilio.init(accountSid, authToken);

                Verification.creator(
                        verifySid,
                        "+91" + cleanMobile,
                        "sms"
                ).create();

                System.out.println("✅ REAL OTP SENT TO " + cleanMobile);

                return "TWILIO_OTP_SENT";

            } catch (Exception e) {

                System.err.println("❌ Twilio SMS failed for " + cleanMobile);
                e.printStackTrace();
                return "FAILED";
            }
        }

        // ==================================================
        // LOCAL / DEMO OTP — for all other numbers
        // ==================================================
        System.out.println(">>> ENTERED DEMO BLOCK — generating local OTP");

        String otp = String.format("%06d", random.nextInt(1000000));

        OtpData data = new OtpData(otp, LocalDateTime.now().plusMinutes(5));

        otpStorage.put(cleanMobile, data);

        System.out.println("🧪 DEMO OTP for [" + cleanMobile + "] = " + otp);

        return otp;
    }

    // ─────────────────────────────────────────────
    // VERIFY OTP
    // ─────────────────────────────────────────────
    public boolean verifyOtp(String mobileNumber, String inputOtp) {

        String cleanMobile = sanitizeMobile(mobileNumber);

        System.out.println("VERIFYING OTP FOR CLEAN MOBILE = [" + cleanMobile + "]");
        System.out.println("MATCH WITH REAL NUMBER         = " + cleanMobile.equals(REAL_SMS_NUMBER));

        // ==================================================
        // VERIFY USING TWILIO
        // ==================================================
        if (cleanMobile.equals(REAL_SMS_NUMBER)) {

            System.out.println(">>> TWILIO VERIFY BLOCK");

            try {

                Twilio.init(accountSid, authToken);

                VerificationCheck check =
                        VerificationCheck.creator(verifySid)
                                .setTo("+91" + cleanMobile)
                                .setCode(inputOtp.trim())
                                .create();

                boolean approved = "approved".equalsIgnoreCase(check.getStatus());

                System.out.println("✅ TWILIO VERIFY STATUS: " + check.getStatus());

                return approved;

            } catch (Exception e) {

                System.err.println("❌ Twilio verification failed");
                e.printStackTrace();
                return false;
            }
        }

        // ==================================================
        // VERIFY LOCAL OTP
        // ==================================================
        System.out.println(">>> LOCAL VERIFY BLOCK");

        OtpData data = otpStorage.get(cleanMobile);

        if (data == null) {
            System.out.println("❌ No OTP found in storage for [" + cleanMobile + "]");
            return false;
        }

        if (LocalDateTime.now().isAfter(data.expiry)) {
            otpStorage.remove(cleanMobile);
            System.out.println("❌ OTP expired for [" + cleanMobile + "]");
            return false;
        }

        boolean valid = data.otp.equals(inputOtp.trim());

        if (valid) {
            otpStorage.remove(cleanMobile);
            System.out.println("✅ LOCAL OTP VERIFIED FOR [" + cleanMobile + "]");
        } else {
            System.out.println("❌ OTP mismatch for [" + cleanMobile + "]");
        }

        return valid;
    }

    // ─────────────────────────────────────────────
    // SANITIZE MOBILE — strips ALL non-digit chars,
    // then removes leading country code / zero
    // ─────────────────────────────────────────────
    private String sanitizeMobile(String mobile) {

        // 1. Strip every character that is NOT a digit
        String clean = mobile.trim().replaceAll("[^0-9]", "");

        // 2. Remove Indian country code (91XXXXXXXXXX → XXXXXXXXXX)
        if (clean.startsWith("91") && clean.length() == 12) {
            clean = clean.substring(2);
        }

        // 3. Remove leading zero (0XXXXXXXXXX → XXXXXXXXXX)
        if (clean.startsWith("0") && clean.length() == 11) {
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

        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
