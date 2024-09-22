package miku.lib.jvm.hotspot.memory;

import me.xdark.shell.JVMUtil;
import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.utils.AddressCalculator;
import miku.lib.utils.NumberTransformer;
import miku.lib.utils.memory.MemoryHelper;
import one.helfy.JVM;
import one.helfy.Type;

//CodeHeap @ 280
//  VirtualSpace _memory @ 0
//  VirtualSpace _segmap @ 112
//  int _log2_segment_size @ 248

public class CodeHeap extends VMObject {

    private static final long _memory_offset;
    private static final long _segmap_offset;
    private static final long _log2_segment_size_offset;

    static {
        Type type = JVM.type("CodeHeap");
        _memory_offset = type.offset("_memory");
        _segmap_offset = type.offset("_segmap");
        _log2_segment_size_offset = type.offset("_log2_segment_size");
    }

    private VirtualSpace memory;
    private VirtualSpace segmentMap;
    private int log2SegmentSize;


    public CodeHeap(long address) {
        super(address);
        this.log2SegmentSize = unsafe.getInt(getAddress() + _log2_segment_size_offset);
        this.memory = new VirtualSpace(getAddress() + _memory_offset);
        this.segmentMap = new VirtualSpace(getAddress() + _segmap_offset);
    }

    public VirtualSpace getMemory(){
        return this.memory;
    }

    private VirtualSpace getSegmentMap() {
        return this.segmentMap;
    }

    public long begin() {
        return this.getMemory().low();
    }

    public long end() {
        return this.getMemory().high();
    }

    public boolean contains(long p) {
        return AddressCalculator.lessThanOrEqual(this.begin(),p) && AddressCalculator.greaterThan(end(),p);
    }

    public long findStart(long p) {
        if (!this.contains(p)) {
            return 0;
        } else {
            HeapBlock h = this.blockStart(p);
            return h != null && !h.isFree() ? h.getAllocatedSpace() : null;
        }
    }

    public long nextBlock(long ptr) {
        long base = this.blockBase(ptr);
        if (base == 0) {
            return 0;
        } else {
            HeapBlock block = this.getBlockAt(base);
            return base + block.getLength() * (1L << this.getLog2SegmentSize());
        }
    }

    private long segmentFor(long p) {
        return AddressCalculator.minus(p,(this.getMemory().low()) >> this.getLog2SegmentSize());
    }

    private int getLog2SegmentSize() {
        return this.log2SegmentSize;
    }

    private HeapBlock getBlockAt(long addr) {
        return new HeapBlock(addr);
    }

    private HeapBlock blockStart(long p) {
        long base = this.blockBase(p);
        return base == 0 ? null : this.getBlockAt(base);
    }

    private long blockBase(long p) {
        long i = this.segmentFor(p);
        long b = this.getSegmentMap().low();
        if (NumberTransformer.dataToCInteger(MemoryHelper.getInstance().getBytes(b+i,1), true) == 255L) {
            return 0;
        } else {
            while(NumberTransformer.dataToCInteger(MemoryHelper.getInstance().getBytes(b+i,1), true) > 0L) {
                i -= NumberTransformer.dataToCInteger(MemoryHelper.getInstance().getBytes(b+i,1), true);
            }

            return this.getMemory().low() + (i << this.getLog2SegmentSize());
        }
    }
}
