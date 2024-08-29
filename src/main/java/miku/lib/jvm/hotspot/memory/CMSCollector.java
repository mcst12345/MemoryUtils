package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.runtime.VMObjectFactory;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;

//CMSCollector @ 1992
//  CMSBitMap _markBitMap @ 840

public class CMSCollector extends VMObject {

    private static final long _markBitMap_offset;

    static {
        Type type = JVM.type("CMSCollector");
        _markBitMap_offset = type.offset("_markBitMap");
    }

    public CMSCollector(long address) {
        super(address);
    }

    public CMSBitMap markBitMap() {
        return (CMSBitMap) VMObjectFactory.newObject(CMSBitMap.class, getAddress() + _markBitMap_offset);
    }

    public long blockSizeUsingPrintezisBits(long address){
        CMSBitMap markBitMap = this.markBitMap();
        long addressSize = unsafe.addressSize();
        if(markBitMap.isMarked(address) && markBitMap.isMarked(address + addressSize)){
            long nextOneAddr = markBitMap.getNextMarkedWordAddress(address + (2L * addressSize));
            return AddressCalculator.minus(nextOneAddr+addressSize,address);
        } else {
            return -1;
        }
    }
}
