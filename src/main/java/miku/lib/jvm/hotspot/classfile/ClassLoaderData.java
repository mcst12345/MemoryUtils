package miku.lib.jvm.hotspot.classfile;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.oops.Oop;
import one.helfy.JVM;
import one.helfy.Type;

public class ClassLoaderData extends VMObject {
    private static final long _class_loader_offset;
    private static final long _next_offset;

    static {
        Type type = JVM.type("ClassLoaderData");
        _class_loader_offset = type.offset("_class_loader");
        _next_offset = type.offset("_next");
    }

    private Oop _class_loader;
    private final long _next;

    public ClassLoaderData(long address) {
        super(address);
        _class_loader = new Oop(unsafe.getAddress(address + _class_loader_offset));
        _next = unsafe.getAddress(address + _next_offset);
    }

    public ClassLoaderData next() {
        return new ClassLoaderData(_next);
    }

    public Oop getClassLoader() {
        return _class_loader;
    }
    public void setClassLoader(Oop oop){
        _class_loader = oop;
        unsafe.putAddress(getAddress() + _class_loader_offset,oop.getAddress());
    }
}
