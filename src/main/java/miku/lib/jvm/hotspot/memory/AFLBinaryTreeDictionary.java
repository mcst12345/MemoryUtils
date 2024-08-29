package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

//AFLBinaryTreeDictionary @ 32
//  size_t _total_size @ 8

public class AFLBinaryTreeDictionary extends VMObject {
    private static final long _total_size_offset;

    public AFLBinaryTreeDictionary(long address) {
        super(address);
    }

    public long size(){
        return unsafe.getLong(getAddress() + _total_size_offset);
    }

    static {
        Type type = JVM.type("AFLBinaryTreeDictionary");
        _total_size_offset = type.offset("_total_size");
    }
}
