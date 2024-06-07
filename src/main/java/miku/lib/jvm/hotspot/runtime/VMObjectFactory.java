package miku.lib.jvm.hotspot.runtime;

import sun.jvm.hotspot.runtime.ConstructionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class VMObjectFactory {
    public static VMObject newObject(Class<? extends VMObject> clazz, long addr) throws ConstructionException {
        try {
            if (addr == 0L) {
                return null;
            } else {
                Constructor<? extends VMObject> c = clazz.getConstructor(long.class);
                return c.newInstance(addr);
            }
        } catch (InvocationTargetException var3) {
            if (var3.getTargetException() instanceof RuntimeException) {
                throw (RuntimeException) var3.getTargetException();
            } else {
                throw new ConstructionException(var3);
            }
        } catch (Exception var4) {
            throw new ConstructionException(var4);
        }
    }
}
