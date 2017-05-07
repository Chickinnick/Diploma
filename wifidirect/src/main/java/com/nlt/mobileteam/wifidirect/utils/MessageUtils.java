package com.nlt.mobileteam.wifidirect.utils;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.nlt.mobileteam.wifidirect.controller.Message;

public class MessageUtils {
    private MessageUtils() {
    }

    public static byte[] prepareMessage(Message message, int value) {
        return prepareMessage(message, String.valueOf(value));
    }

    public static byte[] prepareMessage(Message message, @Nullable Long value) {
        return prepareMessage(message, String.valueOf(value));
    }

    public static byte[] prepareMessage(Message message) {
        return prepareMessage(message, "");
    }


    public static void prepareMessage(Message messageMediaFormat, byte[] bytes) {

    }

    public static byte[] prepareMessage(Message message, @Nullable String value) {
        StringBuilder sb = new StringBuilder();
        sb.append(message.ordinal());
        sb.append(";");
        if(!TextUtils.isEmpty(value)) {
            sb.append(value);
            sb.append(";");
        }
        return sb.toString().getBytes();
    }
}