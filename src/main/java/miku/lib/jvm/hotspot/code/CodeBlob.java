package miku.lib.jvm.hotspot.code;

import miku.lib.jvm.hotspot.compiler.OopMap;
import miku.lib.jvm.hotspot.compiler.OopMapSet;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.VMObject;
import miku.lib.jvm.hotspot.utilities.CStringUtilities;
import miku.lib.utils.AddressCalculator;
import one.helfy.JVM;
import one.helfy.Type;

//CodeBlob @ 64
//  const char* _name @ 8
//  int _size @ 16
//  int _header_size @ 20
//  int _relocation_size @ 24
//  int _content_offset @ 28
//  int _code_offset @ 32
//  int _frame_complete_offset @ 36
//  int _data_offset @ 40
//  int _frame_size @ 44
//  OopMapSet* _oop_maps @ 48

public class CodeBlob extends VMObject {

    private static final long _name_offset;
    private static final long _size_offset;
    private static final long _header_size_offset;
    private static final long _relocation_size_offset;
    private static final long _content_offset_offset;
    private static final long _code_offset_offset;
    private static final long _frame_complete_offset_offset;
    private static final long _data_offset_offset;
    private static final long _frame_size_offset;
    private static final long _oop_maps_offset;

    static {
        Type type = JVM.type("CodeBlob");
        _name_offset = type.offset("_name");
        _size_offset = type.offset("_size");
        _header_size_offset = type.offset("_header_size");
        _relocation_size_offset = type.offset("_relocation_size");
        _content_offset_offset = type.offset("_content_offset");
        _code_offset_offset = type.offset("_code_offset");
        _frame_complete_offset_offset = type.offset("_frame_complete_offset");
        _data_offset_offset = type.offset("_data_offset");
        _frame_size_offset = type.offset("_frame_size");
        _oop_maps_offset = type.offset("_oop_maps");
    }

    private static final int NOT_YET_COMPUTED = -2;
    private static final int UNDEFINED = -1;
    private int linkOffset = -2;
    private static int matcherInterpreterFramePointerReg;

    static {
        if(VM.usingServerCompiler){
            matcherInterpreterFramePointerReg = JVM.intConstant("Matcher::interpreter_frame_pointer_reg");
        }
    }

    public CodeBlob(long address) {
        super(address);
    }

    public boolean isBufferBlob() {
        return false;
    }

    public boolean isNMethod() {
        return false;
    }

    public boolean isRuntimeStub() {
        return false;
    }

    public boolean isDeoptimizationStub() {
        return false;
    }

    public boolean isUncommonTrapStub() {
        return false;
    }

    public boolean isExceptionStub() {
        return false;
    }

    public boolean isSafepointStub() {
        return false;
    }

    public boolean isAdapterBlob() {
        return false;
    }

    public boolean isJavaMethod() {
        return false;
    }

    public boolean isNativeMethod() {
        return false;
    }

    public boolean isOSRMethod() {
        return false;
    }

    public NMethod asNMethodOrNull(){
        return isNMethod() ? (NMethod) this : null;
    }

    public long headerBegin() {
        return getAddress();
    }

    public long headerEnd() {
        return getAddress() + unsafe.getInt(getAddress() + _header_size_offset);
    }

    public long contentBegin() {
        return this.headerBegin() + unsafe.getInt(getAddress() + _content_offset_offset);
    }

    public long contentEnd() {
        return this.headerBegin() + unsafe.getInt(getAddress() + _data_offset_offset);
    }

    public long codeBegin() {
        return this.headerBegin() + unsafe.getInt(getAddress() + _content_offset_offset);
    }

    public long codeEnd() {
        return this.headerBegin() + unsafe.getInt(getAddress() + _data_offset_offset);
    }

    public long dataBegin() {
        return this.headerBegin() + unsafe.getInt(getAddress() + _data_offset_offset);
    }

    public long dataEnd() {
        return this.headerBegin() + unsafe.getInt(getAddress() + _size_offset);
    }

    public int getRelocationOffset() {
        return unsafe.getInt(getAddress() + _header_size_offset);
    }

    public int getContentOffset() {
        return unsafe.getInt(getAddress() + _content_offset_offset);
    }

    public int getCodeOffset() {
        return unsafe.getInt(getAddress() + _code_offset_offset);
    }

    public int getDataOffset() {
        return unsafe.getInt(getAddress() + _data_offset_offset);
    }

    public int getSize() {
        return unsafe.getInt(getAddress() + _size_offset);
    }

    public int getHeaderSize() {
        return unsafe.getInt(getAddress() + _header_size_offset);
    }

    public int getContentSize() {
        return (int) AddressCalculator.minus(this.contentEnd(),this.contentBegin());
    }

    public int getCodeSize() {
        return (int) AddressCalculator.minus(this.codeEnd(),this.codeBegin());
    }

    public int getDataSize() {
        return (int) AddressCalculator.minus(this.dataEnd(),this.dataBegin());
    }


    public boolean blobContains(long addr) {
        return AddressCalculator.lessThanOrEqual(headerBegin(),addr) && AddressCalculator.greaterThan(dataEnd(),addr);
    }

    public boolean contentContains(long addr) {
        return AddressCalculator.lessThanOrEqual(contentBegin(),addr) && AddressCalculator.greaterThan(contentEnd(),addr);
    }

    public boolean codeContains(long addr) {
        return AddressCalculator.lessThanOrEqual(codeBegin(),addr) && AddressCalculator.greaterThan(codeEnd(),addr);
    }

    public boolean dataContains(long addr) {
        return AddressCalculator.lessThanOrEqual(dataBegin(),addr) && AddressCalculator.greaterThan(dataEnd(),addr);
    }

    public boolean contains(long addr) {
        return this.contentContains(addr);
    }

    public boolean isFrameCompleteAt(long a) {
        return this.codeContains(a) && AddressCalculator.minus(a,codeBegin()) >= unsafe.getInt(getAddress() + _frame_complete_offset_offset);
    }

    public boolean isZombie() {
        return false;
    }

    public boolean isLockedByVM() {
        return false;
    }

    public OopMapSet getOopMaps() {
        long oopMapsAddr = unsafe.getAddress(getAddress() + _oop_maps_offset);
        return oopMapsAddr == 0 ? null : new OopMapSet(oopMapsAddr);
    }

    public OopMap getOopMapForReturnAddress(long returnAddress, boolean debugging) {

        return this.getOopMaps().findMapAtOffset(AddressCalculator.minus(returnAddress,codeBegin()), debugging);
    }

    public int getFrameSize() {
        return unsafe.addressSize() * unsafe.getInt(getAddress() + _frame_size_offset);
    }

    public boolean callerMustGCArguments() {
        return false;
    }

    public String getName() {
        return CStringUtilities.getString(unsafe.getAddress(getAddress() + _name_offset));
    }

}
