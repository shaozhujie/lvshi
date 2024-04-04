package com.project.lvshi.controller.rotation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysRotation;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysRotationService;
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
 * @description: 轮播controller
 * @date 2024/02/28 09:55
 */
@Controller
@ResponseBody
@RequestMapping("rotation")
public class SysRotationController {

    @Autowired
    private SysRotationService sysRotationService;

    /** 分页获取轮播 */
    @PostMapping("getSysRotationPage")
    public Result getSysRotationPage(@RequestBody SysRotation sysRotation) {
        Page<SysRotation> page = new Page<>(sysRotation.getPageNumber(),sysRotation.getPageSize());
        QueryWrapper<SysRotation> queryWrapper = new QueryWrapper<>();
        Page<SysRotation> sysRotationPage = sysRotationService.page(page, queryWrapper);
        return Result.success(sysRotationPage);
    }

    @GetMapping("getSysRotationList")
    public Result getSysRotationList() {
        List<SysRotation> list = sysRotationService.list();
        return Result.success(list);
    }

    /** 根据id获取轮播 */
    @GetMapping("getSysRotationById")
    public Result getSysRotationById(@RequestParam("id")String id) {
        SysRotation sysRotation = sysRotationService.getById(id);
        return Result.success(sysRotation);
    }

    /** 保存轮播 */
    @PostMapping("saveSysRotation")
    public Result saveSysRotation(@RequestBody SysRotation sysRotation) {
        boolean save = sysRotationService.save(sysRotation);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑轮播 */
    @PostMapping("editSysRotation")
    public Result editSysRotation(@RequestBody SysRotation sysRotation) {
        boolean save = sysRotationService.updateById(sysRotation);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除轮播 */
    @GetMapping("removeSysRotation")
    public Result removeSysRotation(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysRotationService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("轮播id不能为空！");
        }
    }

}
