package com.zhouzhu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author zhouzhu
 * @Description
 * @create 2019-07-05 17:36
 */
@SpringBootApplication
@MapperScan(basePackages = "com.zhouzhu.mapper")
@ComponentScan(basePackages = {"com.zhouzhu","org.n3r.idworker"})
public class SpringbootNettyApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringbootNettyApplication.class,args);
    }
}
