package admin.controller;

import admin.common.convention.result.Result;
import admin.common.convention.result.Results;
import admin.dto.req.UserLoginReqDTO;
import admin.dto.req.UserRegisterReqDTO;
import admin.dto.req.UserUpdateReqDTO;
import admin.dto.resp.UserActualRespDTO;
import admin.dto.resp.UserLoginRespDTO;
import admin.dto.resp.UserRespDTO;
import admin.service.UserServcie;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserServcie userServcie;

    /**
     * 根据用户名查询处理过的参数
     */
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        UserRespDTO result = userServcie.getUserByUsername(username);
        return Results.success(result);
    }

    /**
     * 根据用户名查询真实参数
     */
    @GetMapping("/api/short-link/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable String username) {
        return Results.success(BeanUtil.toBean(userServcie.getUserByUsername(username), UserActualRespDTO.class));
    }

    /**
     * 查询用户名是否存在
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        return Results.success(userServcie.hasUsername(username));
    }

    /**
     * 新增用户
     */
    @PostMapping("/api/short-link/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO) {
        userServcie.register(userRegisterReqDTO);
        return Results.success();
    }

    /**
     * 根据用户名修改用户参数
     */
    @PutMapping("/api/short-link/v1/user")
    public Result<Void> update(@RequestBody UserUpdateReqDTO userUpdateReqDTO) {
        userServcie.update(userUpdateReqDTO);
        return Results.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/api/short-link/v1/user/login")
    public Result<UserLoginRespDTO> login(@RequestBody UserLoginReqDTO userLoginReqDTO) {
        UserLoginRespDTO userLoginRespDTO = userServcie.login(userLoginReqDTO);
        return Results.success(userLoginRespDTO);
    }

    /**
     * 检验用户是否登录
     */
    @GetMapping("/api/short-link/v1/user/check-login")
    public Result<Boolean> checkLogin(@RequestParam("username") String username, @RequestParam("token") String token) {
        return Results.success(userServcie.checkLogin(username, token));
    }

    /**
     * 用户退出登录
     */
    @DeleteMapping("/api/short-link/v1/user/logout")
    public Result<Void> logOut(@RequestParam("username") String username, @RequestParam("token") String token) {
        userServcie.logOut(username, token);
        return Results.success();
    }
}
