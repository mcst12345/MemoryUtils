import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.tools.jcore.ClassWriter;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import sun.misc.Unsafe;

import java.io.FileOutputStream;
import java.lang.reflect.*;
import java.util.ArrayList;

import static one.helfy.JVM.*;

public class Test {

    private static void printHex(long hex){
        System.out.println("0x"+Long.toHexString(hex));
    }

    private static void exit(){
        Runtime.getRuntime().exit(0);
    }



    //The following method tries to extract the PID from java.lang.management.ManagementFactory:

    public static final Object lock = new Object();

    private static class A {}


    public static void main(String[] args) throws Throwable{
        //System.out.println(JVM.type("ConstantPoolCacheEntry"));
        try(FileOutputStream fos = new FileOutputStream("/mnt/Testtt")){
            ClassWriter cw = new ClassWriter((InstanceKlass) Klass.getKlass(ArrayList.class),fos);
            cw.write();
        }
    }

    private static int getAt(int i,int k){
        return (i >> k) & 1;
    }

    private static final Unsafe unsafe = InternalUtils.getUnsafe();
    private static final JVM jvm = JVM.getInstance();

    private static long getKlassAddr(Class<?> target){
        return intConstant("oopSize") == 8 ? unsafe.getLong(target, (long) getInt(type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(target, getInt(type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL;
    }



    private static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Constructor<?> constructor : getConstructors(clazz)) {
            if (arrayContentsEq(parameterTypes, constructor.getParameterTypes())) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        throw new NoSuchMethodError();
    }

    private static Constructor<?>[] getConstructors(Class<?> clazz) {
        try {
            return (Constructor<?>[]) getDeclaredConstructors0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new Constructor[0];
        }
    }

    private static Method[] getMethods(Class<?> clazz) {
        try {
            return (Method[]) getDeclaredMethods0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new Method[0];
        }
    }

    private static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Method method : getMethods(clazz)) {
            if (method.getName().equals(name) && arrayContentsEq(parameterTypes, method.getParameterTypes())) {
                return method;
            }
        }
        throw new NoSuchMethodError(name);
    }


    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }


    private static Field[] getFields(Class<?> clazz) {
        if (clazz == null) {
            return new Field[0];
        }
        try {
            return (Field[]) getDeclaredFields0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException | NullPointerException e) {
            return new Field[0];
        }
    }

    private static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Field field : getFields(clazz)) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static final Method getDeclaredFields0;
    private static final Method getDeclaredConstructors0;
    private static final Method getDeclaredMethods0;

    static {
        try {
            getDeclaredFields0 = Class.class.getDeclaredMethod("getDeclaredFields0", boolean.class);
            getDeclaredFields0.setAccessible(true);
            getDeclaredConstructors0 = Class.class.getDeclaredMethod("getDeclaredConstructors0", boolean.class);
            getDeclaredConstructors0.setAccessible(true);
            getDeclaredMethods0 = Class.class.getDeclaredMethod("getDeclaredMethods0", boolean.class);
            getDeclaredMethods0.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
