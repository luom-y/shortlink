package com.shortlink.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("URL validator")
class UrlValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://www.example.com",
            "http://example.com/path?q=1",
            "https://sub.domain.co.uk/page#anchor"
    })
    @DisplayName("valid URLs return true")
    void validUrls(String url) {
        assertTrue(UrlValidator.isValid(url));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "not-a-url", "ftp://example.com", "://missing-scheme"})
    @DisplayName("invalid URLs return false")
    void invalidUrls(String url) {
        assertFalse(UrlValidator.isValid(url));
    }

    @Test
    @DisplayName("normalize adds https scheme")
    void normalizeAddsScheme() {
        assertEquals("https://example.com", UrlValidator.normalize("example.com"));
    }

    @Test
    @DisplayName("normalize keeps existing scheme")
    void normalizeKeepsScheme() {
        assertEquals("http://example.com", UrlValidator.normalize("http://example.com"));
    }
}
