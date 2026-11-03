package project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import project.dao.entity.ShortLinkDO;
import project.dto.req.ShortLinkCreateReqDTO;
import project.dto.req.ShortLinkPageReqDTO;
import project.dto.resp.ShortLinkCreateRespDTO;
import project.dto.resp.ShortLinkPageRespDTO;

public interface ShortLinkService extends IService<ShortLinkDO> {
    /**
     * 新增短链接
     *
     * @param shortLinkCreateReqDTO
     * @return 新增短链接返回参数
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO shortLinkCreateReqDTO);

    /**
     * 分页查询短链接
     * @param ShortLinkDO 短链接分页请求参数
     * @return 短链接分页返回参数
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO ShortLinkDO);


}
