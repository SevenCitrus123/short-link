package admin.common.biz.user;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Optional;

/**
 * 用户上下文
 *
 * 请求进来时，UserTransmitFilter 从请求头读 username、userId、realName，只有 username 非空才处理；
 * 它把这些字段装进 UserInfoDTO，再通过 UserContext.setUser() 存进当前线程的 threadLocals（ThreadLocalMap）
 * ，key 是静态的 USER_THREAD_LOCAL，value 是 UserInfoDTO。
 * 这样同一次请求的任意代码（Controller / Service / Dao）都能直接 UserContext.getUsername() 拿到身份，不用层层传参。
 * ThreadLocal 本身只是那把“钥匙”，不存数据；数据存在 Thread 对象的 threadLocals 里。
 * 因为线程池会复用线程、线程对象不会销毁，map 里的数据也不会自己消失，所以必须在 finally 里 removeUser() 把这条 Entry 删掉。
 * 否则下一个请求复用同一线程时会读到上一个用户的身份（串号），value 也会因强引用而无法回收（内存泄漏）。
 */
public final class UserContext {

    /**
     * <a href="https://github.com/alibaba/transmittable-thread-local" />
     */
    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置用户至上下文
     *
     * @param user 用户详情信息
     */
    public static void setUser(UserInfoDTO user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取上下文中用户 ID
     *
     * @return 用户 ID
     */
    public static String getUserId() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUserId).orElse(null);
    }

    /**
     * 获取上下文中用户名称
     *
     * @return 用户名称
     */
    public static String getUsername() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getUsername).orElse(null);
    }

    /**
     * 获取上下文中用户真实姓名
     *
     * @return 用户真实姓名
     */
    public static String getRealName() {
        UserInfoDTO userInfoDTO = USER_THREAD_LOCAL.get();
        return Optional.ofNullable(userInfoDTO).map(UserInfoDTO::getRealName).orElse(null);
    }

    /**
     * 清理用户上下文
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}