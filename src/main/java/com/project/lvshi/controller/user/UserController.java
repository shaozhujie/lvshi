package com.project.lvshi.controller.user;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.lvshi.domain.*;
import com.project.lvshi.enums.ResultCode;
import com.project.lvshi.service.*;
import com.project.lvshi.utils.PasswordUtils;
import com.project.lvshi.utils.RedisUtils;
import com.project.lvshi.utils.TokenUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @version 1.0
 * @description: 用户controller
 * @date 2024/2/26 21:00
 */
@Controller
@ResponseBody
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private SysSeekService sysSeekService;
    @Autowired
    private SysSeekItemService sysSeekItemService;
    @Autowired
    private SysQuestionService sysQuestionService;
    @Autowired
    private SysQuestionItemService sysQuestionItemService;
    @Autowired
    private SysKnowledgesFavorService sysKnowledgesFavorService;
    @Autowired
    private SysCommentService sysCommentService;

    /** 分页查询用户 */
    @PostMapping("getUserPage")
    public Result getUserPage(@RequestBody User user) {
        Page<User> page = userService.getUserPage(user);
        return Result.success(page);
    }

    /** 根据id查询用户 */
    @GetMapping("getUserById")
    public Result getUserById(@RequestParam("id") String id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    /** 新增用户 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("saveUser")
    public Result saveUser(@RequestBody User user) {
        //先校验登陆账号是否重复
        boolean account = checkAccount(user);
        if (!account) {
            return Result.fail("登陆账号已存在不可重复！");
        }
        String uuid = IdWorker.get32UUID();
        //密码加盐加密
        String encrypt = PasswordUtils.encrypt(user.getPassword());
        String[] split = encrypt.split("\\$");
        user.setId(uuid);
        user.setPassword(split[0]);
        user.setSalt(split[1]);
        user.setAvatar("/img/1697436716646531073.jpeg");
        user.setPwdUpdateDate(new Date());
        //保存用户
        boolean save = userService.save(user);
        return Result.success();
    }

    /** 编辑用户 */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping("editUser")
    public Result editUser(@RequestBody User user) {
        User user1 = userService.getById(user.getId());
        if (!user1.getLoginAccount().equals(user.getLoginAccount())) {
            //先校验登陆账号是否重复
            boolean account = checkAccount(user);
            if (!account) {
                return Result.fail("登陆账号已存在不可重复！");
            }
        }
        //更新用户
        boolean edit = userService.updateById(user);
        return Result.success();
    }

    /** 校验用户 */
    public boolean checkAccount(User user) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getLoginAccount,user.getLoginAccount());
        int count = userService.count(queryWrapper);
        return count <= 0;
    }

    /** 删除用户 */
    @Transactional(rollbackFor = Exception.class)
    @GetMapping("removeUser")
    public Result removeUser(@RequestParam("ids")String ids) {
        if (StringUtils.isNotBlank(ids)) {
            String[] asList = ids.split(",");
            for (String id : asList) {
                User user = userService.getById(id);
                boolean remove = userService.removeById(id);
                //如果是用户，删除提问，咨询
                if (user.getUserType() == 1) {
                    //删除咨询
                    QueryWrapper<SysSeek> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(SysSeek::getUserId,id);
                    List<SysSeek> seekList = sysSeekService.list(queryWrapper);
                    sysSeekService.remove(queryWrapper);
                    for (SysSeek seek : seekList) {
                        QueryWrapper<SysSeekItem> queryWrapper1 = new QueryWrapper<>();
                        queryWrapper1.lambda().eq(SysSeekItem::getSeekId,seek.getId());
                        sysSeekItemService.remove(queryWrapper1);
                    }
                    //删除问答
                    QueryWrapper<SysQuestion> queryWrapper2 = new QueryWrapper<>();
                    queryWrapper2.lambda().eq(SysQuestion::getUserId,id);
                    List<SysQuestion> questionList = sysQuestionService.list(queryWrapper2);
                    sysQuestionService.remove(queryWrapper2);
                    for (SysQuestion sysQuestion : questionList) {
                        QueryWrapper<SysQuestionItem> queryWrapper3 = new QueryWrapper<>();
                        queryWrapper3.lambda().eq(SysQuestionItem::getQuestionId,sysQuestion.getId());
                        sysQuestionItemService.remove(queryWrapper3);
                    }
                } else if (user.getUserType() == 2) {
                    //删除咨询
                    QueryWrapper<SysSeek> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(SysSeek::getLawyerId,id);
                    List<SysSeek> seekList = sysSeekService.list(queryWrapper);
                    sysSeekService.remove(queryWrapper);
                    for (SysSeek seek : seekList) {
                        QueryWrapper<SysSeekItem> queryWrapper1 = new QueryWrapper<>();
                        queryWrapper1.lambda().eq(SysSeekItem::getSeekId,seek.getId());
                        sysSeekItemService.remove(queryWrapper1);
                    }
                    //删除评论
                    QueryWrapper<SysComment> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.lambda().eq(SysComment::getUserId,id);
                    sysCommentService.remove(queryWrapper1);
                }
            }
            return Result.success();
        } else {
            return Result.fail("角色id不能为空！");
        }
    }

    /** 重置密码 */
    @PostMapping("resetPassword")
    public Result resetPassword(@RequestBody JSONObject json) {
        String id = json.getString("id");
        String newPassword = json.getString("newPassword");
        String encrypt = PasswordUtils.encrypt(newPassword);
        String[] split = encrypt.split("\\$");
        User user = userService.getById(id);
        boolean decrypt = PasswordUtils.decrypt(newPassword, user.getPassword() + "$" + user.getSalt());
        if (decrypt) {
            return Result.fail("新密码不可和旧密码相同！");
        }
        user.setPassword(split[0]);
        user.setSalt(split[1]);
        user.setPwdUpdateDate(new Date());
        boolean update = userService.updateById(user);
        if (update) {
            return Result.success();
        } else {
            return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
        }
    }

    /** 获取登陆用户信息 */
    @GetMapping("getUserInfo")
    public Result getUserInfo() {
        String id = TokenUtils.getUserIdByToken();
        User user = userService.getById(id);
        return Result.success(user);
    }

    /** 修改个人信息 */
    @PostMapping("setUserInfo")
    public Result setUserInfo(@RequestBody User user) {
        String id = TokenUtils.getUserIdByToken();
        user.setId(id);
        userService.updateById(user);
        return Result.success();
    }

    /** 修改个人头像 */
    @PostMapping("setUserAvatar/{id}")
    public Result setUserAvatar(@PathVariable("id") String id, @RequestParam("file") MultipartFile avatar) {
        if(StringUtils.isBlank(id)){
            return Result.fail("用户id为空!");
        }
        User apeUser = userService.getById(id);
        if(avatar.isEmpty()){
            return Result.fail("上传的头像不能为空!");
        }
        String coverType = avatar.getOriginalFilename().substring(avatar.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase();
        if ("jpeg".equals(coverType)  || "gif".equals(coverType) || "png".equals(coverType) || "bmp".equals(coverType)  || "jpg".equals(coverType)) {
            //文件路径
            String filePath = System.getProperty("user.dir")+System.getProperty("file.separator")+"img";
            //文件名=当前时间到毫秒+原来的文件名
            String fileName = id + "."+ coverType;
            //如果文件路径不存在，新增该路径
            File file1 = new File(filePath);
            if(!file1.exists()){
                boolean mkdir = file1.mkdir();
            }
            //现在的文件地址
            if (StringUtils.isNotBlank(apeUser.getAvatar())) {
                String s = apeUser.getAvatar().split("/")[2];
                File now = new File(filePath + System.getProperty("file.separator") + s);
                boolean delete = now.delete();
            }
            //实际的文件地址
            File dest = new File(filePath + System.getProperty("file.separator") + fileName);
            //存储到数据库里的相对文件地址
            String storeImgPath = "/img/"+fileName;
            try {
                avatar.transferTo(dest);
                //更新头像
                apeUser.setAvatar(storeImgPath);
                userService.updateById(apeUser);
                return Result.success(storeImgPath);
            } catch (IOException e) {
                return Result.fail("上传失败");
            }
        } else {
            return Result.fail("请选择正确的图片格式");
        }
    }

    @GetMapping("getLawyerIndex")
    public Result getLawyerIndex() {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getUserType,2).orderByDesc(User::getNumPeople).last("limit 3");
        List<User> userList = userService.list(queryWrapper);
        return Result.success(userList);
    }

    @PostMapping("changePassword")
    public Result changePassword(@RequestBody JSONObject json) {
        String id = json.getString("id");
        String password = json.getString("password");
        User user = userService.getById(id);
        boolean decrypt = PasswordUtils.decrypt(password, user.getPassword() + "$" + user.getSalt());
        if (decrypt) {
            String newPassword = json.getString("newPassword");
            String encrypt = PasswordUtils.encrypt(newPassword);
            String[] split = encrypt.split("\\$");
            user.setSalt(split[1]);
            user.setPassword(split[0]);
            user.setPwdUpdateDate(new Date());
            boolean update = userService.updateById(user);
            if (update) {
                return Result.success();
            } else {
                return Result.fail(ResultCode.COMMON_DATA_OPTION_ERROR.getMessage());
            }
        } else {
            return Result.fail("旧密码不正确");
        }
    }

    @GetMapping("getEmailReg")
    public Result getEmailReg(@RequestParam("email") String email) {
        String str="abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789";
        Random r=new Random();
        String arr[]=new String [4];
        String reg="";
        for(int i=0;i<4;i++) {
            int n=r.nextInt(62);
            arr[i]=str.substring(n,n+1);
            reg+=arr[i];
        }
        try {
            redisUtils.set(email + "forget",reg.toLowerCase(),60L);
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setPort(25);
            sender.setHost("smtp.qq.com");
            sender.setUsername("1760272627@qq.com");
            sender.setPassword("nwavnzopbtpibchc");
            sender.setDefaultEncoding("utf-8");
            MimeMessage msg = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true);
            helper.setFrom(sender.getUsername());
            helper.setTo(email);
            helper.setSubject("Foodtuck修改密码验证");
            helper.setText("您的邮箱验证码为："+reg,true);
            sender.send(msg);
        }catch (Exception e){
            Result.fail("邮件发送失败");
        }
        return Result.success();
    }

    @PostMapping("forgetPassword")
    public Result forgetPassword(@RequestBody JSONObject jsonObject) {
        String loginAccount = jsonObject.getString("loginAccount");
        String email = jsonObject.getString("email");
        String password = jsonObject.getString("password");
        String code = jsonObject.getString("code").toLowerCase();
        String s = redisUtils.get(email + "forget");
        if (!code.equals(s)) {
            return Result.fail("验证码错误");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(User::getLoginAccount,loginAccount).last("limit 1");
        User user = userService.getOne(queryWrapper);
        //密码加盐加密
        String encrypt = PasswordUtils.encrypt(password);
        String[] split = encrypt.split("\\$");
        user.setPassword(split[0]);
        user.setSalt(split[1]);
        boolean update = userService.updateById(user);
        if (update) {
            return Result.success();
        } else {
            return Result.fail("密码修改失败");
        }
    }

    @PostMapping("toExamine")
    public Result toExamine(@RequestBody JSONObject jsonObject) throws MessagingException {
        String id = jsonObject.getString("id");
        Integer status = jsonObject.getInteger("status");
        User user1 = userService.getById(id);
        user1.setStatus(status);
        userService.updateById(user1);
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
            helper.setTo(user1.getEmail());
            helper.setSubject("lvshi平台审核通知");
            if (status == 0) {
                //通过
                helper.setText("您在本平台注册的律师账号："+ user1.getLoginAccount() +"已通过审核，请注意查收，祝您生活愉快！");
            } else {
                //停用
                helper.setText("您在本平台注册的律师账号："+ user1.getLoginAccount() +"已被管理员停用，如有问题请联系平台管理员:0539-8765321！");
            }
            sender.send(msg);
        } catch (Exception e) {
            Result.fail("邮件发送失败");
        }
        return Result.success();
    }

    @GetMapping("/getLawyerData")
    public Result getLawyerData() {
        JSONObject json = new JSONObject();
        String id = TokenUtils.getUserIdByToken();
        User user = userService.getById(id);
        json.put("num",user.getNumPeople());
        json.put("comment",user.getComment());
        //获取图表数据
        List<String> dates = new ArrayList<>();
        List<Integer> nums = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            String formattedDate = dateFormat.format(date);
            QueryWrapper<SysSeek> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().ge(SysSeek::getCreateTime,formattedDate + " 00:00:00")
                    .le(SysSeek::getCreateTime,formattedDate + " 23:59:59")
                    .eq(SysSeek::getLawyerId,id);
            int count = sysSeekService.count(queryWrapper);
            nums.add(count);
            dates.add(formattedDate);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        json.put("dates",dates);
        json.put("nums",nums);
        return Result.success(json);
    }

    @GetMapping("getIndexManage")
    public Result getIndexManage() {
        JSONObject json = new JSONObject();
        List<String> dates = new ArrayList<>();
        List<Integer> nums = new ArrayList<>();
        List<Integer> numsTotal = new ArrayList<>();
        List<Integer> seekNums = new ArrayList<>();
        List<Integer> seekNumsTotal = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            Date date = calendar.getTime();
            String formattedDate = dateFormat.format(date);
            //最近七日咨询量
            QueryWrapper<SysSeek> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().ge(SysSeek::getCreateTime,formattedDate + " 00:00:00")
                    .le(SysSeek::getCreateTime,formattedDate + " 23:59:59");
            int count = sysSeekService.count(queryWrapper);
            nums.add(count);
            //最近七日咨询总量
            QueryWrapper<SysSeek> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.lambda().le(SysSeek::getCreateTime,formattedDate + " 23:59:59");
            int count1 = sysSeekService.count(queryWrapper);
            numsTotal.add(count1);
            //最近七日提问量
            QueryWrapper<SysQuestion> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.lambda().ge(SysQuestion::getCreateTime,formattedDate + " 00:00:00")
                    .le(SysQuestion::getCreateTime,formattedDate + " 23:59:59");
            int count2 = sysQuestionService.count(queryWrapper2);
            seekNums.add(count2);
            //最近七日提问总量
            QueryWrapper<SysQuestion> queryWrapper3 = new QueryWrapper<>();
            queryWrapper3.lambda().le(SysQuestion::getCreateTime,formattedDate + " 23:59:59");
            int count3 = sysQuestionService.count(queryWrapper3);
            seekNumsTotal.add(count3);
            dates.add(formattedDate);
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        json.put("dates",dates);
        json.put("nums",nums);
        json.put("numsTotal",numsTotal);
        json.put("seekNums",seekNums);
        json.put("seekNumsTotal",seekNumsTotal);
        return Result.success(json);
    }

}
