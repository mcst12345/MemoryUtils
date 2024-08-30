package miku.lib.jvm.hotspot.memory;

import one.helfy.JVM;
import one.helfy.Type;

//GenCollectedHeap extends SharedHeap @ 240
//  static GenCollectedHeap* _gch @ 0x7d25c87972f0
//  int _n_gens @ 124
//  null _gens @ 128
//  GenerationSpec** _gen_specs @ 208

public class GenCollectedHeap extends SharedHeap{

    private static final long _n_gens_offset;
    private static final long _gens;
    private static final long _gen_specs;

    static {
        Type type = JVM.type("GenCollectedHeap");
        _n_gens_offset = type.offset("_n_gens");
        _gens = type.offset("_gens");
        _gen_specs = type.global("_gen_specs");
    }

    public GenCollectedHeap(long address) {
        super(address);
    }
}
