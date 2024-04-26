package org.wep.utils;

public class ArrayUtils {
    private ArrayUtils(){}
    public static boolean isEmpty(Object[] arr) {
        return (arr == null || arr.length == 0);
    }
}
