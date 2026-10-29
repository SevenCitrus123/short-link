package admin.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BloomFilterTest {

    @Autowired
    private RBloomFilter<String> userRegisterCachePenetrationBloomFilter;

    @Test
    public void resetBloomFilter() {
        userRegisterCachePenetrationBloomFilter.delete();
        userRegisterCachePenetrationBloomFilter.tryInit(100000000L, 0.001);
    }
}
