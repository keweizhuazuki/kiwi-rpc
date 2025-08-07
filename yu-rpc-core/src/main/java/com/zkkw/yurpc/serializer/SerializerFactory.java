package com.zkkw.yurpc.serializer;

import com.zkkw.yurpc.spi.SpiLoader;

import java.util.HashMap;
import java.util.Map;

public class SerializerFactory {

    static {
        SpiLoader.load(Serializer.class);
    }

    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    public static Serializer getInstance(String key){
        return SpiLoader.getInstance(Serializer.class, key);
    }
}
