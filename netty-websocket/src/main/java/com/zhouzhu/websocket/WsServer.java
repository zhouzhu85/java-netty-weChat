package com.zhouzhu.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author zhouzhu
 * @Description:
 * @create 2019-07-04 17:28
 */
public class WsServer {

    public static void main(String[] args) throws Exception{
        EventLoopGroup mainGroup=new NioEventLoopGroup();
        EventLoopGroup subGroup=new NioEventLoopGroup();

        try {
            ServerBootstrap server=new ServerBootstrap();
            server.group(mainGroup,subGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WsServerInitialzer());

            ChannelFuture future = server.bind(8089).sync();
            future.channel().closeFuture().sync();
        } finally {
            mainGroup.shutdownGracefully();
            subGroup.shutdownGracefully();
        }
    }
}
