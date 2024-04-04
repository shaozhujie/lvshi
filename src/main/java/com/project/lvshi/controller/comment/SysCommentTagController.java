package com.project.lvshi.controller.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysCommentTag;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysCommentTagService;
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
 * @description: 评价标签controller
 * @date 2024/02/29 10:33
 */
@Controller
@ResponseBody
@RequestMapping("tag")
public class SysCommentTagController {

    @Autowired
    private SysCommentTagService sysCommentTagService;

    /** 分页获取评价标签 */
    @PostMapping("getSysCommentTagPage")
    public Result getSysCommentTagPage(@RequestBody SysCommentTag sysCommentTag) {
        Page<SysCommentTag> page = new Page<>(sysCommentTag.getPageNumber(),sysCommentTag.getPageSize());
        QueryWrapper<SysCommentTag> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysCommentTag.getName()),SysCommentTag::getName,sysCommentTag.getName());
        Page<SysCommentTag> sysCommentTagPage = sysCommentTagService.page(page, queryWrapper);
        return Result.success(sysCommentTagPage);
    }

    @GetMapping("getSysCommentTagList")
    public Result getSysCommentTagList() {
        List<SysCommentTag> tagList = sysCommentTagService.list();
        return Result.success(tagList);
    }

    /** 根据id获取评价标签 */
    @GetMapping("getSysCommentTagById")
    public Result getSysCommentTagById(@RequestParam("id")String id) {
        SysCommentTag sysCommentTag = sysCommentTagService.getById(id);
        return Result.success(sysCommentTag);
    }

    /** 保存评价标签 */
    @PostMapping("saveSysCommentTag")
    public Result saveSysCommentTag(@RequestBody SysCommentTag sysCommentTag) {
        boolean save = sysCommentTagService.save(sysCommentTag);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑评价标签 */
    @PostMapping("editSysCommentTag")
    public Result editSysCommentTag(@RequestBody SysCommentTag sysCommentTag) {
        boolean save = sysCommentTagService.updateById(sysCommentTag);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除评价标签 */
    @GetMapping("removeSysCommentTag")
    public Result removeSysCommentTag(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysCommentTagService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("评价标签id不能为空！");
        }
    }

}