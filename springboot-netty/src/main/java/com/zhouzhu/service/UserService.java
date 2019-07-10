package com.zhouzhu.service;

import com.zhouzhu.pojo.Users;

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
}
