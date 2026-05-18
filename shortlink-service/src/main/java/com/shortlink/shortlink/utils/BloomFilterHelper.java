package com.shortlink.shortlink.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于Redis位图的布隆过滤器。
 * 使用Lua脚本保证添加和查询的原子性。
 * 误判率约1%，位图约12MB，支持约1000万条短链接。
 */
@Component
public class BloomFilterHelper {

    private static final String BLOOM_FILTER_KEY = "bloom:shortlink";

    private final StringRedisTemplate stringRedisTemplate;

    public BloomFilterHelper(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 将短码加入布隆过滤器。
     */
    public void add(String shortCode) {
        int[] offsets = hashOffsets(shortCode);
        String lua = """
                for _, offset in ipairs(ARGV) do
                    redis.call('SETBIT', KEYS[1], offset, 1)
                end
                return 1
                """;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(lua, Long.class);
        stringRedisTemplate.execute(script, List.of(BLOOM_FILTER_KEY),
                String.valueOf(offsets[0]), String.valueOf(offsets[1]), String.valueOf(offsets[2]));
    }

    /**
     * 查询短码是否可能存在于布隆过滤器。
     * @return true=可能存在, false=一定不存在
     */
    public boolean mightContain(String shortCode) {
        int[] offsets = hashOffsets(shortCode);
        String lua = """
                for _, offset in ipairs(ARGV) do
                    if redis.call('GETBIT', KEYS[1], offset) == 0 then
                        return 0
                    end
                end
                return 1
                """;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(lua, Long.class);
        Long result = stringRedisTemplate.execute(script, List.of(BLOOM_FILTER_KEY),
                String.valueOf(offsets[0]), String.valueOf(offsets[1]), String.valueOf(offsets[2]));
        return Long.valueOf(1).equals(result);
    }

    /** 三种哈希计算offset */
    private int[] hashOffsets(String value) {
        int hash1 = value.hashCode();
        int hash2 = hash1 >>> 16;
        int hash3 = hash1 ^ (hash2 * 31);
        int bitSize = 100_000_000;
        return new int[]{
                Math.abs(hash1 % bitSize),
                Math.abs(hash2 % bitSize),
                Math.abs(hash3 % bitSize)
        };
    }
}
