package admin.controller;

import admin.common.convention.result.Result;
import admin.common.convention.result.Results;
import admin.common.enums.UserErrorCodeEnum;
import admin.dto.Resp.UserRespDTO;
import admin.service.UserServcie;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制层
 */
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserServcie userServcie;

    /**
     * 根据用户名查询参数
     *
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public Result<UserRespDTO> getUserByUsername(@PathVariable String username) {
        UserRespDTO result = userServcie.getUserByUsername(username);
        return Results.success(result);
    }
}
