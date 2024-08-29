package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.runtime.VMObjectFactory;
import miku.lib.jvm.hotspot.utilities.BitMap;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;
import sun.jvm.hotspot.runtime.VM;

//CMSBitMap @ 192
//  size_t _bmWordSize @ 8
//  const int _shifter @ 16
//  VirtualSpace _virtual_space @ 24
//  BitMap _bm @ 136

public class CMSBitMap extends VMObject {

    private static final long _bmWordSize_offset;
    private static final long _shifter_offset;
    private static final long _virtual_space_offset;
    private static final long _bm_offset;

    static {
        Type type = JVM.type("CMSBitMap");
        _bmWordSize_offset = type.offset("_bmWordSize");
        _shifter_offset = type.offset("_shifter");
        _virtual_space_offset = type.offset("_virtual_space");
        _bm_offset = type.offset("_bm");
    }

    public CMSBitMap(long address) {
        super(address);
    }

    public long bmStartWord(){
        return unsafe.getAddress(getAddress() + _bm_offset);
    }

    public long bmWordSize(){
        return unsafe.getLong(getAddress() + _bmWordSize_offset);
    }

    public int shifter(){
        return unsafe.getInt(getAddress() + _shifter_offset);
    }

    public VirtualSpace virtualSpace(){
        return (VirtualSpace) VMObjectFactory.newObject(VirtualSpace.class,getAddress() + _virtual_space_offset);
    }

    public BitMap bm(){
        BitMap bitMap = new BitMap((int) (this.bmWordSize() >> this.shifter()));
        VirtualSpace vs = virtualSpace();
        bitMap.set_map(vs.low());
        return bitMap;
    }

    public long getNextMarkedWordAddress(long address){
        long endWord = bmStartWord() + bmWordSize();
        int nextOffset = bm().getNextOneOffset(heapWordToOffset(address),heapWordToOffset(endWord));
        return offsetToHeapWord(nextOffset);
    }

    int heapWordToOffset(long address){
        int temp = (int) AddressCalculator.minus(address,bmStartWord()) / (int) VM.getVM().getAddressSize();
        return temp >> this.shifter();
    }

    long offsetToHeapWord(int offset) {
        int temp = offset << this.shifter();
        return this.bmStartWord() + (long)temp * VM.getVM().getAddressSize();
    }

    boolean isMarked(long address){
        BitMap bm = this.bm();
        return bm.at(this.heapWordToOffset(address));
    }
}
