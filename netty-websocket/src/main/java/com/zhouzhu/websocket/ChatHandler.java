package com.zhouzhu.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author zhouzhu
 * @Description: 处理消息的handler
 * TextWebSocketFrame 在netty中，是用于为websocket专门处理文本的对象，frame是消息的载体
 * @create 2019-07-04 17:52
 */
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    /** 用于记录和管理所有客户端的channle **/
    private static ChannelGroup clients=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     *  当客户端连接服务端之后（打开连接）
     *  获取客户端的channle，并且放到channelGroup中进行管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        clients.add(ctx.channel());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        //获取客户端传输过来的消息
        String content = textWebSocketFrame.text();
        System.out.println("接收到的数据："+content);


    }
}
