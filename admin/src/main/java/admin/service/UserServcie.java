package admin.service;

import admin.dao.entity.UserDO;
import admin.dto.req.UserRegisterReqDTO;
import admin.dto.resp.UserRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserServcie extends IService<UserDO> {
    UserRespDTO getUserByUsername(String username);

    Boolean hasUsername(String username);

    void register(UserRegisterReqDTO userRegisterReqDTO);
}
