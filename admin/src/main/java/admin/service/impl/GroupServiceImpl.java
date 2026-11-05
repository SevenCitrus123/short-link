package admin.service.impl;

import admin.common.biz.user.UserContext;
import admin.common.convention.exception.ClientException;
import admin.common.convention.result.Result;
import admin.common.enums.GroupErrorCodeEnum;
import admin.common.enums.UserErrorCodeEnum;
import admin.dao.entity.GroupDO;
import admin.dao.entity.UserDO;
import admin.dao.mapper.GroupMapper;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import admin.dto.req.ShortLinkGroupSortReqDTO;
import admin.dto.req.ShortLinkGroupUpdateReqDTO;
import admin.dto.resp.ShortLinkGroupRespDTO;
import admin.remote.dto.ShortLinkRemoteService;
import admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import admin.service.GroupService;
import admin.util.RandomCodeUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {

    private final ShortLinkRemoteService shortLinkRemoteService;

    @Override
    public void save(ShortLinkGroupSaveReqDTO shortLinkGroupSaveReqDTO) {
        String username = UserContext.getUsername();
        if (username == null) {
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
        return baseMapper.exists(queryWrapper);
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
        List<ShortLinkGroupRespDTO> resultList = BeanUtil.copyToList(list, ShortLinkGroupRespDTO.class);
        if (CollUtil.isEmpty(resultList)) {
            return resultList;
        }
        List<String> gids = resultList.stream().map(ShortLinkGroupRespDTO::getGid).toList();
        Result<List<ShortLinkGroupCountQueryRespDTO>> remoteResult =
                shortLinkRemoteService.listGroupShortLinkCount(gids);
        List<ShortLinkGroupCountQueryRespDTO> countList =
                remoteResult == null || remoteResult.getData() == null
                        ? Collections.emptyList()
                        : remoteResult.getData();
        Map<String, Integer> countMap = countList.stream().collect(
                Collectors.toMap(ShortLinkGroupCountQueryRespDTO::getGid,
                        ShortLinkGroupCountQueryRespDTO::getShortLinkCount,
                        (a, b) -> a));
        resultList.forEach(each -> each.setShortLinkCount(countMap.getOrDefault(each.getGid(), 0)));
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
        if (updated < 1) {
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


    @Override
    public void saveGroup(String username, String groupName) {
        String gid;
        do {
            gid = RandomCodeUtil.generateRandomCode();
        } while (hasGid(gid));

        int inserted = baseMapper.insert(GroupDO.builder()
                .gid(gid)
                .name(groupName)
                .username(username)
                .sortOrder(0)
                .build());
        if (inserted < 1) {
            throw new ClientException(GroupErrorCodeEnum.GROUP_SAVE_FAIL);
        }
    }

}
