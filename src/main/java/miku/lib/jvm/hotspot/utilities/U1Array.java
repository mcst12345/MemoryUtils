package miku.lib.jvm.hotspot.utilities;

import one.helfy.Type;

public class U1Array extends GenericArray{

    protected static final Type elemType = jvm.type("u1");
    private static final long dataFieldOffset = jvm.type("Array<u1>").offset("_data");

    public U1Array(long address) {
        super(address, dataFieldOffset);
    }

    @Override
    public Type getElemType() {
        return elemType;
    }

    public byte at(int i) {
        return (byte) this.getIntegerAt(i);
    }
}
