package admin.service;

import admin.dao.entity.UserDO;
import admin.dto.Resp.UserRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserServcie extends IService<UserDO> {
    public UserRespDTO getUserByUsername(String username);
}
