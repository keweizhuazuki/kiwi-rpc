package com.zkkw.example.consumer;

import com.zkkw.example.common.model.User;
import com.zkkw.example.common.service.UserService;

public class EasyConsumerExample {
    public static void main(String[] args) {
        UserService userService = null;
        User user = new User();
        user.setName("张三");
        User newUser = userService.getUser(user);
        if(newUser != null){
            System.out.println("用户名：" + newUser.getName());
        } else {
            System.out.println("用户信息获取失败");
        }

    }
}
