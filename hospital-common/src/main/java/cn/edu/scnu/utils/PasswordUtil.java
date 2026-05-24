package cn.edu.scnu.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    public static boolean matches(String raw, String encoded) {
        if (encoded == null) {
            return false;
        }
        if (encoded.startsWith("$2a$")) {
            return ENCODER.matches(raw, encoded);
        }
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().equalsIgnoreCase(encoded);
        } catch (java.security.NoSuchAlgorithmException e) {
            return false;
        }
    }
}
