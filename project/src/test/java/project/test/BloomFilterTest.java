package project.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BloomFilterTest {

    @Autowired
    private RBloomFilter<String> bloomFilter;

    @Test
    public void resetBloomFilter() {
        bloomFilter.delete();
        Assertions.assertTrue(bloomFilter.tryInit(100000000L, 0.001), "布隆过滤器初始化失败");
        Assertions.assertFalse(bloomFilter.contains("https://www.example.com/not-exists"));
    }
}
