package admin.controller;

import admin.common.convention.result.Result;
import admin.common.convention.result.Results;
import admin.dto.req.ShortLinkGroupSaveReqDTO;
import admin.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GroupController {

    final GroupService groupService;

    /**
     * 新增分组名
     */
    @PostMapping("/api/short-link/v1/group")
    public Result<Void> save(@RequestBody ShortLinkGroupSaveReqDTO requestParam){
        groupService.save(requestParam);
        return Results.success();
    }
}
