package com.automation.security;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "~!@#$%^&*-_+=.?:";

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateCompliantPassword() {
        StringBuilder sb = new StringBuilder(8);

        // Ensure 1 of each required category
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        sb.append(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        String allAllowed = UPPER + LOWER + DIGITS + SYMBOLS;
        for (int i = 4; i < 8; i++) {
            sb.append(allAllowed.charAt(RANDOM.nextInt(allAllowed.length())));
        }

        // Shuffle characters
        char[] array = sb.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }

        return new String(array);
    }
}
