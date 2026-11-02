package project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import project.dao.entity.ShortLinkDO;
import project.dao.mapper.ShortLinkMapper;
import project.dto.resp.ShortLinkCreateRespDTO;
import project.service.ShortLinkService;
import project.util.HashUtil;

public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper,ShortLinkDO > implements ShortLinkService {
    @Override
    public ShortLinkCreateRespDTO createShortLink() {
        return null;
    }
}
