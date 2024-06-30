package miku.lib.jvm.hotspot.runtime;

import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import sun.misc.Unsafe;

public abstract class VMObject {
    public static final Unsafe unsafe = InternalUtils.getUnsafe();
    public static final JVM jvm = JVM.getInstance();
    private final long address;

    public VMObject(long address) {
        this.address = address;
    }

    public String toString() {
        return this.getClass().getName() + "@" + this.address;
    }

    public final long getAddress() {
        return address;
    }

    @Override
    public int hashCode() {
        return (int) address;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VMObject) {
            return ((VMObject) obj).getAddress() == getAddress();
        }
        return false;
    }
}
