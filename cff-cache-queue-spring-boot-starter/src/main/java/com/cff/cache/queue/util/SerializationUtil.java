package com.cff.cache.queue.util;

import java.io.*;

public class SerializationUtil {
    
    /**
     * 序列化对象
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            return bos.toByteArray();
        }
    }
    
    /**
     * 反序列化对象
     * @param data 序列化后的字节数组
     * @return 反序列化后的对象
     */
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return ois.readObject();
        }
    }
    
    /**
     * 序列化对象为字符串
     * @param obj 要序列化的对象
     * @return 序列化后的字符串
     */
    public static String serializeToString(Object obj) throws IOException {
        return java.util.Base64.getEncoder().encodeToString(serialize(obj));
    }
    
    /**
     * 从字符串反序列化对象
     * @param str 序列化后的字符串
     * @return 反序列化后的对象
     */
    public static Object deserializeFromString(String str) throws IOException, ClassNotFoundException {
        byte[] data = java.util.Base64.getDecoder().decode(str);
        return deserialize(data);
    }
}