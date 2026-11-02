package project.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

import java.nio.charset.StandardCharsets;

/**
 * 哈希工具类
 *
 * <p>面向短链接 SaaS 场景，核心职责是把「原始长链接」转换成「短链码」：
 * 先用 MurmurHash3（32 位）把任意长度的链接压缩成一个整数，再用 62 进制
 * （字母 + 数字）把它表示成 1~6 位的字符串。同一个链接每次算出来的短码是
 * 完全相同的（确定性），方便做缓存、去重和幂等。</p>
 *
 * <p><b>关于碰撞：</b>32 位哈希的空间只有约 21 亿，按生日悖论，链接量到
 * 7~8 万条时碰撞概率就接近 50%。因此调用方必须在使用前查一次 Redis / 数据库，
 * 确认短码没被占用；如果冲突，就给原始链接加随机盐后重新哈希，直到不冲突为止。
 * 这段「重试」逻辑属于业务层，不应该写在工具类里。</p>
 *
 * <p>本类所有方法都是静态、无状态的，可以直接调用。</p>
 */
public final class HashUtil {

    /**
     * 62 进制字符表。
     *
     * <p>顺序与 {@code admin.util.RandomCodeUtil} 的字符池保持一致，
     * 避免项目里同时存在两套编码表导致短码风格不统一。</p>
     */
    private static final char[] BASE62_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    /**
     * 进制数，固定 62
     */
    private static final int BASE62 = BASE62_CHARS.length;

    /**
     * 短链码的默认最小长度。32 位哈希最大约 21 亿，62^5 ≈ 9.2 亿，62^6 ≈ 568 亿，
     * 所以取 6 位可以把所有取值都覆盖住，短码看起来也整齐。
     */
    private static final int DEFAULT_MIN_LENGTH = 6;

    private HashUtil() {
    }

    // ==================== 短链码生成 ====================

    /**
     * 把原始链接哈希成 62 进制短链码（默认 6 位）
     *
     * <p>示例：{@code hashToBase62("https://www.example.com/a/very/long/url")}
     * 返回形如 {@code "A7fK2q"} 的字符串。</p>
     *
     * @param originalUrl 原始长链接
     * @return 62 进制短链码
     */
    public static String hashToBase62(String originalUrl) {
        return hashToBase62(originalUrl, DEFAULT_MIN_LENGTH);
    }

    /**
     * 把原始链接哈希成指定最小长度的 62 进制短链码
     *
     * <p>字符串长度不足 {@code minLength} 时，会在左侧补字符表的第 0 个字符
     * （相当于数值的「前导零」），保证所有短码长度一致。</p>
     *
     * @param originalUrl 原始长链接
     * @param minLength   短链码的最小长度，必须大于 0
     * @return 62 进制短链码
     * @throws IllegalArgumentException 链接为空或长度非法时抛出
     */
    public static String hashToBase62(String originalUrl, int minLength) {
        if (StrUtil.isBlank(originalUrl)) {
            throw new IllegalArgumentException("原始链接不能为空");
        }
        if (minLength <= 0) {
            throw new IllegalArgumentException("短链码的最小长度必须大于 0");
        }

        String code = toBase62(murmurHash32(originalUrl));
        if (code.length() >= minLength) {
            return code;
        }

        // 左侧补「零」，对齐长度
        StringBuilder builder = new StringBuilder(minLength);
        for (int i = code.length(); i < minLength; i++) {
            builder.append(BASE62_CHARS[0]);
        }
        return builder.append(code).toString();
    }

    /**
     * 计算 MurmurHash3 32 位哈希值（非负）
     *
     * <p>返回值被 {@code & Integer.MAX_VALUE} 处理过，保证是 0 ~ 2147483647 的正数。
     * 之所以不用 {@code Math.abs()}，是因为 {@code Math.abs(Integer.MIN_VALUE)}
     * 仍然是负数，用掩码可以规避这个坑。</p>
     *
     * @param text 待哈希的文本
     * @return 非负的 32 位哈希值
     */
    public static int murmurHash32(String text) {
        if (StrUtil.isBlank(text)) {
            throw new IllegalArgumentException("待哈希的内容不能为空");
        }
        // 本类与 Hutool 的工具类同名，无法 import，只能用全限定名调用
        int hash = cn.hutool.core.util.HashUtil.murmur32(text.getBytes(StandardCharsets.UTF_8));
        return hash & Integer.MAX_VALUE;
    }

    /**
     * 把非负整数转换成 62 进制字符串
     *
     * <p>可用于「自增 ID → 短码」的方案：{@code toBase62(123456789L)}
     * 会得到一个远短于原数字的字符串。</p>
     *
     * @param value 非负整数
     * @return 62 进制字符串
     * @throws IllegalArgumentException 传入负数时抛出
     */
    public static String toBase62(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("待转换的数值不能为负数");
        }
        if (value == 0) {
            return String.valueOf(BASE62_CHARS[0]);
        }

        StringBuilder builder = new StringBuilder(11);
        while (value > 0) {
            builder.append(BASE62_CHARS[(int) (value % BASE62)]);
            value /= BASE62;
        }
        // 上面是从低位往高位取的，需要反过来
        return builder.reverse().toString();
    }

    // ==================== 通用摘要 ====================

    /**
     * 计算 MD5 摘要（32 位小写十六进制）
     *
     * <p>适用场景：接口签名、缓存 key 生成、文件校验。
     * <b>不要直接用它存储密码</b> —— MD5 不加盐极易被彩虹表撞库，
     * 密码请使用加盐哈希或 BCrypt。</p>
     *
     * @param text 待摘要的文本，按 UTF-8 编码
     * @return 32 位小写十六进制字符串
     */
    public static String md5Hex(String text) {
        if (StrUtil.isBlank(text)) {
            throw new IllegalArgumentException("待摘要的内容不能为空");
        }
        return DigestUtil.md5Hex(text);
    }

    /**
     * 计算 SHA-256 摘要（64 位小写十六进制）
     *
     * <p>适用场景：需要比 MD5 更强抗碰撞性的签名或缓存 key。</p>
     *
     * @param text 待摘要的文本，按 UTF-8 编码
     * @return 64 位小写十六进制字符串
     */
    public static String sha256Hex(String text) {
        if (StrUtil.isBlank(text)) {
            throw new IllegalArgumentException("待摘要的内容不能为空");
        }
        return DigestUtil.sha256Hex(text);
    }
}
