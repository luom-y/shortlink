package com.shortlink.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Base62 codec")
class Base62CodecTest {

    @Test
    @DisplayName("encode 0 returns first alphabet char")
    void encodeZero() {
        assertEquals("0", Base62Codec.encode(0));
    }

    @Test
    @DisplayName("encode and decode are reversible")
    void encodeDecodeRoundTrip() {
        long[] cases = {1L, 62L, 3844L, 9999999L, Long.MAX_VALUE};
        for (long n : cases) {
            String encoded = Base62Codec.encode(n);
            long decoded = Base62Codec.decode(encoded);
            assertEquals(n, decoded, "Roundtrip failed for " + n + " -> " + encoded);
        }
    }

    @Test
    @DisplayName("encode negative throws")
    void encodeNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.encode(-1));
    }

    @Test
    @DisplayName("encode produces non-empty string")
    void encodeProducesNonEmpty() {
        for (long n = 0; n < 1000; n++) {
            assertNotNull(Base62Codec.encode(n));
            assertFalse(Base62Codec.encode(n).isEmpty());
        }
    }

    @Test
    @DisplayName("decode invalid char throws")
    void decodeInvalidCharThrows() {
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.decode("@#$!"));
    }

    @Test
    @DisplayName("encode snowflake-sized IDs")
    void encodeSnowflakeSizedIds() {
        long snowflakeId = 7250000000000000000L;
        String code = Base62Codec.encode(snowflakeId);
        assertTrue(code.length() <= 12, "short code should be 12 chars or fewer");
    }
}
