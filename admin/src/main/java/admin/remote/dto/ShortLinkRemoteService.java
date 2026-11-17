package admin.remote.dto;

import admin.common.convention.result.Result;
import admin.dto.req.RecycleBinRecoverReqDTO;
import admin.dto.req.RecycleBinRemoveReqDTO;
import admin.dto.req.RecycleBinSaveReqDTO;
import admin.dto.resp.ShortLinkGroupRespDTO;
import admin.remote.dto.req.ShortLinkCreateReqDTO;
import admin.remote.dto.req.ShortLinkStatsReqDTO;
import admin.remote.dto.resp.ShortLinkGroupCountQueryRespDTO;
import admin.remote.dto.req.ShortLinkPageReqDTO;
import admin.remote.dto.resp.ShortLinkCreateRespDTO;
import admin.remote.dto.resp.ShortLinkPageRespDTO;
import admin.remote.dto.resp.ShortLinkStatsRespDTO;
import admin.service.GroupService;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShortLinkRemoteService {


    /**
     * 新增短链接
     */
    public Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String url = "http://127.0.0.1:8001/api/short-link/admin/v1/create";
        String body = JSON.toJSONString(requestParam);
        String resultStr = HttpRequest.post(url)
                .body(body)
                .contentType("application/json")
                .execute()
                .body();
        return JSON.parseObject(resultStr, new TypeReference<Result<ShortLinkCreateRespDTO>>() {
        });
    }


    /**
     * 分页查询短链接
     */
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultStr = HttpUtil.get(
                "http://127.0.0.1:8001/api/short-link/admin/v1/page", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>() {
        });
    }


    /**
     * 分页查询回收站短链接
     */
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBinShortLink(List<String> gidList, long current, long size) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", gidList);
        requestMap.put("current", current);
        requestMap.put("size", size);
        String resultStr = HttpUtil.get(
                "http://127.0.0.1:8001/api/short-link/admin/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<Result<IPage<ShortLinkPageRespDTO>>>() {
        });
    }


    /**
     * 查询用户拥有的短链接
     */
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> gids) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", gids);
        String resultStr = HttpUtil.get(
                "http://127.0.0.1:8001/api/short-link/admin/v1/count", requestMap);
        return JSON.parseObject(resultStr,
                new TypeReference<Result<List<ShortLinkGroupCountQueryRespDTO>>>() {
                });
    }

    /**
     * 获取目标网站标题功能
     */
    public Result<String> getTitleByUrl(String url) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("url", url);
        String resultStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<Result<String>>() {
        });
    }

    /**
     * 查询短链接访问统计（IP / 地区 / 操作系统 / 浏览器）
     */
    public Result<ShortLinkStatsRespDTO> getShortLinkStats(ShortLinkStatsReqDTO requestParam) {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("fullShortUrl", requestParam.getFullShortUrl());
        requestMap.put("startDate", requestParam.getStartDate());
        requestMap.put("endDate", requestParam.getEndDate());
        String resultStr = HttpUtil.get(
                "http://127.0.0.1:8001/api/short-link/admin/v1/stats", requestMap);
        return JSON.parseObject(resultStr, new TypeReference<Result<ShortLinkStatsRespDTO>>() {
        });
    }

    /**
     * 将短链接添加至回收站
     */
    public void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        String url = "http://127.0.0.1:8001/api/short-link/admin/v1/recycle-bin/save";
        HttpRequest.post(url)
                .body(JSON.toJSONString(requestParam))
                .contentType("application/json")
                .execute();
    }

    /**
     * 从回收站恢复短链接
     */
    public void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        String url = "http://127.0.0.1:8001/api/short-link/admin/v1/recycle-bin/recover";
        HttpRequest.post(url)
                .body(JSON.toJSONString(requestParam))
                .contentType("application/json")
                .execute();
    }

    /**
     * 从回收站彻底移除短链接
     */
    public void removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        String url = "http://127.0.0.1:8001/api/short-link/admin/v1/recycle-bin/remove";
        HttpRequest.post(url)
                .body(JSON.toJSONString(requestParam))
                .contentType("application/json")
                .execute();
    }
}
