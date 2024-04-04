package com.project.lvshi.controller.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysKnowledges;
import com.project.lvshi.domain.SysKnowledgesFavor;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysKnowledgesFavorService;
import com.project.lvshi.service.SysKnowledgesService;
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
 * @description: 法律知识controller
 * @date 2024/02/27 04:54
 */
@Controller
@ResponseBody
@RequestMapping("knowledges")
public class SysKnowledgesController {

    @Autowired
    private SysKnowledgesService sysKnowledgesService;
    @Autowired
    private SysKnowledgesFavorService sysKnowledgesFavorService;

    /** 分页获取法律知识 */
    @PostMapping("getSysKnowledgesPage")
    public Result getSysKnowledgesPage(@RequestBody SysKnowledges sysKnowledges) {
        Page<SysKnowledges> page = new Page<>(sysKnowledges.getPageNumber(),sysKnowledges.getPageSize());
        QueryWrapper<SysKnowledges> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysKnowledges.getTitle()),SysKnowledges::getTitle,sysKnowledges.getTitle())
                .eq(StringUtils.isNotBlank(sysKnowledges.getType()),SysKnowledges::getType,sysKnowledges.getType())
                .eq(StringUtils.isNotBlank(sysKnowledges.getAssort()),SysKnowledges::getAssort,sysKnowledges.getAssort())
                .orderByDesc(SysKnowledges::getCreateTime);
        Page<SysKnowledges> sysKnowledgesPage = sysKnowledgesService.page(page, queryWrapper);
        return Result.success(sysKnowledgesPage);
    }

    /** 根据id获取法律知识 */
    @GetMapping("getSysKnowledgesById")
    public Result getSysKnowledgesById(@RequestParam("id")String id) {
        SysKnowledges sysKnowledges = sysKnowledgesService.getById(id);
        return Result.success(sysKnowledges);
    }

    /** 保存法律知识 */
    @PostMapping("saveSysKnowledges")
    public Result saveSysKnowledges(@RequestBody SysKnowledges sysKnowledges) {
        boolean save = sysKnowledgesService.save(sysKnowledges);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑法律知识 */
    @PostMapping("editSysKnowledges")
    public Result editSysKnowledges(@RequestBody SysKnowledges sysKnowledges) {
        boolean save = sysKnowledgesService.updateById(sysKnowledges);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除法律知识 */
    @GetMapping("removeSysKnowledges")
    @Transactional(rollbackFor = Exception.class)
    public Result removeSysKnowledges(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysKnowledgesService.removeById(id);
                QueryWrapper<SysKnowledgesFavor> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SysKnowledgesFavor::getKnowledgesId,id);
                sysKnowledgesFavorService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("法律知识id不能为空！");
        }
    }

    @GetMapping("getSysKnowledgesIndex")
    public Result getSysKnowledgesIndex() {
        QueryWrapper<SysKnowledges> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(SysKnowledges::getCreateTime).last("limit 6");
        List<SysKnowledges> list = sysKnowledgesService.list(queryWrapper);
        return Result.success(list);
    }

}