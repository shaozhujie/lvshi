package com.project.lvshi.controller.knowledge;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysKnowledges;
import com.project.lvshi.domain.SysKnowledgesFavor;
import com.project.lvshi.domain.User;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysKnowledgesFavorService;
import com.project.lvshi.service.SysKnowledgesService;
import com.project.lvshi.service.UserService;
import com.project.lvshi.utils.TokenUtils;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 法律知识收藏controller
 * @date 2024/02/28 09:00
 */
@Controller
@ResponseBody
@RequestMapping("favor")
public class SysKnowledgesFavorController {

    @Autowired
    private SysKnowledgesFavorService sysKnowledgesFavorService;
    @Autowired
    private UserService userService;
    @Autowired
    private SysKnowledgesService sysKnowledgesService;

    /** 分页获取法律知识收藏 */
    @PostMapping("getSysKnowledgesFavorPage")
    public Result getSysKnowledgesFavorPage(@RequestBody SysKnowledgesFavor sysKnowledgesFavor) {
        Page<SysKnowledgesFavor> page = new Page<>(sysKnowledgesFavor.getPageNumber(),sysKnowledgesFavor.getPageSize());
        QueryWrapper<SysKnowledgesFavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(sysKnowledgesFavor.getUserId()),SysKnowledgesFavor::getUserId,sysKnowledgesFavor.getUserId())
                .eq(StringUtils.isNotBlank(sysKnowledgesFavor.getKnowledgesId()),SysKnowledgesFavor::getKnowledgesId,sysKnowledgesFavor.getKnowledgesId())
                .eq(StringUtils.isNotBlank(sysKnowledgesFavor.getTitle()),SysKnowledgesFavor::getTitle,sysKnowledgesFavor.getTitle())
                .eq(StringUtils.isNotBlank(sysKnowledgesFavor.getCreateBy()),SysKnowledgesFavor::getCreateBy,sysKnowledgesFavor.getCreateBy())
                .eq(sysKnowledgesFavor.getCreateTime() != null,SysKnowledgesFavor::getCreateTime,sysKnowledgesFavor.getCreateTime())
                .eq(StringUtils.isNotBlank(sysKnowledgesFavor.getUpdateBy()),SysKnowledgesFavor::getUpdateBy,sysKnowledgesFavor.getUpdateBy())
                .eq(sysKnowledgesFavor.getUpdateTime() != null,SysKnowledgesFavor::getUpdateTime,sysKnowledgesFavor.getUpdateTime());
        Page<SysKnowledgesFavor> sysKnowledgesFavorPage = sysKnowledgesFavorService.page(page, queryWrapper);
        return Result.success(sysKnowledgesFavorPage);
    }

    /** 根据id获取法律知识收藏 */
    @GetMapping("getSysKnowledgesFavorById")
    public Result getSysKnowledgesFavorById(@RequestParam("knowledgesId")String knowledgesId) {
        String id = TokenUtils.getUserIdByToken();
        QueryWrapper<SysKnowledgesFavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysKnowledgesFavor::getUserId,id).eq(SysKnowledgesFavor::getKnowledgesId,knowledgesId);
        int count = sysKnowledgesFavorService.count(queryWrapper);
        return Result.success(count);
    }

    /** 保存法律知识收藏 */
    @PostMapping("saveSysKnowledgesFavor")
    public Result saveSysKnowledgesFavor(@RequestBody SysKnowledgesFavor sysKnowledgesFavor) {
        String id = TokenUtils.getUserIdByToken();
        User user = userService.getById(id);
        sysKnowledgesFavor.setUserId(user.getId());
        SysKnowledges knowledges = sysKnowledgesService.getById(sysKnowledgesFavor.getKnowledgesId());
        sysKnowledgesFavor.setTitle(knowledges.getTitle());
        boolean save = sysKnowledgesFavorService.save(sysKnowledgesFavor);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除法律知识收藏 */
    @GetMapping("removeSysKnowledgesFavor")
    public Result removeSysKnowledgesFavor(@RequestParam("id")String id) {
        String idByToken = TokenUtils.getUserIdByToken();
        QueryWrapper<SysKnowledgesFavor> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysKnowledgesFavor::getUserId,idByToken)
                .eq(SysKnowledgesFavor::getKnowledgesId,id);
        boolean remove = sysKnowledgesFavorService.remove(queryWrapper);
        if (remove) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

}
