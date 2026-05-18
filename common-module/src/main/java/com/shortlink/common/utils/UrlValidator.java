package com.shortlink.common.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * URL校验及标准化工具。
 */
public final class UrlValidator {

    private UrlValidator() {}

    /**
     * 校验URL是否合法（仅允许http/https协议）。
     */
    public static boolean isValid(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return false;
            }
            return uri.getHost() != null && !uri.getHost().isBlank();
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * 标准化URL：去空格，无协议时默认加https://。
     */
    public static String normalize(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }
        return trimmed;
    }
}
