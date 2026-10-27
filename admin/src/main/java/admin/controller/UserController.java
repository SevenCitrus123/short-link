package admin.controller;

import admin.dao.entity.UserDO;
import admin.dao.mapper.UserMapper;
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
     * @param username
     * @return
     */
    @GetMapping("/api/shortlink/v1/user/{username}")
    public UserRespDTO getUserByUsername(@PathVariable String username) {
        UserRespDTO userRespDTO = userServcie.getUserByUsername(username);
        if (userRespDTO == null){
            return new UserRespDTO();
        }
        return userRespDTO;
    }
}
