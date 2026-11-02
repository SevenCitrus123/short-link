package project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import project.common.convention.result.Result;
import project.common.convention.result.Results;
import project.dto.req.ShortLinkCreateReqDTO;
import project.dto.resp.ShortLinkCreateRespDTO;
import project.service.ShortLinkService;

@RestController
@RequiredArgsConstructor
public class ShortLinkController {
    private ShortLinkService shortLinkService;
    @PostMapping("/api/short-link/v1/group")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam){
        return Results.success(shortLinkService.createShortLink());
    }
}
