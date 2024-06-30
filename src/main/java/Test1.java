import miku.lib.utils.InternalUtils;
import miku.lib.utils.ObjectUtils;
import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import one.helfy.JVM;
import sun.misc.Unsafe;

import java.lang.management.ManagementFactory;

import static one.helfy.JVM.*;

public class Test1 {
    public static void main(String[] args){
        InstanceKlass klass = (InstanceKlass) Klass.getKlass(CLASS.class);
        System.out.println(klass.getNonstaticFieldSize());
        System.out.println(klass);
        for(int i = 0;  i < klass.getNonstaticFieldSize(); i++){
            System.out.println(klass.getFieldName(i));
            System.out.println(klass.getFieldOffset(i));
            System.out.println("------------------------");
        }
        CLASS test = new CLASS();
        System.out.println(test.f4);
        unsafe.putIntVolatile(test,klass.getFieldOffset(3),114514);
        System.out.println(test.f4);
        long address = ObjectUtils.location(test);
        unsafe.putInt(address + klass.getFieldOffset(3),191919);
        System.out.println(test.f4);
    }

    public static class CLASS {
        int f1;
        int f2;
        int f3;
        int f4;
    }

    private static final Unsafe unsafe = InternalUtils.getUnsafe();
    private static final JVM jvm = JVM.getInstance();

    private static long getKlassAddr(Class<?> target){
        return intConstant("oopSize") == 8 ? unsafe.getLong(target, (long) getInt(type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(target, getInt(type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL;
    }

    private static String getProcessId(final String fallback) {
        // Note: may fail in some JVM implementations
        // therefore fallback has to be provided

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        final String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        final int index = jvmName.indexOf('@');

        if (index < 1) {
            // part before '@' empty (index = 0) / '@' not found (index = -1)
            return fallback;
        }

        try {
            return Long.toString(Long.parseLong(jvmName.substring(0, index)));
        } catch (NumberFormatException e) {
            // ignore
        }
        return fallback;
    }
}
