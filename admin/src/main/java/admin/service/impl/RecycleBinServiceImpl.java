package admin.service.impl;

import admin.common.biz.user.UserContext;
import admin.common.convention.exception.ServiceException;
import admin.common.convention.result.Result;
import admin.dao.entity.GroupDO;
import admin.dao.mapper.GroupMapper;
import admin.remote.dto.ShortLinkRemoteService;
import admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import admin.remote.dto.resp.ShortLinkPageRespDTO;
import admin.service.RecycleBinService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkRemoteService shortLinkRemoteService;

    private final GroupMapper groupMapper;

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        List<GroupDO> groupDOList = groupMapper.selectList(
                Wrappers.lambdaQuery(GroupDO.class)
                        .eq(GroupDO::getUsername, UserContext.getUsername()));
        if (CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        List<String> gidList = groupDOList.stream()
                .map(GroupDO::getGid)
                .toList();
        return shortLinkRemoteService.pageRecycleBinShortLink(gidList, requestParam.getCurrent(), requestParam.getSize());
    }
}
