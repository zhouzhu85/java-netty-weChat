package com.zhouzhu.mapper;


import com.zhouzhu.pojo.Users;
import com.zhouzhu.pojo.vo.FriendRequestVO;
import com.zhouzhu.pojo.vo.MyFriendsVO;
import com.zhouzhu.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
    List<MyFriendsVO> queryMyFriends(String userId);
}