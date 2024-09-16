package miku.lib.jvm.hotspot.utilities;

import one.helfy.JVM;

public class HashtableEntry extends BasicHashtableEntry {
    private long _literal;

    public HashtableEntry(long address) {
        super(address);
        _literal = unsafe.getAddress(address + JVM.type("IntptrHashtableEntry").offset("_literal"));
    }

    public long literal() {
        return _literal;
    }
}
