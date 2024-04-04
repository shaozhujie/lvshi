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
 * @description: 法律问答内容controller
 * @date 2024/02/28 01:51
 */
@Controller
@ResponseBody
@RequestMapping("item")
public class SysQuestionItemController {

    @Autowired
    private SysQuestionItemService sysQuestionItemService;
    @Autowired
    private UserService userService;
    @Autowired
    private SysQuestionService sysQuestionService;

    /** 分页获取法律问答内容 */
    @PostMapping("getSysQuestionItemPage")
    public Result getSysQuestionItemPage(@RequestBody SysQuestionItem sysQuestionItem) {
        Page< SysQuestionItem > page = new Page<>(sysQuestionItem.getPageNumber(),sysQuestionItem.getPageSize());
        QueryWrapper<SysQuestionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(sysQuestionItem.getQuestionId()),SysQuestionItem::getQuestionId,sysQuestionItem.getQuestionId())
                .eq(StringUtils.isNotBlank(sysQuestionItem.getContent()),SysQuestionItem::getContent,sysQuestionItem.getContent())
                .orderByDesc(SysQuestionItem::getCreateTime);
        Page<SysQuestionItem> itemPage = sysQuestionItemService.page(page, queryWrapper);
        return Result.success(itemPage);
    }

    @PostMapping("getSysQuestionItemList")
    public Result getSysQuestionItemList(@RequestBody SysQuestionItem sysQuestionItem) {
        QueryWrapper<SysQuestionItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(sysQuestionItem.getQuestionId()),SysQuestionItem::getQuestionId,sysQuestionItem.getQuestionId())
                .eq(StringUtils.isNotBlank(sysQuestionItem.getContent()),SysQuestionItem::getContent,sysQuestionItem.getContent());
        List<SysQuestionItem> itemList = sysQuestionItemService.list(queryWrapper);
        return Result.success(itemList);
    }

    /** 根据id获取法律问答内容 */
    @GetMapping("getSysQuestionItemById")
    public Result getSysQuestionItemById(@RequestParam("id")String id) {
        SysQuestionItem sysQuestionItem = sysQuestionItemService.getById(id);
        return Result.success(sysQuestionItem);
    }

    /** 保存法律问答内容 */
    @PostMapping("saveSysQuestionItem")
    @Transactional(rollbackFor = Exception.class)
    public Result saveSysQuestionItem(@RequestBody SysQuestionItem sysQuestionItem) {
        String id = TokenUtils.getUserIdByToken();
        User user = userService.getById(id);
        sysQuestionItem.setUserId(user.getId());
        sysQuestionItem.setAvatar(user.getAvatar());
        boolean save = sysQuestionItemService.save(sysQuestionItem);
        SysQuestion question = sysQuestionService.getById(sysQuestionItem.getQuestionId());
        question.setNum(question.getNum() + 1);
        sysQuestionService.updateById(question);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑法律问答内容 */
    @PostMapping("editSysQuestionItem")
    public Result editSysQuestionItem(@RequestBody SysQuestionItem sysQuestionItem) {
        boolean save = sysQuestionItemService.updateById(sysQuestionItem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除法律问答内容 */
    @GetMapping("removeSysQuestionItem")
    public Result removeSysQuestionItem(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysQuestionItemService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("法律问答内容id不能为空！");
        }
    }

}
