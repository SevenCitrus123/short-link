package admin.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机短码/字符串生成工具类
 */
public final class RandomCodeUtil {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final int DEFAULT_LENGTH = 6;

    private RandomCodeUtil() {}

    /**
     * 生成默认 6 位的随机字符串（包含大小写字母和数字）
     *
     * @return 6位随机字符串（例如 "aX9kB3"）
     */
    public static String generateRandomCode() {
        return generateRandomCode(DEFAULT_LENGTH);
    }

    /**
     * 生成指定长度的随机字符串（包含大小写字母和数字）
     *
     * @param length 指定长度
     * @return 随机字符串
     */
    public static String generateRandomCode(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("生成字符串的长度必须大于 0");
        }

        char[] result = new char[length];
        int poolLength = CHAR_POOL.length();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(poolLength);
            result[i] = CHAR_POOL.charAt(randomIndex);
        }

        return new String(result);
    }
}
