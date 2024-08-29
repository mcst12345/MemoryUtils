package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

//AdaptiveFreeList<FreeChunk> @ 160
//  size_t _size @ 16
//  ssize_t _count @ 24

public class AdaptiveFreeList extends VMObject {

    private static final long _size_offset;
    private static final long _count_offset;
    private static final long headerSize;

    static {
        Type type = JVM.type("AdaptiveFreeList<FreeChunk>");
        _size_offset = type.offset("_size");
        _count_offset = type.offset("_count");
        headerSize = type.size;
    }

    public long size() {
        return unsafe.getLong(getAddress() + _size_offset);
    }

    public long count() {
        return unsafe.getLong(getAddress() + _count_offset);
    }

    public static long sizeOf() {
        return headerSize;
    }

    public AdaptiveFreeList(long address) {
        super(address);
    }
}
