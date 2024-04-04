package com.project.lvshi.controller.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysKnowledgeAssort;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysKnowledgeAssortService;
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
 * @description: 法律知识分类controller
 * @date 2024/02/27 04:09
 */
@Controller
@ResponseBody
@RequestMapping("assort")
public class SysKnowledgeAssortController {

    @Autowired
    private SysKnowledgeAssortService sysKnowledgeAssortService;

    /** 分页获取法律知识分类 */
    @PostMapping("getSysKnowledgeAssortPage")
    public Result getSysKnowledgeAssortPage(@RequestBody SysKnowledgeAssort sysKnowledgeAssort) {
        Page<SysKnowledgeAssort> page = new Page<>(sysKnowledgeAssort.getPageNumber(),sysKnowledgeAssort.getPageSize());
        QueryWrapper<SysKnowledgeAssort> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysKnowledgeAssort.getType()),SysKnowledgeAssort::getType,sysKnowledgeAssort.getType())
                .like(StringUtils.isNotBlank(sysKnowledgeAssort.getName()),SysKnowledgeAssort::getName,sysKnowledgeAssort.getName());
        Page<SysKnowledgeAssort> sysKnowledgeAssortPage = sysKnowledgeAssortService.page(page, queryWrapper);
        return Result.success(sysKnowledgeAssortPage);
    }

    @GetMapping("getSysKnowledgeAssortList")
    public Result getSysKnowledgeAssortList() {
        List<SysKnowledgeAssort> knowledgeAssortList = sysKnowledgeAssortService.list();
        return Result.success(knowledgeAssortList);
    }

    /** 根据id获取法律知识分类 */
    @GetMapping("getSysKnowledgeAssortById")
    public Result getSysKnowledgeAssortById(@RequestParam("id")String id) {
        SysKnowledgeAssort sysKnowledgeAssort = sysKnowledgeAssortService.getById(id);
        return Result.success(sysKnowledgeAssort);
    }

    /** 保存法律知识分类 */
    @PostMapping("saveSysKnowledgeAssort")
    public Result saveSysKnowledgeAssort(@RequestBody SysKnowledgeAssort sysKnowledgeAssort) {
        boolean save = sysKnowledgeAssortService.save(sysKnowledgeAssort);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑法律知识分类 */
    @PostMapping("editSysKnowledgeAssort")
    public Result editSysKnowledgeAssort(@RequestBody SysKnowledgeAssort sysKnowledgeAssort) {
        boolean save = sysKnowledgeAssortService.updateById(sysKnowledgeAssort);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除法律知识分类 */
    @GetMapping("removeSysKnowledgeAssort")
    public Result removeSysKnowledgeAssort(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysKnowledgeAssortService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("法律知识分类id不能为空！");
        }
    }

}