package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

public class CollectedHeap extends VMObject {

    private static final long _reserved_offset;

    static {
        Type type = JVM.type("CollectedHeap");
        _reserved_offset = type.offset("_reserved");
    }

    public CollectedHeap(long address) {
        super(address);
    }
}
