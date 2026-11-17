package project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import project.common.convention.exception.ClientException;
import project.common.enums.ShortLinkErrorCodeEnum;
import project.dao.entity.LinkStatsTodayDO;
import project.dao.entity.ShortLinkDO;
import project.dao.mapper.LinkStatsTodayMapper;
import project.dao.mapper.ShortLinkMapper;
import project.dto.req.RecycleBinRecoverReqDTO;
import project.dto.req.RecycleBinRemoveReqDTO;
import project.dto.req.RecycleBinSaveReqDTO;
import project.dto.req.ShortLinkRecycleBinPageReqDTO;
import project.dto.resp.ShortLinkPageRespDTO;
import project.service.RecycleBinService;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    private final LinkStatsTodayMapper linkStatsTodayMapper;

    @Override
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 0)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(1)
                .build();
        int rows = baseMapper.update(shortLinkDO, updateWrapper);
        if (rows == 0) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_NULL);
        }
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    @Override
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .enableStatus(0)
                .build();
        int rows = baseMapper.update(shortLinkDO, updateWrapper);
        if (rows == 0) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_NULL);
        }
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, requestParam.getFullShortUrl()));
    }

    @Override
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getGid, requestParam.getGid())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelTime, 0L)
                .eq(ShortLinkDO::getDelFlag, 0);
        ShortLinkDO delShortLinkDO = ShortLinkDO.builder()
                .delTime(System.currentTimeMillis())
                .build();
        delShortLinkDO.setDelFlag(1);
        int rows = baseMapper.update(delShortLinkDO, updateWrapper);
        if (rows == 0) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_NULL);
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        if (CollUtil.isEmpty(requestParam.getGidList())) {
            return new Page<>(requestParam.getCurrent(), requestParam.getSize());
        }
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .in(ShortLinkDO::getGid, requestParam.getGidList())
                .eq(ShortLinkDO::getEnableStatus, 1)
                .eq(ShortLinkDO::getDelFlag, 0)
                .orderByDesc(ShortLinkDO::getUpdateTime);
        IPage<ShortLinkDO> resultPage = baseMapper.selectPage(requestParam, queryWrapper);
        IPage<ShortLinkPageRespDTO> pageResult = resultPage.convert(
                each -> BeanUtil.copyProperties(each, ShortLinkPageRespDTO.class));
        fillTodayStats(pageResult.getRecords());
        return pageResult;
    }

    private void fillTodayStats(List<ShortLinkPageRespDTO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        try {
            List<String> fullShortUrls = records.stream()
                    .map(ShortLinkPageRespDTO::getFullShortUrl)
                    .toList();
            List<LinkStatsTodayDO> todayStatsList = linkStatsTodayMapper.selectList(
                    Wrappers.lambdaQuery(LinkStatsTodayDO.class)
                            .in(LinkStatsTodayDO::getFullShortUrl, fullShortUrls)
                            .eq(LinkStatsTodayDO::getDate, DateUtil.beginOfDay(new Date())));
            Map<String, LinkStatsTodayDO> todayStatsMap = todayStatsList.stream()
                    .collect(Collectors.toMap(LinkStatsTodayDO::getFullShortUrl, Function.identity(), (a, b) -> a));
            records.forEach(each -> {
                LinkStatsTodayDO todayStats = todayStatsMap.get(each.getFullShortUrl());
                each.setTodayPv(todayStats == null || todayStats.getTodayPv() == null ? 0 : todayStats.getTodayPv());
                each.setTodayUv(todayStats == null || todayStats.getTodayUv() == null ? 0 : todayStats.getTodayUv());
                each.setTodayUip(todayStats == null || todayStats.getTodayUip() == null ? 0 : todayStats.getTodayUip());
            });
        } catch (Exception ex) {
            log.warn("回收站分页填充今日统计数据失败", ex);
        }
    }
}
