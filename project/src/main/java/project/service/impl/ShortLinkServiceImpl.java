package project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import project.common.convention.exception.ClientException;
import project.common.convention.exception.ServiceException;
import project.common.enums.ShortLinkErrorCodeEnum;
import project.dao.entity.ShortLinkDO;
import project.dao.mapper.ShortLinkMapper;
import project.dto.req.ShortLinkCreateReqDTO;
import project.dto.resp.ShortLinkCreateRespDTO;
import project.service.ShortLinkService;
import project.util.HashUtil;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    private static final int MAX_GENERATE_COUNT = 5;

    private final RBloomFilter<String> bloomFilter;

    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        isValid(requestParam);
        String suffix = generateSuffix(requestParam);
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(requestParam.getDomain())
                .shortUri(suffix)
                .fullShortUrl(buildFullShortUrl(requestParam.getDomain(), suffix))
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .enableStatus(0)
                .clickNum(0).totalPv(0).totalUv(0).totalUip(0).delFlag(0).delTime(0L)
                .build();
        int inserted;
        try {
            inserted = baseMapper.insert(shortLinkDO);
        } catch (DuplicateKeyException ex) {
            throw new ServiceException(ShortLinkErrorCodeEnum.SHORT_LINK_ALREADY_EXIST);
        }
        if (inserted < 1) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_OPERATION_FAIL);
        }
        return BeanUtil.copyProperties(shortLinkDO, ShortLinkCreateRespDTO.class);
    }

    private void isValid(ShortLinkCreateReqDTO requestParam) {
        if (requestParam == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (requestParam.getOriginUrl() == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_NULL);
        }
        if (requestParam.getDomain() == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_DOMAIN_NULL);
        }
        Integer validDateType = requestParam.getValidDateType();
        Date validDate = requestParam.getValidDate();
        if (validDateType == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (validDateType == 0 && validDate != null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (validDateType == 1 && (validDate == null || validDate.before(new Date()))) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (requestParam.getDescribe() == null) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_ARGS_INVALID);
        }
        if (requestParam.getDescribe().length() > 1024) {
            throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_DESCRIBE_TOO_LONG);
        }
    }

    public Boolean hasFullShortUrl(String fullShortUrl) {
        if (!bloomFilter.contains(fullShortUrl)) {
            return false;
        }
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getFullShortUrl, fullShortUrl);
        ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
        return shortLinkDO != null;
    }


    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        for (int i = 0; i < MAX_GENERATE_COUNT; i++) {
            String originUrlWithSalt = requestParam.getOriginUrl() + UUID.randomUUID();
            String shortUri = HashUtil.hashToBase62(originUrlWithSalt);
            String fullShortUrl = buildFullShortUrl(requestParam.getDomain(), shortUri);
            if (!hasFullShortUrl(fullShortUrl)) {
                bloomFilter.add(fullShortUrl);
                return shortUri;
            }
        }
        throw new ClientException(ShortLinkErrorCodeEnum.SHORT_LINK_TRYS_UP_TO_5);
    }

    private String buildFullShortUrl(String domain, String shortUri) {
        return "http://" + domain + "/" + shortUri;
    }
}
