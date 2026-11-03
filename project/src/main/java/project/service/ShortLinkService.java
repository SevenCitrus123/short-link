package project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import project.dao.entity.ShortLinkDO;
import project.dto.req.ShortLinkCreateReqDTO;
import project.dto.resp.ShortLinkCreateRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 新增短链接
     * @param shortLinkCreateReqDTO
     * @return 新增短链接返回参数
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO);
}
