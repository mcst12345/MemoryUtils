package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.runtime.VirtualBaseConstructor;

public abstract class Metadata extends VMObject {
    protected Metadata(long address) {
        super(address);
    }

    public static Metadata instantiateWrapperFor(long addr) {
        return (Metadata) VirtualBaseConstructor.INSTANCE.instantiateWrapperFor(addr);
    }
}
