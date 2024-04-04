package com.project.lvshi.controller.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysKnowledgeAssort;
import com.project.lvshi.domain.SysKnowledgeType;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysKnowledgeAssortService;
import com.project.lvshi.service.SysKnowledgeTypeService;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.model.KotlinDefaultMask;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 知识类型controller
 * @date 2024/02/27 03:59
 */
@Controller
@ResponseBody
@RequestMapping("type")
public class SysKnowledgeTypeController {

    @Autowired
    private SysKnowledgeTypeService sysKnowledgeTypeService;
    @Autowired
    private SysKnowledgeAssortService sysKnowledgeAssortService;

    /** 分页获取知识类型 */
    @PostMapping("getSysKnowledgeTypePage")
    public Result getSysKnowledgeTypePage(@RequestBody SysKnowledgeType sysKnowledgeType) {
        Page<SysKnowledgeType> page = new Page<>(sysKnowledgeType.getPageNumber(),sysKnowledgeType.getPageSize());
        QueryWrapper<SysKnowledgeType> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysKnowledgeType.getName()),SysKnowledgeType::getName,sysKnowledgeType.getName());
        Page<SysKnowledgeType> sysKnowledgeTypePage = sysKnowledgeTypeService.page(page, queryWrapper);
        return Result.success(sysKnowledgeTypePage);
    }

    @GetMapping("getSysKnowledgeTypeList")
    public Result getSysKnowledgeTypeList() {
        List<SysKnowledgeType> knowledgeTypeList = sysKnowledgeTypeService.list();
        for (SysKnowledgeType sysKnowledgeType : knowledgeTypeList) {
            QueryWrapper<SysKnowledgeAssort> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(SysKnowledgeAssort::getType,sysKnowledgeType.getName());
            List<SysKnowledgeAssort> assortList = sysKnowledgeAssortService.list(queryWrapper);
            sysKnowledgeType.setSysKnowledgeAssorts(assortList);
        }
        return Result.success(knowledgeTypeList);
    }

    /** 根据id获取知识类型 */
    @GetMapping("getSysKnowledgeTypeById")
    public Result getSysKnowledgeTypeById(@RequestParam("id")String id) {
        SysKnowledgeType sysKnowledgeType = sysKnowledgeTypeService.getById(id);
        return Result.success(sysKnowledgeType);
    }

    /** 保存知识类型 */
    @PostMapping("saveSysKnowledgeType")
    public Result saveSysKnowledgeType(@RequestBody SysKnowledgeType sysKnowledgeType) {
        boolean save = sysKnowledgeTypeService.save(sysKnowledgeType);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑知识类型 */
    @PostMapping("editSysKnowledgeType")
    public Result editSysKnowledgeType(@RequestBody SysKnowledgeType sysKnowledgeType) {
        boolean save = sysKnowledgeTypeService.updateById(sysKnowledgeType);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除知识类型 */
    @GetMapping("removeSysKnowledgeType")
    public Result removeSysKnowledgeType(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysKnowledgeTypeService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("知识类型id不能为空！");
        }
    }

}