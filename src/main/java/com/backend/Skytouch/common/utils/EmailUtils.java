package com.backend.Skytouch.common.utils;

public final class EmailUtils {

    private EmailUtils() {
    }

    public static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(atIndex);
    }
}
