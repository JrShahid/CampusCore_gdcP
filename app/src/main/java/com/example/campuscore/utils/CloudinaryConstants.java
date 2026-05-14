package com.example.campuscore.utils;

public final class CloudinaryConstants {
    public static final String CLOUD_NAME = "dpot51lpj";
    public static final String UNSIGNED_UPLOAD_PRESET = "campuscore_notes";
    public static final String RAW_UPLOAD_URL = "https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/raw/upload";
    public static final String MIME_TYPE_PDF = "application/pdf";
    public static final long MAX_PDF_SIZE_BYTES = 10L * 1024L * 1024L;

    private CloudinaryConstants() {
    }
}
