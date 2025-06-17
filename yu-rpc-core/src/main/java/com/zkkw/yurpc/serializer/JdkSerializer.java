package com.zkkw.yurpc.serializer;

import java.io.*;

public class JdkSerializer implements Serializer {

    /**
     * JDK 序列化器
     * 使用 JDK 自带的序列化机制
     * 注意：JDK 序列化需要对象实现 Serializable 接口
     * @param object
     * @return
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return outputStream.toByteArray();
    }

    /**
     *
     *  JDK 反序列化器
     *  使用 JDK 自带的反序列化机制
     *  注意：JDK 反序列化需要对象实现 Serializable 接口
     * @param bytes
     * @param type
     * @return
     * @param <T>
     * @throws IOException
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> type) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        try {
            return (T) objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            objectInputStream.close();
        }

    }
}
