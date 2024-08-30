package miku.lib.jvm.hotspot.compiler;

import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

//OopMapSet @ 16
//  int _om_count @ 0
//  int _om_size @ 4
//  OopMap** _om_data @ 8

public class OopMapSet extends VMObject {

    private static final long _om_count_offset;
    private static final long _om_size_offset;
    private static final long _om_data_offset;

    private static int REG_COUNT;
    private static int SAVED_ON_ENTRY_REG_COUNT;
    private static int C_SAVED_ON_ENTRY_REG_COUNT;

    static {
        Type type = JVM.type("OopMapSet");
        _om_count_offset = type.offset("_om_count");
        _om_size_offset = type.offset("_om_size");
        _om_data_offset = type.offset("_om_data");
        if(!VM.isCore()){
            REG_COUNT = JVM.intConstant("REG_COUNT");
            if(VM.usingServerCompiler){
                SAVED_ON_ENTRY_REG_COUNT = JVM.intConstant("SAVED_ON_ENTRY_REG_COUNT");
                C_SAVED_ON_ENTRY_REG_COUNT = JVM.intConstant("C_SAVED_ON_ENTRY_REG_COUNT");
            }
        }
    }

    public OopMapSet(long address) {
        super(address);
    }

    public int getSize() {
        return unsafe.getInt(getAddress() + _om_size_offset);
    }

    public OopMap getMapAt(int index) {

        long omDataAddr = unsafe.getAddress(getAddress() + _om_data_offset);
        long oopMapAddr = unsafe.getAddress(omDataAddr + (long) index * unsafe.addressSize());
        return oopMapAddr == 0 ? null : new OopMap(oopMapAddr);
    }

    public OopMap findMapAtOffset(long pcOffset, boolean debugging) {
        int len = this.getSize();

        int i;
        for(i = 0; i < len && this.getMapAt(i).getOffset() < pcOffset; ++i) {
        }

        return this.getMapAt(i);
    }

}
