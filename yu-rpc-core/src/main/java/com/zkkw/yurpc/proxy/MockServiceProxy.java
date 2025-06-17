package com.zkkw.yurpc.proxy;


import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@Slf4j
public class MockServiceProxy implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> methodReturnType = method.getReturnType();
        log.info("method return type is {}", methodReturnType);
        return getDefaultObject(methodReturnType);
    }
    private Object getDefaultObject(Class<?> type) {
        if(type.isPrimitive()){
            if(type == boolean.class){
                return false;
            }else if(type == short.class){
                return (short) 0;
            }else if(type == int.class){
                return 0;
            }else if(type == long.class){
                return 0L;
            }else if(type == float.class){
                return 0.0f;
            }else if(type == double.class){
                return 0.0d;
            }else if(type == char.class){
                return '\0';
            }else if(type == byte.class){
                return (byte) 0;
            }
        }
        // 对于非基本类型，返回 null
        return null;
    }


}
