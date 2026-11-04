package admin.remote.dto;

import admin.common.convention.result.Result;
import admin.common.convention.result.Results;
import admin.remote.dto.req.ShortLinkCreateReqDTO;
import admin.remote.dto.req.ShortLinkPageReqDTO;
import admin.remote.dto.resp.ShortLinkCreateRespDTO;
import admin.remote.dto.resp.ShortLinkPageRespDTO;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
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

}
