package com.zhouzhu.service.impl;

import com.zhouzhu.enums.SearchFriendsStatusEnum;
import com.zhouzhu.mapper.MyFriendsMapper;
import com.zhouzhu.mapper.UsersMapper;
import com.zhouzhu.pojo.MyFriends;
import com.zhouzhu.pojo.Users;
import com.zhouzhu.service.UserService;
import com.zhouzhu.utils.FastDFSClient;
import com.zhouzhu.utils.FileUtils;
import com.zhouzhu.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;

/**
 * @author zhouzhu
 * @Description
 * @create 2019-07-08 16:42
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private QRCodeUtils qrCodeUtils;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Autowired
    private MyFriendsMapper myFriendsMapper;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user=new Users();
        user.setUsername(username);
        Users result = usersMapper.selectOne(user);
        return result!=null?true:false;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String password) {
        Example userExample=new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",password);
        Users result = usersMapper.selectOneByExample(userExample);
        return result;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {
        String userId=sid.nextShort();
        // 为每个用户生成一个唯一的二维码
         String qrcodePath="E:\\"+userId+"qrcode.png";
         qrCodeUtils.createQRCode(qrcodePath,"muxin_qrcode:"+user.getUsername());
        MultipartFile qrcodeFile = FileUtils.fileToMultipart(qrcodePath);
        String qrcodeUrl="";
        try {
            qrcodeUrl=fastDFSClient.uploadQRCode(qrcodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        user.setQrcode(qrcodeUrl);
        user.setId(userId);
        usersMapper.insert(user);
        return user;
    }
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updateUserInfo(Users user) {
        usersMapper.updateByPrimaryKeySelective(user);
        return queryUserById(user.getId());
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    Users queryUserById(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        //1.搜索的用户如果不存在，返回【无此用户】
        Users user = queryUserInfoByUsername(friendUsername);
        if (ObjectUtils.isEmpty(user)){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        //2.搜索账号是自己，返回【不能添加自己】
        if (user.getId().equals(myUserId)){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        //3.搜索的朋友已经是你的好友，返回【该用户已经是你的好友】
        Example example=new Example(MyFriends.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("myUserId",myUserId);
        criteria.andEqualTo("myFriendUserId",user.getId());
        MyFriends myFriendsRel = myFriendsMapper.selectOneByExample(example);
        if (!ObjectUtils.isEmpty(myFriendsRel)){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserInfoByUsername(String username){
        Example example=new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username",username);
        return usersMapper.selectOneByExample(example);
    }
}
