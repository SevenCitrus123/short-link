package admin.service;

import admin.dao.entity.UserDO;
import admin.dto.req.UserLoginReqDTO;
import admin.dto.req.UserRegisterReqDTO;
import admin.dto.req.UserUpdateReqDTO;
import admin.dto.resp.UserLoginRespDTO;
import admin.dto.resp.UserRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserServcie extends IService<UserDO> {
    /**
     * 根据用户名查询处理过的参数
     *
     * @param username 用户名
     * @return 用户返回参数
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     *
     * @param username 用户名
     * @return 布尔值
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     *
     * @param userRegisterReqDTO 用户注册请求参数
     */
    void register(UserRegisterReqDTO userRegisterReqDTO);

    /**
     * 用户数据修改
     *
     * @param userUpdateReqDTO 更新请求参数
     */
    void update(UserUpdateReqDTO userUpdateReqDTO);

    /**
     * 用户登录
     *
     * @param userLoginReqDTO 用户登录请求
     * @return 登录响应 Token
     */
    UserLoginRespDTO login(UserLoginReqDTO userLoginReqDTO);


    /**
     * 检验用户是否登录
     *
     * @param username 用户名
     * @param token    Token值
     * @return 布尔值
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     *
     * @param username 用户名
     */
    void logOut(String username, String token);
}
