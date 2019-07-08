package com.zhouzhu.controller;

import com.zhouzhu.pojo.Users;
import com.zhouzhu.pojo.vo.UsersVO;
import com.zhouzhu.service.UserService;
import com.zhouzhu.utils.IMoocJSONResult;
import com.zhouzhu.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouzhu
 * @Description
 * @create 2019-07-08 16:27
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/registOrlogin")
    public IMoocJSONResult registOrLogin(@RequestBody Users users) throws Exception{
        if (StringUtils.isNotBlank(users.getUsername())
                || StringUtils.isNotBlank(users.getPassword())){
            return IMoocJSONResult.errorMsg("用户名或密码不能为空");
        }
        boolean usernameIsExist = userService.queryUsernameIsExist(users.getUsername());
        Users userResult=null;
        if (usernameIsExist){
            //登录
            userResult=userService.queryUserForLogin(users.getUsername(),MD5Utils.getMD5Str(users.getPassword()));
            if (userResult==null){
                return IMoocJSONResult.errorMsg("用户名或密码不正确");
            }
        }else {
            //注册
            users.setNickname(users.getUsername());
            users.setFaceImage("");
            users.setFaceImageBig("");
            users.setPassword(MD5Utils.getMD5Str(users.getPassword()));
            userResult=userService.saveUser(users);
        }
        UsersVO usersVO=new UsersVO();
        BeanUtils.copyProperties(userResult,usersVO);
        return IMoocJSONResult.ok(usersVO);
    }

}
