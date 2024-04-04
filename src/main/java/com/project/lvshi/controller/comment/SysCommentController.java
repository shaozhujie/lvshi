package com.project.lvshi.controller.comment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysComment;
import com.project.lvshi.domain.User;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysCommentService;
import com.project.lvshi.service.UserService;
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
 * @description: 律师评价controller
 * @date 2024/02/29 10:56
 */
@Controller
@ResponseBody
@RequestMapping("comment")
public class SysCommentController {

    @Autowired
    private SysCommentService sysCommentService;
    @Autowired
    private UserService userService;

    /** 分页获取律师评价 */
    @PostMapping("getSysCommentPage")
    public Result getSysCommentPage(@RequestBody SysComment sysComment) {
        Page<SysComment> page = new Page<>(sysComment.getPageNumber(),sysComment.getPageSize());
        QueryWrapper<SysComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(sysComment.getStar() != null,SysComment::getStar,sysComment.getStar())
                .eq(StringUtils.isNotBlank(sysComment.getTag()),SysComment::getTag,sysComment.getTag())
                .eq(StringUtils.isNotBlank(sysComment.getUserId()),SysComment::getUserId,sysComment.getUserId());
        Page<SysComment> sysCommentPage = sysCommentService.page(page, queryWrapper);
        return Result.success(sysCommentPage);
    }

    /** 根据id获取律师评价 */
    @GetMapping("getSysCommentById")
    public Result getSysCommentById(@RequestParam("id")String id) {
        SysComment sysComment = sysCommentService.getById(id);
        return Result.success(sysComment);
    }

    /** 保存律师评价 */
    @PostMapping("saveSysComment")
    @Transactional(rollbackFor = Exception.class)
    public Result saveSysComment(@RequestBody SysComment sysComment) {
        User user = userService.getById(sysComment.getUserId());
        //设置服务人数
        user.setNumPeople(user.getNumPeople() + 1);
        //设置好评
        QueryWrapper<SysComment> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(SysComment::getUserId,user.getId());
        List<SysComment> commentList = sysCommentService.list(queryWrapper);
        commentList.add(sysComment);
        int count = 0;
        for (SysComment comment : commentList) {
            //评分大于6分算好评
            if (comment.getStar() >= 3) {
                count++;
            }
        }
        user.setComment(count/commentList.size());
        userService.updateById(user);
        boolean save = sysCommentService.save(sysComment);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑律师评价 */
    @PostMapping("editSysComment")
    public Result editSysComment(@RequestBody SysComment sysComment) {
        boolean save = sysCommentService.updateById(sysComment);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除律师评价 */
    @GetMapping("removeSysComment")
    public Result removeSysComment(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysCommentService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("律师评价id不能为空！");
        }
    }

}