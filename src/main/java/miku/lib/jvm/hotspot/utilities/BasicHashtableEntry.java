package miku.lib.jvm.hotspot.utilities;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.Type;

import java.lang.reflect.InvocationTargetException;

public class BasicHashtableEntry extends VMObject {
    //private int _hash; //unsigned :(
    private long _next;

    public BasicHashtableEntry(long address) {
        super(address);
        Type type = jvm.type("BasicHashtableEntry<mtInternal>");
        //_hash = unsafe.getInt(address + type.offset("_hash"));
        _next = unsafe.getAddress(address + type.offset("_next"));
    }

    public BasicHashtableEntry next() {
        if (_next == 0) {
            return null;
        }
        //return new BasicHashtableEntry(_next);
        try {
            return getClass().getConstructor(long.class).newInstance(_next);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException |
                 InstantiationException e) {
            throw new RuntimeException(e);
        }
    }
}
