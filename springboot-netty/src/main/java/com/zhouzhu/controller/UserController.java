package com.zhouzhu.controller;

import com.zhouzhu.enums.OperatorFriendRequestTypeEnum;
import com.zhouzhu.enums.SearchFriendsStatusEnum;
import com.zhouzhu.pojo.ChatMsg;
import com.zhouzhu.pojo.Users;
import com.zhouzhu.pojo.bo.UsersBO;
import com.zhouzhu.pojo.vo.MyFriendsVO;
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

import javax.swing.*;
import java.security.PublicKey;
import java.util.List;

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
        if (StringUtils.isBlank(users.getUsername())
                || StringUtils.isBlank(users.getPassword())){
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

    /**
     * 修改昵称
     * @param usersBO
     * @return
     * @throws Exception
     */
    @PostMapping("setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBO usersBO) throws Exception{
        Users user=new Users();
        user.setId(usersBO.getUserId());
        user.setNickname(usersBO.getNickname());

        user=userService.updateUserInfo(user);
        return IMoocJSONResult.ok(user);
    }

    /**
     * 搜索好友接口
     * @param myUserId
     * @param friendUsername
     * @return
     * @throws Exception
     */
    @PostMapping("/search")
    public IMoocJSONResult searchUser(String myUserId,String friendUsername) throws Exception{
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return IMoocJSONResult.errorMsg("昵称不能为空");
        }
        //前置条件检测好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)){
            Users users = userService.queryUserInfoByUsername(friendUsername);
            UsersVO usersVO=new UsersVO();
            BeanUtils.copyProperties(users,usersVO);
            return IMoocJSONResult.ok(usersVO);
        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
    }

    /**
     * 发送好友请求
     * @param myUserId
     * @param friendUsername
     * @return
     */
    @PostMapping("addFriendRequest")
    public IMoocJSONResult addFriend(String myUserId,String friendUsername){
        if (StringUtils.isBlank(myUserId) || StringUtils.isBlank(friendUsername)){
            return IMoocJSONResult.errorMsg("昵称不能为空");
        }
        //前置条件检测好友
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)){
            userService.sendFrindRequest(myUserId,friendUsername);
        }else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
        return IMoocJSONResult.ok();
    }

    /**
     * 查询好友请求列表
     * @param userId
     * @return
     */
    @PostMapping("queryFriendRequests")
    public IMoocJSONResult queryFriendRequests(String userId){
        if (StringUtils.isBlank(userId)){
            return IMoocJSONResult.errorMsg("好友不存在");
        }
        return IMoocJSONResult.ok(userService.queryFriendRequestList(userId));
    }

    /**
     * 接收方通过或者忽略朋友请求
     * @param acceptUserId
     * @param sendUserId
     * @param operType
     * @return
     */
    @PostMapping("operFriendRequest")
    public IMoocJSONResult queryFriendRequests(String acceptUserId,String sendUserId,Integer operType){
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType==null){
            return IMoocJSONResult.errorMsg("好友不存在");
        }
        //没有对应的枚举值，抛出错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))){
            return IMoocJSONResult.errorMsg("");
        }
        //若是忽略，删除好友请求
        if (operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)){
            userService.deleteFriendRequest(sendUserId,acceptUserId);
        }
        //若是通过，添加成好友
        else if(operType.equals(OperatorFriendRequestTypeEnum.PASS.type)){
            userService.passFriendRequest(sendUserId,acceptUserId);
        }
        //获取好友列表
        List<MyFriendsVO> myFriends = userService.queryMyFriendss(acceptUserId);
        return IMoocJSONResult.ok(myFriends);
    }

    /**
     * 查询我的好友列表
     * @param userId
     * @return
     */
    @PostMapping("myFriends")
    public IMoocJSONResult myFriends(String userId){
        if (StringUtils.isBlank(userId)){
            return IMoocJSONResult.errorMsg("");
        }
        return IMoocJSONResult.ok(userService.queryMyFriendss(userId));
    }

    /**
     * 用户手机端获取未签收的消息列表
     * @param acceptUserId
     * @return
     */
    @PostMapping("getUnReadMsgList")
    public IMoocJSONResult getUnReadMsgList(String acceptUserId){
        if (StringUtils.isBlank(acceptUserId)){
            return IMoocJSONResult.errorMsg("");
        }
        List<ChatMsg> unreadMsgList = userService.getUnReadMsgList(acceptUserId);
        return IMoocJSONResult.ok(unreadMsgList);
    }
}
