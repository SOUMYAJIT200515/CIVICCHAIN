package com.example.civicpulse.service;

import com.twilio.Twilio;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
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

    // YOUR REAL TEST NUMBER
    private static final String REAL_SMS_NUMBER =
            "6290148614";

    // ─────────────────────────────────────────────
    // GENERATE + SEND OTP
    // ─────────────────────────────────────────────
    public String generateAndSendOtp(String mobileNumber) {

        String cleanMobile =
                sanitizeMobile(mobileNumber);

        // ==================================================
        // REAL TWILIO OTP
        // ==================================================
        if (cleanMobile.equals(REAL_SMS_NUMBER)) {

            try {

                Twilio.init(
                        accountSid,
                        authToken
                );

                Verification.creator(
                        verifySid,
                        "+91" + cleanMobile,
                        "sms"
                ).create();

                System.out.println(
                        "✅ REAL OTP SENT TO "
                                + cleanMobile
                );

                return "TWILIO_OTP_SENT";

            } catch (Exception e) {

                System.err.println(
                        "❌ Twilio SMS failed"
                );

                e.printStackTrace();

                return "FAILED";
            }
        }

        // ==================================================
        // LOCAL BACKEND OTP
        // ==================================================
        String otp = String.format(
                "%06d",
                random.nextInt(1000000)
        );

        OtpData data = new OtpData(
                otp,
                LocalDateTime.now().plusMinutes(5)
        );

        otpStorage.put(cleanMobile, data);

        System.out.println(
                "🧪 DEMO OTP for "
                        + cleanMobile
                        + " = "
                        + otp
        );

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

        // ==================================================
        // VERIFY USING TWILIO
        // ==================================================
        if (cleanMobile.equals(REAL_SMS_NUMBER)) {

            try {

                Twilio.init(
                        accountSid,
                        authToken
                );

                VerificationCheck check =
                        VerificationCheck.creator(verifySid)
                                .setTo("+91" + cleanMobile)
                                .setCode(inputOtp.trim())
                                .create();

                boolean approved =
                        "approved".equalsIgnoreCase(
                                check.getStatus()
                        );

                System.out.println(
                        "✅ TWILIO VERIFY STATUS: "
                                + check.getStatus()
                );

                return approved;

            } catch (Exception e) {

                System.err.println(
                        "❌ Twilio verification failed"
                );

                e.printStackTrace();

                return false;
            }
        }

        // ==================================================
        // VERIFY LOCAL OTP
        // ==================================================
        OtpData data =
                otpStorage.get(cleanMobile);

        if (data == null) {
            return false;
        }

        if (LocalDateTime.now().isAfter(data.expiry)) {

            otpStorage.remove(cleanMobile);

            return false;
        }

        boolean valid =
                data.otp.equals(inputOtp.trim());

        if (valid) {

            otpStorage.remove(cleanMobile);

            System.out.println(
                    "✅ LOCAL OTP VERIFIED FOR "
                            + cleanMobile
            );
        }

        return valid;
    }

    // ─────────────────────────────────────────────
    // SANITIZE MOBILE
    // ─────────────────────────────────────────────
    private String sanitizeMobile(String mobile) {

        String clean =
                mobile.trim()
                        .replaceAll("\\s+", "");

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
