package com.zhouzhu.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.stereotype.Component;

/**
 * @author zhouzhu
 * @Description:
 * @create 2019-07-04 17:28
 */
@Component
public class WsServer {

    private static class SingletionWsServer{
        static final WsServer instance=new WsServer();
    }
    public static WsServer getInstance(){
        return SingletionWsServer.instance;
    }

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;
    public WsServer(){
        mainGroup=new NioEventLoopGroup();
        subGroup=new NioEventLoopGroup();
        server=new ServerBootstrap();
        server.group(mainGroup,subGroup)
              .channel(NioServerSocketChannel.class)
              .childHandler(new WsServerInitialzer());
    }
    public void start(){
        this.future=server.bind(8088);
        System.out.println("netty websocket server 启动完毕...");
    }
}
