package com.cms.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class InputSanitizer {

    private static final Safelist PAGE_CONTENT_SAFELIST = Safelist.relaxed();

    private InputSanitizer() {
    }

    public static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static String normalizeWhitespaceToSingleSpaces(String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    /**
     * Sanitizes user-provided HTML content intended for rich text rendering.
     * Returns null if the input is null.
     */
    public static String sanitizePageHtml(String html) {
        if (html == null) {
            return null;
        }
        return Jsoup.clean(html, PAGE_CONTENT_SAFELIST);
    }
}
