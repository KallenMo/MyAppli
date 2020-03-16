package com.example.myappli.util;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

/**
 * 可关闭对象工具类
 * @author BC
 * @date 2019/2/19
 */
public final class CloseUtil {

    public static void close(Closeable obj) {
        if (obj != null) {
            try {
                obj.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Closeable... objs) {
        for (Closeable obj : objs) {
            close(obj);
        }
    }

    public static void close(Collection<Closeable> objs) {
        for (Closeable obj : objs) {
            close(obj);
        }
    }

}
