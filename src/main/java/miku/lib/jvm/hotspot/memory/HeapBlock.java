package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;
import sun.jvm.hotspot.debugger.Address;

//HeapBlock @ 16
//  HeapBlock::Header _header @ 0

//HeapBlock::Header @ 16
//  size_t _length @ 0
//  bool _used @ 8

public class HeapBlock extends VMObject {

    private static final long _header_offset;
    private static final long heapBlockSize;

    private static final long _length_offset;
    private static final long _used_offset;

    static {
        Type type = JVM.type("HeapBlock");
        _header_offset = type.offset("_header");
        heapBlockSize = type.size;
        type = JVM.type("HeapBlock::Header");
        _length_offset = type.offset("_length");
        _used_offset = type.offset("_used");
    }

    public HeapBlock(long address) {
        super(address);
    }

    public long getLength() {
        return this.getHeader().getLength();
    }

    public boolean isFree() {
        return this.getHeader().isFree();
    }

    public long getAllocatedSpace() {
        return getAddress() + heapBlockSize;
    }

    private Header getHeader() {
        return new Header(getAddress() + _header_offset);
    }

    public static class Header extends VMObject {
        public Header(long addr) {
            super(addr);
        }

        public long getLength() {
            return unsafe.getLong(getAddress() + _length_offset);
        }

        public boolean isFree() {
            return unsafe.getInt(getAddress() + _used_offset) == 0L;
        }
    }

}
