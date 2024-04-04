package com.project.lvshi.controller.seek;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysSeekItem;
import com.project.lvshi.domain.User;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysSeekItemService;
import com.project.lvshi.service.UserService;
import com.project.lvshi.utils.TokenUtils;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 咨询内容controller
 * @date 2024/02/27 09:37
 */
@Controller
@ResponseBody
@RequestMapping("item")
public class SysSeekItemController {

    @Autowired
    private SysSeekItemService sysSeekItemService;
    @Autowired
    private UserService userService;

    /** 获取咨询内容 */
    @PostMapping("getSysSeekItemList")
    public Result getSysSeekItemList(@RequestBody SysSeekItem sysSeekItem) {
        QueryWrapper<SysSeekItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(sysSeekItem.getSeekId()),SysSeekItem::getSeekId,sysSeekItem.getSeekId());
        List<SysSeekItem> itemList = sysSeekItemService.list(queryWrapper);
        return Result.success(itemList);
    }

    /** 根据id获取咨询内容 */
    @GetMapping("getSysSeekItemById")
    public Result getSysSeekItemById(@RequestParam("id")String id) {
        SysSeekItem sysSeekItem = sysSeekItemService.getById(id);
        return Result.success(sysSeekItem);
    }

    /** 保存咨询内容 */
    @PostMapping("saveSysSeekItem")
    public Result saveSysSeekItem(@RequestBody SysSeekItem sysSeekItem) {
        String id = TokenUtils.getUserIdByToken();
        User user = userService.getById(id);
        sysSeekItem.setUserId(id);
        sysSeekItem.setAvatar(user.getAvatar());
        boolean save = sysSeekItemService.save(sysSeekItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑咨询内容 */
    @PostMapping("editSysSeekItem")
    public Result editSysSeekItem(@RequestBody SysSeekItem sysSeekItem) {
        boolean save = sysSeekItemService.updateById(sysSeekItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除咨询内容 */
    @GetMapping("removeSysSeekItem")
    public Result removeSysSeekItem(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysSeekItemService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("咨询内容id不能为空！");
        }
    }

}
