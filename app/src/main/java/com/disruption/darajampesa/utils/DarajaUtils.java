package com.disruption.darajampesa.utils;

import android.util.Base64;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DarajaUtils {
    public static String getTimestamp() {
        return new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
    }

    public static String sanitizePhoneNumber(String phone) {
        if (phone.equals("")) {
            return "";
        }
        if (phone.length() < 11 & phone.startsWith("0")) {
            return phone.replaceFirst("^0", "254");
        }
        if (phone.length() == 13 && phone.startsWith("+")) {
            return phone.replaceFirst("^+", "");
        }
        return phone;
    }

    public static String getBase64Password(String businessShortCode, String passkey, String timestamp) {
        String str = businessShortCode + passkey + timestamp;
        //encode the password to Base64
        return Base64.encodeToString(str.getBytes(), Base64.NO_WRAP);
    }
}
