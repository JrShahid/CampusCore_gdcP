package com.example.campuscoret;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public final class SessionQrHelper {
    private SessionQrHelper() {
    }

    public static String buildPayload(SessionRecord sessionRecord) {
        return sessionRecord.getSessionId()
                + "|" + sessionRecord.getClassName()
                + "|" + sessionRecord.getSubjectName()
                + "|" + sessionRecord.getStartedAtMillis();
    }

    public static SessionQrPayload parsePayload(String payload) {
        if (payload == null || payload.trim().isEmpty()) {
            return null;
        }

        String[] parts = payload.split("\\|");
        if (parts.length != 4) {
            return null;
        }

        try {
            long issuedAt = Long.parseLong(parts[3]);
            return new SessionQrPayload(parts[0], parts[1], parts[2], issuedAt);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static Bitmap generateBitmap(String payload, int size) throws WriterException {
        BitMatrix bitMatrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }
}
