package admin.service.impl;

import admin.common.biz.user.UserContext;
import admin.common.convention.exception.ClientException;
import admin.common.enums.GroupErrorCodeEnum;
import admin.dao.entity.GroupDO;
import admin.dao.mapper.GroupMapper;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import admin.dto.req.ShortLinkGroupSortReqDTO;
import admin.dto.req.ShortLinkGroupUpdateReqDTO;
import admin.dto.resp.ShortLinkGroupRespDTO;
import admin.service.GroupService;
import admin.util.RandomCodeUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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

    @Override
    public void updateGroup(ShortLinkGroupUpdateReqDTO requestParam) {
        String username = UserContext.getUsername();
        if (username == null) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_USER_NAME_NULL);
        }
        LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                .eq(GroupDO::getUsername, username)
                .eq(GroupDO::getGid, requestParam.getGid())
                .eq(GroupDO::getDelFlag, 0);
        GroupDO groupDO = new GroupDO();
        groupDO.setName(requestParam.getName());
        int updated = baseMapper.update(groupDO, updateWrapper);
        if (updated < 1){
            throw new ClientException(GroupErrorCodeEnum.GROUP_NULL);
        }
    }

    @Override
    public void deleteGroup(String gid) {
        String username = UserContext.getUsername();
        if (username == null) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_USER_NAME_NULL);
        }
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, username)
                .eq(GroupDO::getGid, gid);
        int deleted = baseMapper.delete(queryWrapper);
        if (deleted < 1) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_NULL);
        }
    }

    @Override
    public void sortGroup(List<ShortLinkGroupSortReqDTO> requestParam) {
        requestParam.forEach(each -> {
            GroupDO groupDO = GroupDO.builder()
                    .sortOrder(each.getSortOrder())
                    .build();
            LambdaUpdateWrapper<GroupDO> updateWrapper = Wrappers.lambdaUpdate(GroupDO.class)
                    .eq(GroupDO::getUsername, UserContext.getUsername())
                    .eq(GroupDO::getGid, each.getGid())
                    .eq(GroupDO::getDelFlag, 0);
            baseMapper.update(groupDO, updateWrapper);
        });
    }

}
