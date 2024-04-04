package com.project.lvshi.controller.question;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.*;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysQuestionItemService;
import com.project.lvshi.service.SysQuestionService;
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
 * @description: 法律问答controller
 * @date 2024/02/28 01:42
 */
@Controller
@ResponseBody
@RequestMapping("question")
public class SysQuestionController {

    @Autowired
    private SysQuestionService sysQuestionService;
    @Autowired
    private SysQuestionItemService sysQuestionItemService;
    @Autowired
    private UserService userService;

    /** 分页获取法律问答 */
    @PostMapping("getSysQuestionPage")
    public Result getSysQuestionPage(@RequestBody SysQuestion sysQuestion) {
        Page<SysQuestion> page = new Page<>(sysQuestion.getPageNumber(),sysQuestion.getPageSize());
        QueryWrapper<SysQuestion> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysQuestion.getTitle()),SysQuestion::getTitle,sysQuestion.getTitle())
                .eq(StringUtils.isNotBlank(sysQuestion.getUserId()),SysQuestion::getUserId,sysQuestion.getUserId())
                .like(StringUtils.isNotBlank(sysQuestion.getCreateBy()),SysQuestion::getCreateBy,sysQuestion.getCreateBy())
                .orderByDesc(SysQuestion::getCreateTime);
        Page<SysQuestion> sysQuestionPage = sysQuestionService.page(page, queryWrapper);
        for (SysQuestion question : sysQuestionPage.getRecords()) {
            QueryWrapper<SysQuestionItem> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(SysQuestionItem::getQuestionId,question.getId()).orderByDesc(SysQuestionItem::getCreateTime);
            List<SysQuestionItem> list = sysQuestionItemService.list(wrapper);
            question.setSysQuestionItems(list);
        }
        return Result.success(sysQuestionPage);
    }

    /** 根据id获取法律问答 */
    @GetMapping("getSysQuestionById")
    public Result getSysQuestionById(@RequestParam("id")String id) {
        SysQuestion sysQuestion = sysQuestionService.getById(id);
        return Result.success(sysQuestion);
    }

    /** 保存法律问答 */
    @PostMapping("saveSysQuestion")
    public Result saveSysQuestion(@RequestBody SysQuestion sysQuestion) {
        String id = TokenUtils.getUserIdByToken();
        User user = userService.getById(id);
        sysQuestion.setUserId(id);
        sysQuestion.setAvatar(user.getAvatar());
        boolean save = sysQuestionService.save(sysQuestion);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑法律问答 */
    @PostMapping("editSysQuestion")
    public Result editSysQuestion(@RequestBody SysQuestion sysQuestion) {
        boolean save = sysQuestionService.updateById(sysQuestion);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除法律问答 */
    @GetMapping("removeSysQuestion")
    @Transactional(rollbackFor = Exception.class)
    public Result removeSysQuestion(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysQuestionService.removeById(id);
                QueryWrapper<SysQuestionItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SysQuestionItem::getQuestionId,id);
                sysQuestionItemService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("法律问答id不能为空！");
        }
    }

}
