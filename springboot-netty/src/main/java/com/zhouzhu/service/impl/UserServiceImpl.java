package com.zhouzhu.service.impl;

import com.zhouzhu.mapper.UsersMapper;
import com.zhouzhu.pojo.Users;
import com.zhouzhu.service.UserService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

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
        // TODO 为每个用户生成一个唯一的二维码
        user.setQrcode("");
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
}
