package com.moesif.helpers;

public class ExceptionUtils {
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void propagateIfInstanceOf(
            Throwable t,
            Class<T> type)
            throws T {
        if (type.isInstance(t)) {
            throw (T) t;
        }
    }
}