package admin.service;

import admin.dao.entity.GroupDO;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import admin.dto.req.ShortLinkGroupSortReqDTO;
import admin.dto.req.ShortLinkGroupUpdateReqDTO;
import admin.dto.resp.ShortLinkGroupRespDTO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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

    /**
     * 查询短链接分组
     * @return 短链接分组查询返回参数
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改分组名
     * @param requestParam 短链接分组修改请求参数
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除分组
     * @param gid 分组标识
     */
    void deleteGroup(String gid);

    /**
     * 排序分组
     * @param requestParam 排序分组参数
     */
    void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam);
}
