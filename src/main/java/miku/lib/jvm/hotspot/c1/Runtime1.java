package miku.lib.jvm.hotspot.c1;

import miku.lib.jvm.hotspot.code.CodeBlob;
import miku.lib.jvm.hotspot.code.CodeCache;
import miku.lib.utils.memory.MemoryHelper;
import one.helfy.JVM;
import one.helfy.Type;

//Runtime1 @ 1
//  static null _blobs @ 0x784d23d8c780

public class Runtime1 {

    private static final long _blobs;

    static {
        Type type = JVM.type("Runtime1");
        _blobs = type.global("_blobs");
    }

    private static final MemoryHelper unsafe = MemoryHelper.getInstance();

    public CodeBlob blobFor(int id){
        long address = unsafe.getAddress(_blobs + (long) id * unsafe.addressSize());
        return CodeCache.createCodeBlobWrapper(address);
    }



}
