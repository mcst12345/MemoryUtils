package miku.lib.jvm.hotspot.memory;

import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;

//VirtualSpace @ 112
//  char* _low_boundary @ 0
//  char* _high_boundary @ 8
//  char* _low @ 16
//  char* _high @ 24
//  char* _lower_high @ 40
//  char* _middle_high @ 48
//  char* _upper_high @ 56

public class VirtualSpace extends VMObject {

    private static final long _low_boundary_offset;
    private static final long _high_boundary_offset;
    private static final long _low_offset;
    private static final long _high_offset;
    private static final long _lower_high_offset;
    private static final long _middle_high_offset;
    private static final long _upper_high_offset;

    static {
        Type type = JVM.type("VirtualSpace");
        _low_boundary_offset = type.offset("_low_boundary");
        _high_boundary_offset = type.offset("_high_boundary");
        _low_offset = type.offset("_low");
        _high_offset = type.offset("_high");
        _lower_high_offset = type.offset("_lower_high");
        _middle_high_offset = type.offset("_middle_high");
        _upper_high_offset = type.offset("_upper_high");
    }

    public VirtualSpace(long address) {
        super(address);
    }

    public long low(){
        return unsafe.getAddress(getAddress() + _low_offset);
    }

    public long high(){
        return unsafe.getAddress(getAddress() + _high_offset);
    }

    public long lowBoundary(){
        return unsafe.getAddress(getAddress() + _low_boundary_offset);
    }

    public long highBoundary(){
        return unsafe.getAddress(getAddress() + _high_boundary_offset);
    }

    public long committedSize(){
        return AddressCalculator.minus(high(),low());
    }

    public long reservedSize(){
        return AddressCalculator.minus(highBoundary(),lowBoundary());
    }

    public long uncommittedSize() {
        return this.reservedSize() - this.committedSize();
    }

    public boolean contains(long address){
        return AddressCalculator.lessThanOrEqual(low(),address) && AddressCalculator.lessThanOrEqual(address,high());
    }
}
