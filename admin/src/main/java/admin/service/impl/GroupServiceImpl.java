package admin.service.impl;

import admin.common.convention.exception.ClientException;
import admin.common.enums.GroupErrorCodeEnum;
import admin.dao.entity.GroupDO;
import admin.dao.mapper.GroupMapper;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import admin.service.GroupService;
import admin.util.RandomCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void save(ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getName, shortLinkGroupSaveReqDTO.getName());
        GroupDO groupDO = baseMapper.selectOne(queryWrapper);
        if (groupDO != null) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_NAME_EXIST);
        }

        String gid;
        do {
            gid = RandomCodeUtil.generateRandomCode();
        } while (hasGid(gid));

        //TODO 未设置username
        int inserted = baseMapper.insert(GroupDO.builder()
                .gid(gid)
                .name(shortLinkGroupSaveReqDTO.getName())
                .build());
        if (inserted < 1) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_SAVE_FAIL);
        }
    }

    @Override
    public Boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid);
        GroupDO groupDO = baseMapper.selectOne(queryWrapper);
        return groupDO != null;
    }
}
