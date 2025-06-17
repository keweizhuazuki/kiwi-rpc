package com.zkkw.example.consumer;

import com.zkkw.example.common.model.User;
import com.zkkw.example.common.service.UserService;
import com.zkkw.yurpc.config.RpcConfig;
import com.zkkw.yurpc.proxy.ServiceProxy;
import com.zkkw.yurpc.proxy.ServiceProxyFactory;
import com.zkkw.yurpc.utils.ConfigUtils;

public class ConsumerExample {
    public static void main(String[] args) {
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("zhangsan");
        User newUser = userService.getUser(user);
        if (newUser != null) {
            System.out.println("用户名：" + newUser.getName());
        } else {
            System.out.println("user == null");
        }
        short nUmber = userService.getNumber();
        System.out.println("nUmber: " + nUmber);
    }
}
