package one.helfy;

import miku.lib.reflection.ReflectionHelper;

public class VMThread {
    private static final java.lang.reflect.Field eetop;

    static {
        try {
            eetop = ReflectionHelper.getField(Thread.class, "eetop");
            eetop.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new JVMException("Thread.eetop field not found");
        }
    }

    public static long of(Thread javaThread) {
        try {
            return eetop.getLong(javaThread);
        } catch (IllegalAccessException e) {
            throw new JVMException(e);
        }
    }

    public static long current() {
        return of(Thread.currentThread());
    }
}
