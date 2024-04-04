package com.project.lvshi.controller.seek;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.Result;
import com.project.lvshi.domain.SysSeek;
import com.project.lvshi.domain.SysSeekItem;
import com.project.lvshi.domain.User;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.SysSeekItemService;
import com.project.lvshi.service.SysSeekService;
import com.project.lvshi.service.UserService;
import com.project.lvshi.utils.TokenUtils;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;

/**
 * @author 超级管理员
 * @version 1.0
 * @description: 咨询controller
 * @date 2024/02/27 09:18
 */
@Controller
@ResponseBody
@RequestMapping("seek")
public class SysSeekController {

    @Autowired
    private SysSeekService sysSeekService;
    @Autowired
    private SysSeekItemService sysSeekItemService;
    @Autowired
    private UserService userService;

    /** 分页获取咨询 */
    @PostMapping("getSysSeekPage")
    public Result getSysSeekPage(@RequestBody SysSeek sysSeek) {
        Page<SysSeek> page = new Page<>(sysSeek.getPageNumber(),sysSeek.getPageSize());
        QueryWrapper<SysSeek> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .like(StringUtils.isNotBlank(sysSeek.getTitle()),SysSeek::getTitle,sysSeek.getTitle())
                .like(StringUtils.isNotBlank(sysSeek.getLawyerName()),SysSeek::getLawyerName,sysSeek.getLawyerName())
                .eq(sysSeek.getState() != null,SysSeek::getState,sysSeek.getState())
                .eq(StringUtils.isNotBlank(sysSeek.getLawyerId()),SysSeek::getLawyerId,sysSeek.getLawyerId())
                .eq(StringUtils.isNotBlank(sysSeek.getUserId()),SysSeek::getUserId,sysSeek.getUserId())
                .like(StringUtils.isNotBlank(sysSeek.getCreateBy()),SysSeek::getCreateBy,sysSeek.getCreateBy())
                .orderByDesc(SysSeek::getCreateTime);
        Page<SysSeek> sysSeekPage = sysSeekService.page(page, queryWrapper);
        for (SysSeek seek : sysSeekPage.getRecords()) {
            QueryWrapper<SysSeekItem> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(SysSeekItem::getSeekId,seek.getId()).orderByDesc(SysSeekItem::getCreateTime);
            List<SysSeekItem> itemList = sysSeekItemService.list(wrapper);
            seek.setSysSeekItems(itemList);
            User user = userService.getById(seek.getLawyerId());
            seek.setNumPeople(user.getNumPeople());
            seek.setBest(user.getBest());
        }
        return Result.success(sysSeekPage);
    }

    /** 根据id获取咨询 */
    @GetMapping("getSysSeekById")
    public Result getSysSeekById(@RequestParam("id")String id) {
        SysSeek sysSeek = sysSeekService.getById(id);
        return Result.success(sysSeek);
    }

    /** 保存咨询 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("saveSysSeek")
    public Result saveSysSeek(@RequestBody SysSeek sysSeek) throws Exception {
        String idStr = IdWorker.getIdStr();
        sysSeek.setId(idStr);
        String id = TokenUtils.getUserIdByToken();
        sysSeek.setUserId(id);
        User user = userService.getById(sysSeek.getLawyerId());
        sysSeek.setLawyerName(user.getUserName());
        sysSeek.setAvatar(user.getAvatar());
        boolean save = sysSeekService.save(sysSeek);
        //先保存内容
        SysSeekItem sysSeekItem = new SysSeekItem();
        sysSeekItem.setSeekId(idStr);
        sysSeekItem.setContent("您想要咨询的问题是" + sysSeek.getTitle() + ",已经通过邮件通知上线律师解答，请稍后。");
        sysSeekItem.setUserId(id);
        User user1 = userService.getById(id);
        sysSeekItem.setAvatar(user1.getAvatar());
        sysSeekItemService.save(sysSeekItem);
        //TODO 邮件通知
        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setPort(25);
            sender.setHost("smtp.qq.com");
            sender.setUsername("1760272627@qq.com");
            sender.setPassword("nwavnzopbtpibchc");
            sender.setDefaultEncoding("utf-8");
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(sender.getUsername());
            helper.setTo(user.getEmail());
            helper.setSubject("lvshi平台咨询通知");
            helper.setText("用户："+user1.getUserName() + "正在向您发起咨询，请及时上线回复！",true);
            sender.send(msg);
        } catch (Exception e) {
            throw new Exception("邮件发送失败");
        }
        if (save) {
            return Result.success(idStr);
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 编辑咨询 */
    @PostMapping("editSysSeek")
    public Result editSysSeek(@RequestBody SysSeek sysSeek) {
        boolean save = sysSeekService.updateById(sysSeek);
        if (save) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 删除咨询 */
    @GetMapping("removeSysSeek")
    @Transactional(rollbackFor = Exception.class)
    public Result removeSysSeek(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                sysSeekService.removeById(id);
                //删除咨询内容
                QueryWrapper<SysSeekItem> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(SysSeekItem::getSeekId,id);
                sysSeekItemService.remove(queryWrapper);
            }
            return Result.success();
        } else {
            return Result.fail("咨询id不能为空！");
        }
    }

}
