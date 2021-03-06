package com.zhouzhu.service.impl;

import com.zhouzhu.enums.MsgActionEnum;
import com.zhouzhu.enums.MsgSignFlagEnum;
import com.zhouzhu.enums.SearchFriendsStatusEnum;
import com.zhouzhu.mapper.*;
import com.zhouzhu.netty.ChatMsg;
import com.zhouzhu.netty.DataContent;
import com.zhouzhu.netty.UserChannelRel;
import com.zhouzhu.pojo.FriendsRequest;
import com.zhouzhu.pojo.MyFriends;
import com.zhouzhu.pojo.Users;
import com.zhouzhu.pojo.vo.FriendRequestVO;
import com.zhouzhu.pojo.vo.MyFriendsVO;
import com.zhouzhu.service.UserService;
import com.zhouzhu.utils.FastDFSClient;
import com.zhouzhu.utils.FileUtils;
import com.zhouzhu.utils.JsonUtils;
import com.zhouzhu.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.IOException;
import java.util.Date;
import java.util.List;

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

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;

    @Autowired
    private UsersMapperCustom usersMapperCustom;

    @Autowired
    private ChatMsgMapper chatMsgMapper;

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.SUPPORTS)
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

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public void sendFrindRequest(String myUserId, String friendUsername) {
        //查询好友信息
        Users friend = queryUserInfoByUsername(friendUsername);
        //查询发送好友请求记录表
        Example example=new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",myUserId);
        criteria.andEqualTo("acceptUserId",friend.getId());
        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(example);
        //如果不是好友，而且好友记录没有添加，则新增好友请求记录
        if (ObjectUtils.isEmpty(friendsRequest)){
            String requestId = sid.nextShort();
            FriendsRequest request = new FriendsRequest();
            request.setId(requestId);
            request.setSendUserId(myUserId);
            request.setAcceptUserId(friend.getId());
            request.setRequestDateTime(new Date());
            friendsRequestMapper.insert(request);
        }
    }
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return usersMapperCustom.queryFriendRequestList(acceptUserId);
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example example=new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",sendUserId);
        criteria.andEqualTo("acceptUserId",acceptUserId);
        friendsRequestMapper.deleteByExample(example);
    }
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        saveFriends(sendUserId,acceptUserId);
        saveFriends(acceptUserId,sendUserId);
        deleteFriendRequest(sendUserId,acceptUserId);

        Channel sendChannel = UserChannelRel.get(sendUserId);
        if (sendChannel!=null){
            //使用websocket主动推送消息到请求发起者，更新他的通讯录列表为最新
            DataContent dataContent=new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            sendChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    private void saveFriends(String sendUserId,String acceptUserId){
        MyFriends myFriends=new MyFriends();
        String recordId = sid.nextShort();
        myFriends.setId(recordId);
        myFriends.setMyFriendUserId(acceptUserId);
        myFriends.setMyUserId(sendUserId);
        myFriendsMapper.insert(myFriends);
    }
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVO> queryMyFriendss(String userId) {
        return usersMapperCustom.queryMyFriends(userId);
    }
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatMsg chatMsg) {
        com.zhouzhu.pojo.ChatMsg msgDB=new com.zhouzhu.pojo.ChatMsg();
        String msgId=sid.nextShort();
        msgDB.setId(msgId);
        msgDB.setAcceptUserId(chatMsg.getReceiverId());
        msgDB.setSendUserId(chatMsg.getSenderId());
        msgDB.setCreateTime(new Date());
        msgDB.setSignFlag(MsgSignFlagEnum.unsign.type);
        msgDB.setMsg(chatMsg.getMsg());
        chatMsgMapper.insert(msgDB);
        return msgId;
    }
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRED)
    @Override
    public void updateMsgSigned(List<String> msgIdList) {
        usersMapperCustom.batchUpdateMsgSigned(msgIdList);
    }

    @Transactional(rollbackFor = Exception.class,propagation = Propagation.SUPPORTS)
    @Override
    public List<com.zhouzhu.pojo.ChatMsg> getUnReadMsgList(String acceptUserId) {
        Example chatExample=new Example(com.zhouzhu.pojo.ChatMsg.class);
        Example.Criteria chatCriteria = chatExample.createCriteria();
        chatCriteria.andEqualTo("signFlag",0);
        chatCriteria.andEqualTo("acceptUserId",acceptUserId);
        List<com.zhouzhu.pojo.ChatMsg> result = chatMsgMapper.selectByExample(chatExample);
        return result;
    }
}
