package com.zhouzhu.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author zhouzhu
 * @Description:用于检测channel的心跳handler  继承ChannelInboundHandlerAdapter，从不需要实现channelRead0方法
 * @create 2019-07-17 17:32
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //判读evt是否是IdleStateEvent（用于触发用户事件，包含读空闲/写空闲/读写空闲）
       if (evt instanceof IdleStateEvent){
           IdleStateEvent event=(IdleStateEvent)evt;
           if (event.state()== IdleState.READER_IDLE){
               System.out.println("进入读空闲...");
           }else if(event.state()==IdleState.WRITER_IDLE){
               System.out.println("进入写空闲");
           }else if(event.state()==IdleState.ALL_IDLE){
               System.out.println("channel关闭前，users的数量为：" + ChatHandler.users.size());
               Channel channel = ctx.channel();
               //关闭无用的channel,以防浪费资源
               channel.close();
               System.out.println("channel关闭之后，users的数量为：" + ChatHandler.users.size());
           }
       }
    }
}
