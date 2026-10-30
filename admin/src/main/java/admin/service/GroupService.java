package admin.service;

import admin.dao.entity.GroupDO;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface GroupService extends IService<GroupDO> {
    /**
     * 新增分组名
     * @param shortLinkGroupSaveReqDTO 新增分组名参数
     */
    void save(ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO);

    /**
     * 判断当前gid是否存在
     * @param gid 分组id
     * @return 布尔值
     */
    Boolean hasGid(String gid);
}
