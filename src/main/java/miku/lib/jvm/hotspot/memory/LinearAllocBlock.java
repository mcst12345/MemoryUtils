package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

//LinearAllocBlock @ 32
//  size_t _word_size @ 8

public class LinearAllocBlock extends VMObject {

    private static final long _word_size_offset;

    static {
        Type type = JVM.type("LinearAllocBlock");
        _word_size_offset = type.offset("_word_size");
    }

    public LinearAllocBlock(long address) {
        super(address);
    }

    public long word_size(){
        return unsafe.getLong(getAddress() + _word_size_offset);
    }
}
