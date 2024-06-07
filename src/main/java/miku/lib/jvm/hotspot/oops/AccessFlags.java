package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VMObject;

public class AccessFlags extends VMObject {
    private int flags;

    public AccessFlags(long address) {
        super(address);
        long _flags_offset = jvm.type("AccessFlags").offset("_flags");
        flags = unsafe.getInt(address + _flags_offset);
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
        long _flags_offset = jvm.type("AccessFlags").offset("_flags");
        unsafe.putInt(getAddress() + _flags_offset, flags);
    }
}
