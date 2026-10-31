package admin.service.impl;

import admin.common.biz.user.UserContext;
import admin.common.convention.exception.ClientException;
import admin.common.enums.GroupErrorCodeEnum;
import admin.dao.entity.GroupDO;
import admin.dao.mapper.GroupMapper;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import admin.dto.resp.ShortLinkGroupRespDTO;
import admin.service.GroupService;
import admin.util.RandomCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    @Override
    public void save(ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO) {
        String username = UserContext.getUsername();
        if (username == null){
            throw new ClientException(GroupErrorCodeEnum.GROUP_USER_NAME_NULL);
        }
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getName, shortLinkGroupSaveReqDTO.getName())
                .eq(GroupDO::getUsername, username);
        GroupDO groupDO = baseMapper.selectOne(queryWrapper);
        if (groupDO != null) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_NAME_EXIST);
        }

        String gid;
        do {
            gid = RandomCodeUtil.generateRandomCode();
        } while (hasGid(gid));


        int inserted = baseMapper.insert(GroupDO.builder()
                .gid(gid)
                .name(shortLinkGroupSaveReqDTO.getName())
                .username(username)
                .sortOrder(0)
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

    @Override
    public List<ShortLinkGroupRespDTO> listGroup() {
        String username = UserContext.getUsername();
        if (username == null) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_USER_NAME_NULL);
        }
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, username)
                .orderByAsc(GroupDO::getSortOrder)
                .orderByAsc(GroupDO::getCreateTime);
        List<GroupDO> list = baseMapper.selectList(queryWrapper);
        List<ShortLinkGroupRespDTO> resultList = new ArrayList<>(list.size());
        for (GroupDO groupDO : list) {
            ShortLinkGroupRespDTO dto = new ShortLinkGroupRespDTO();
            BeanUtils.copyProperties(groupDO, dto);
            resultList.add(dto);
        }
        return resultList;
    }

}
