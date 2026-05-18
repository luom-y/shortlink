package com.shortlink.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Snowflake ID generator")
class SnowflakeIdGeneratorTest {

    @Test
    @DisplayName("generates unique IDs")
    void generatesUniqueIds() {
        SnowflakeIdGenerator gen = SnowflakeIdGenerator.getInstance(1, 2);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 10_000; i++) {
            long id = gen.nextId();
            assertTrue(ids.add(id), "Duplicate ID: " + id);
        }
    }

    @Test
    @DisplayName("generates positive IDs")
    void generatesPositiveIds() {
        SnowflakeIdGenerator gen = SnowflakeIdGenerator.getInstance();
        for (int i = 0; i < 100; i++) {
            assertTrue(gen.nextId() > 0);
        }
    }

    @Test
    @DisplayName("IDs are monotonically increasing")
    void idsAreMonotonicallyIncreasing() {
        SnowflakeIdGenerator gen = SnowflakeIdGenerator.getInstance();
        long prev = gen.nextId();
        for (int i = 0; i < 1000; i++) {
            long next = gen.nextId();
            assertTrue(next > prev);
            prev = next;
        }
    }
}
