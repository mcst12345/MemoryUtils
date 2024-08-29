package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.oops.Oop;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObjectFactory;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CompactibleFreeListSpace extends CompactibleSpace {

    private static final long MinChunkSizeInBytes;

    private static final long _collector_offset;
    private static final long _dictionary_offset;
    private static final long _indexedFreeList_offset;
    private static final long _smallLinearAllocBlock_offset;

    private static final int heapWordSize = VM.heapWordSize;
    private static final int IndexSetStart = VM.minObjAlignmentInBytes / heapWordSize;
    private static final int IndexSetStride = IndexSetStart;
    private static final int IndexSetSize = 257;

    static {
        long sizeofFreeChunk = JVM.type("FreeChunk").size;
        MinChunkSizeInBytes = numQuanta(sizeofFreeChunk, VM.minObjAlignmentInBytes) * (long)VM.minObjAlignmentInBytes;
        Type type = JVM.type("CompactibleFreeListSpace");
        _collector_offset = type.offset("_collector");
        _dictionary_offset = type.offset("_dictionary");
        _indexedFreeList_offset = type.offset("_indexedFreeList[0]");
        _smallLinearAllocBlock_offset = type.offset("_smallLinearAllocBlock");
    }

    private static long numQuanta(long x, long y) {
        return (x + y - 1L) / y;
    }

    public CompactibleFreeListSpace(long address){
        super(address);
    }



    public static long adjustObjectSizeInBytes(long sizeInBytes) {
        return Oop.alignObjectSize(Math.max(sizeInBytes, MinChunkSizeInBytes));
    }

    public long free0() {
        return this.capacity() - this.used0();
    }

    public long used() {
        return this.capacity() - this.free();
    }

    public long used0() {
        List<MemRegion> regions = this.getLiveRegions();
        long usedSize = 0L;

        MemRegion mr;
        for(Iterator<MemRegion> itr = regions.iterator(); itr.hasNext(); usedSize += mr.byteSize()) {
            mr = itr.next();
        }

        return usedSize;
    }

    public long free() {
        long size = 0L;
        long cur = getAddress() + _indexedFreeList_offset;
        cur = cur + ((long) IndexSetStart * AdaptiveFreeList.sizeOf());

        for(int i = IndexSetStart; i < IndexSetSize; i += IndexSetStride) {
            AdaptiveFreeList freeList = (AdaptiveFreeList) miku.lib.jvm.hotspot.runtime.VMObjectFactory.newObject(AdaptiveFreeList.class, cur);
            size += (long)i * freeList.count();
            cur = cur + ((long) IndexSetStride * AdaptiveFreeList.sizeOf());
        }

        AFLBinaryTreeDictionary aflbd = (AFLBinaryTreeDictionary) VMObjectFactory.newObject(AFLBinaryTreeDictionary.class, unsafe.getAddress(getAddress() + _dictionary_offset));
        size += aflbd.size();
        LinearAllocBlock lab = (LinearAllocBlock)VMObjectFactory.newObject(LinearAllocBlock.class, getAddress() + (_smallLinearAllocBlock_offset));
        size += lab.word_size();
        return size * (long)this.heapWordSize;
    }

    public List<MemRegion> getLiveRegions(){
        List<MemRegion> res = new ArrayList();
        long cur = this.bottom();
        long regionStart = cur;
        long limit = this.end();
        long addressSize = unsafe.addressSize();
        while (AddressCalculator.lessThan(cur,limit)){
            long k = unsafe.getAddress(cur + addressSize);
            long chunkSize;
            if(FreeChunk.indicatesFreeChunk(cur)){
                if(cur != regionStart){
                    res.add(new MemRegion(regionStart,cur,false));
                }
                FreeChunk fc = (FreeChunk) VMObjectFactory.newObject(FreeChunk.class,cur);
                chunkSize = fc.size();
                cur = cur + chunkSize * addressSize;
                regionStart = cur;
            } else if(k == 0){
                Oop obj = new Oop(cur);
                chunkSize = obj.getObjectSize();
                cur = cur + adjustObjectSizeInBytes(chunkSize);
            } else {
                long size = this.collector().blockSizeUsingPrintezisBits(cur);
                if (size == -1L) {
                    break;
                }

                cur = cur + adjustObjectSizeInBytes(size);
            }
        }


        return res;
    }

    public CMSCollector collector(){
        return (CMSCollector)VMObjectFactory.newObject(CMSCollector.class, unsafe.getAddress(getAddress() + _collector_offset));
    }

    public long skipBlockSizeUsingPrintezisBits(long pos){
        CMSCollector collector = this.collector();
        long size = 0L;
        long address = 0;
        if(collector != null){
            size = collector.blockSizeUsingPrintezisBits(pos);
            if (size >= 3L) {
                address = pos + adjustObjectSizeInBytes(size);
            }
        }
        return address;
    }

}
