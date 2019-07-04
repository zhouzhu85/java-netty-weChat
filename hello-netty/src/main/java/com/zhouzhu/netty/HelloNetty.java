package com.zhouzhu.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author zhouzhu
 * @Description: 实现客户端发送一个请求，服务器会返回hello netty
 * @create 2019-07-04 15:32
 */
public class HelloNetty {
    public static void main(String[] args) throws Exception{
        //定义一对线程组
        //主线程组：用于接收客户端的连接，但是不做任何处理
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        //从线程组：完成主线程分配的任务
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try {
            //netty服务器创建，ServerBootstrap是个启动类
            ServerBootstrap serverBootstrap=new ServerBootstrap();
        /*
            设置主从线程组
            设置nio的双向通道
            子处理器，用于处理workerGroup
        */
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new HelloServerInitializer());

            //启动server，并且设置8088为启动的端口号，同时启动方式为同步
            ChannelFuture channelFuture = serverBootstrap.bind(8088).sync();
            //监听关闭的channel，设置为同步方式
            channelFuture.channel().closeFuture().sync();
        } finally {
            //关闭线程
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
