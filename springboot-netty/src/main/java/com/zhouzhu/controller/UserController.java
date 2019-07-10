package com.zhouzhu.controller;

import com.zhouzhu.pojo.Users;
import com.zhouzhu.pojo.bo.UsersBO;
import com.zhouzhu.pojo.vo.UsersVO;
import com.zhouzhu.service.UserService;
import com.zhouzhu.utils.FastDFSClient;
import com.zhouzhu.utils.FileUtils;
import com.zhouzhu.utils.IMoocJSONResult;
import com.zhouzhu.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @Autowired
    private FastDFSClient fastDFSClient;

    @PostMapping("/registOrlogin")
    public IMoocJSONResult registOrLogin(@RequestBody Users users) throws Exception{
        if (!StringUtils.isNotBlank(users.getUsername())
                || !StringUtils.isNotBlank(users.getPassword())){
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

    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBO usersBO) throws Exception{
        //获取前端传过来的base64字符串，然后转换成文件对象再上传
        String base64Data = usersBO.getFaceData();
        String userFacePath="E:\\"+usersBO.getUserId()+"userface64.png";
        FileUtils.base64ToFile(userFacePath,base64Data);
        //上传文件到fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println("url = " + url);
        //获取缩略图的url
        String thump="_80x80.";
        String[] arr = url.split("\\.");
        String thumpImgUrl=arr[0]+thump+arr[1];

        //更新用户头像
        Users user=new Users();
        user.setId(usersBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        user=userService.updateUserInfo(user);

        return IMoocJSONResult.ok(user);
    }

    @PostMapping("setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBO usersBO) throws Exception{
        Users user=new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickname());

        user=userService.updateUserInfo(user);
        return IMoocJSONResult.ok(user);
    }

}
