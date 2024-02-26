package com.fooddiary.api.common.util;

import java.security.SecureRandom;

public final class Random {
    public static String RandomString(int length) {
    String SALTCHARS = "1234567890";
    StringBuilder salt = new StringBuilder();
    SecureRandom rnd = new SecureRandom();
    while (salt.length() < length) { // length of the random string.
        int index = (int) (rnd.nextFloat() * SALTCHARS.length());
        salt.append(SALTCHARS.charAt(index));
    }
    return salt.toString();
    }
}
