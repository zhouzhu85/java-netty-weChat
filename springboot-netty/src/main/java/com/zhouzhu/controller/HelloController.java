package com.zhouzhu.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouzhu
 * @Description
 * @create 2019-07-08 10:44
 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello(){
        return "hello netty";
    }
}
