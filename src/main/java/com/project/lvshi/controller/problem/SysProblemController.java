package com.project.lvshi.controller.problem;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysProblem;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysProblemService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 问题controller
 * @date 2024/02/27 03:08
 */
@Controller
@ResponseBody
@RequestMapping("problem")
public class SysProblemController {

    @Autowired
    private SysProblemService sysProblemService;

    /** 分页获取问题 */
    @PostMapping("getSysProblemPage")
    public Result getSysProblemPage(@RequestBody SysProblem sysProblem) {
        Page<SysProblem> page = new Page<>(sysProblem.getPageNumber(),sysProblem.getPageSize());
        QueryWrapper<SysProblem> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysProblem.getName()),SysProblem::getName,sysProblem.getName());
        Page<SysProblem> sysProblemPage = sysProblemService.page(page, queryWrapper);
        return Result.success(sysProblemPage);
    }

    @GetMapping("getSysProblemList")
    public Result getSysProblemList() {
        List<SysProblem> problemList = sysProblemService.list();
        return Result.success(problemList);
    }

    /** 根据id获取问题 */
    @GetMapping("getSysProblemById")
    public Result getSysProblemById(@RequestParam("id")String id) {
        SysProblem sysProblem = sysProblemService.getById(id);
        return Result.success(sysProblem);
    }

    /** 保存问题 */
    @PostMapping("saveSysProblem")
    public Result saveSysProblem(@RequestBody SysProblem sysProblem) {
        boolean save = sysProblemService.save(sysProblem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑问题 */
    @PostMapping("editSysProblem")
    public Result editSysProblem(@RequestBody SysProblem sysProblem) {
        boolean save = sysProblemService.updateById(sysProblem);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除问题 */
    @GetMapping("removeSysProblem")
    public Result removeSysProblem(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysProblemService.removeById(id);
            }
            return Result.success();
        } else {
            return Result.fail("问题id不能为空！");
        }
    }

}