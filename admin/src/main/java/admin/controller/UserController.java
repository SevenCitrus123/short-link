package admin.controller;

import admin.common.convention.exception.ClientException;
import admin.common.convention.result.Result;
import admin.common.convention.result.Results;
import admin.common.enums.UserErrorCodeEnum;
import admin.dto.req.UserRegisterReqDTO;
import admin.dto.resp.UserActualRespDTO;
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
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        UserRespDTO result = userServcie.getUserByUsername(username);
        return Results.success(result);
    }

    /**
     * 根据用户名查询真实参数
     *
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/v1/actual/user/{username}")
    public Result<UserActualRespDTO> getActualUserByUsername(@PathVariable String username) {
        return Results.success(BeanUtil.toBean(userServcie.getUserByUsername(username), UserActualRespDTO.class));
    }

    /**
     * 查询用户名是否存在
     *
     * @param username
     * @return
     */
    @GetMapping("/api/short-link/v1/user/has-username")
    public Result<Boolean> hasUsername(@RequestParam("username") String username) {
        Boolean result = userServcie.hasUsername(username);
        if (result == true) {
            throw new ClientException(UserErrorCodeEnum.USER_NAME_EXIST);
        }
        return Results.success(true);
    }

    @PostMapping("/api/short-link/v1/user")
    public Result<Void> register(@RequestBody UserRegisterReqDTO userRegisterReqDTO) {
        userServcie.register(userRegisterReqDTO);
        return Results.success();
    }
}
