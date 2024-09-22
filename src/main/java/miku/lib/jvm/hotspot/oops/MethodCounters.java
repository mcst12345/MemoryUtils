package miku.lib.jvm.hotspot.oops;

//MethodCounters extends MetaspaceObj @ 32
//  int _interpreter_invocation_count @ 0
//  u2 _interpreter_throwout_count @ 4
//  u2 _number_of_breakpoints @ 6
//  InvocationCounter _invocation_counter @ 8
//  InvocationCounter _backedge_counter @ 12

//InvocationCounter @ 4
//  unsigned int _counter @ 0

import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.utils.NumberTransformer;
import one.helfy.JVM;
import one.helfy.Type;

public class MethodCounters extends Metadata{

    private static final long _interpreter_invocation_count_offset;
    private static final long _interpreter_throwout_count_offset;
    private static final long _number_of_breakpoints_offset;
    private static final long _invocation_counter_offset;
    private static final long _backedge_counter_offset;

    static {
        Type type = JVM.type("MethodCounters");
        _interpreter_invocation_count_offset = type.offset("_interpreter_invocation_count");
        _interpreter_throwout_count_offset = type.offset("_interpreter_throwout_count");
        _number_of_breakpoints_offset = type.offset("_number_of_breakpoints");
        if(!VM.isCore()){
            _invocation_counter_offset = type.offset("_invocation_counter");
            _backedge_counter_offset = type.offset("_backedge_counter");
        } else {
            _invocation_counter_offset = 0;
            _backedge_counter_offset = 0;
        }
    }

    public MethodCounters(long address) {
        super(address);
    }


    public int interpreterInvocationCount() {
        return unsafe.getInt(getAddress() + _interpreter_invocation_count_offset);
    }

    public short interpreterThrowoutCount() {
        return unsafe.getShort(getAddress() + _interpreter_throwout_count_offset);
    }

    public long getInvocationCounter() {
        return NumberTransformer.dataToCInteger(unsafe.getBytes(getAddress() + _invocation_counter_offset,4), true);
    }

    public long getBackedgeCounter() {
        return NumberTransformer.dataToCInteger(unsafe.getBytes(getAddress() + _backedge_counter_offset,4), true);
    }

    public void clear_number_of_breakpoints(){
        unsafe.putShort(getAddress() + _number_of_breakpoints_offset, (short) 0);
    }
}
