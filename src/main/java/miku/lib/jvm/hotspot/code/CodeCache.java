package miku.lib.jvm.hotspot.code;

import miku.lib.jvm.hotspot.memory.CodeHeap;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VirtualBaseConstructor;
import miku.lib.utils.AddressCalculator;
import miku.lib.utils.memory.MemoryHelper;
import one.helfy.JVM;
import one.helfy.Type;

//CodeCache @ 1
//  static nmethod* _scavenge_root_nmethods @ 0x75929cb8f7a0
//  static CodeHeap* _heap @ 0x75929cb8f7c0

public class CodeCache {

    private static final MemoryHelper unsafe = MemoryHelper.getInstance();

    private static final long _scavenge_root_nmethods;
    private static final long _heap;

    private static final CodeHeap heap;

    static {
        Type type = JVM.type("CodeCache");
        _scavenge_root_nmethods = type.global("_scavenge_root_nmethods");
        _heap = type.global("_heap");
        heap = new CodeHeap(unsafe.getAddress(_heap));
    }

    public static NMethod scavengeRootMethods(){
        return new NMethod(unsafe.getAddress(_scavenge_root_nmethods));
    }

    public static CodeHeap getHeap(){
        return heap;
    }

    public static boolean contains(long p) {
        return getHeap().contains(p);
    }

    public static CodeBlob findBlob(long start) {
        CodeBlob result = findBlobUnsafe(start);
        if (result == null) {
            return null;
        } else {
            return result;
        }
    }

    public static CodeBlob findBlobUnsafe(long start) {
        CodeBlob result;

        result = (CodeBlob) VirtualBaseConstructor.INSTANCE.instantiateWrapperFor(getHeap().findStart(start));

        if(result == null){
            try {
                long cbAddr = getHeap().findStart(start);
                String message = "Couldn't deduce type of CodeBlob ";
                if (cbAddr != 0) {
                    message = message + "@" + cbAddr + " ";
                }

                message = message + "for PC=" + start;
                throw new RuntimeException(message);
            } catch (Exception var6) {
                Exception findEx = var6;
                findEx.printStackTrace();
            }
        }

        return result;
    }

    public static NMethod findNMethod(long start) {
        CodeBlob cb = findBlob(start);

        return (NMethod)cb;
    }

    public static NMethod findNMethodUnsafe(long start) {
        CodeBlob cb = findBlobUnsafe(start);

        return (NMethod)cb;
    }


    public static CodeBlob createCodeBlobWrapper(long codeBlobAddr) {
        try {
            return (CodeBlob)VirtualBaseConstructor.INSTANCE.instantiateWrapperFor(codeBlobAddr);
        } catch (Exception var4) {
            String message = "Unable to deduce type of CodeBlob from address " + codeBlobAddr + " (expected type nmethod, RuntimeStub, ";
            if (VM.usingClientCompiler) {
                message = message + " or ";
            }

            message = message + "SafepointBlob";
            if (VM.usingServerCompiler) {
                message = message + ", DeoptimizationBlob, or ExceptionBlob";
            }

            message = message + ")";
            throw new RuntimeException(message);
        }
    }

    public static void iterate(CodeCacheVisitor visitor) {
        CodeHeap heap = getHeap();
        long ptr = heap.begin();
        long end = heap.end();
        visitor.prologue(ptr, end);

        long next;
        for(CodeBlob lastBlob = null; ptr != 0 && AddressCalculator.lessThan(ptr,end); ptr = next) {
            try {
                CodeBlob blob = findBlobUnsafe(heap.findStart(ptr));
                if (blob != null) {
                    visitor.visit(blob);
                    if (blob == lastBlob) {
                        throw new InternalError("saw same blob twice");
                    }

                    lastBlob = blob;
                }
            } catch (RuntimeException var7) {
                RuntimeException e = var7;
                e.printStackTrace();
            }

            next = heap.nextBlock(ptr);
            if (next != 0 && AddressCalculator.lessThan(next,ptr)) {
                throw new InternalError("pointer moved backwards");
            }
        }

        visitor.epilogue();
    }
}
