package miku.lib.jvm.hotspot.runtime;

import one.helfy.JVM;
import one.helfy.Type;

import java.util.ArrayList;
import java.util.List;

public class VM {
    private static VM soleInstance;
    private static long stackBias;
    private static int invocationEntryBCI;
    private static int invalidOSREntryBCI;
    private static int bytesPerWord;

    static {
        JVM jvm = JVM.getInstance();

        stackBias = jvm.intConstant("STACK_BIAS");
        invocationEntryBCI = jvm.intConstant("InvocationEntryBci");
        invalidOSREntryBCI = jvm.intConstant("InvalidOSREntryBci");
        bytesPerWord = jvm.intConstant("BytesPerWord");

        Type type = jvm.type("Abstract_VM_Versio");
    }

    public int getBytesPerWord() {
        return this.bytesPerWord;
    }
}
