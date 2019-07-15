package com.zhouzhu.service;

import com.zhouzhu.netty.ChatMsg;
import com.zhouzhu.pojo.Users;
import com.zhouzhu.pojo.vo.FriendRequestVO;
import com.zhouzhu.pojo.vo.MyFriendsVO;

import java.util.List;

/**
 * @author zhouzhu
 * @Description
 * @create 2019-07-08 16:31
 */
public interface UserService {
    /**
     * 判断用户名是否存在
     * @param username
     * @return
     */
    public boolean queryUsernameIsExist(String username);

    /**
     * 查询用户是否存在
     * @param username
     * @param password
     * @return
     */
    public Users queryUserForLogin(String username,String password);

    /**
     * 注册
     * @param user
     * @return
     */
    public Users saveUser(Users user);

    /**
     * 更新用户信息
     * @param user
     */
    Users updateUserInfo(Users user);

    /** 搜索朋友的前置条件
     * @param myUserId
     * @param friendUsername
     * @return
     */
    Integer preconditionSearchFriends(String myUserId, String friendUsername);

    /**
     * 根据用户名查询用户对象
     * @param friendUsername
     * @return
     */
    Users queryUserInfoByUsername(String friendUsername);

    /**
     *  添加好友
     * @param myUserId
     * @param friendUsername
     */
    void sendFrindRequest(String myUserId, String friendUsername);

    /**
     *  查询好友请求
     * @param acceptUserId
     * @return
     */
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);

    /**
     * 删除好友请求记录
     * @param sendUserId
     * @param acceptUserId
     */
    void deleteFriendRequest(String sendUserId, String acceptUserId);

    /**
     *  通过好友请求
     * @param sendUserId
     * @param acceptUserId
     */
    void passFriendRequest(String sendUserId, String acceptUserId);

    /**
     * 查询好友列表
     * @param userId
     * @return
     */
    List<MyFriendsVO> queryMyFriendss(String userId);

    /**
     * 保存聊天消息到数据库
     * @param chatMsg
     * @return
     */
    String saveMsg(ChatMsg chatMsg);

    /**
     * 批量签收消息
     * @param msgIdList
     */
    void updateMsgSigned(List<String> msgIdList);
}
