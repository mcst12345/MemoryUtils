package miku.lib.jvm.hotspot.code;

//RuntimeStub extends CodeBlob @ 64
//  bool _caller_must_gc_arguments @ 57

import one.helfy.JVM;
import one.helfy.Type;

public class RuntimeStub extends CodeBlob{

    private static final long _caller_must_gc_arguments_offset;

    static {
        Type type = JVM.type("RuntimeStub");
        _caller_must_gc_arguments_offset = type.offset("_caller_must_gc_arguments");
    }

    public RuntimeStub(long address) {
        super(address);
    }

    public boolean isRuntimeStub() {
        return true;
    }

    public boolean callerMustGCArguments() {
        return unsafe.getInt(getAddress() + _caller_must_gc_arguments_offset) != 0L;
    }

    public String getName() {
        return "RuntimeStub: " + super.getName();
    }

}
