package miku.lib.jvm.hotspot.compiler;

import miku.lib.jvm.hotspot.code.CompressedWriteStream;
import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.JVM;
import one.helfy.Type;

//OopMap @ 32
//  int _pc_offset @ 0
//  int _omv_count @ 4
//  int _omv_data_size @ 8
//  unsigned char* _omv_data @ 16
//  CompressedWriteStream* _write_stream @ 24

public class OopMap extends VMObject {

    private static final long _pc_offset_offset;
    private static final long _omv_count_offset;
    private static final long _omv_data_size_offset;
    private static final long _omv_data_offset;
    private static final long _write_stream_offset;

    static {
        Type type = JVM.type("OopMap");
        _pc_offset_offset = type.offset("_pc_offset");
        _omv_count_offset = type.offset("_omv_count");
        _omv_data_size_offset = type.offset("_omv_data_size");
        _omv_data_offset = type.offset("_omv_data");
        _write_stream_offset = type.offset("_write_stream");
    }

    public OopMap(long address) {
        super(address);
    }

    public int getOffset() {
        return unsafe.getInt(getAddress() + _pc_offset_offset);
    }

    long getOMVData() {
        return unsafe.getAddress(getAddress() + _omv_data_offset);
    }

    int getOMVDataSize() {
        return unsafe.getInt(getAddress() + _omv_data_size_offset);
    }

    int getOMVCount() {
        return unsafe.getInt(getAddress() + _omv_count_offset);
    }

    private static final long _buffer_offset;

    static {
        _buffer_offset = JVM.type("CompressedStream").offset("_buffer");
    }

    CompressedWriteStream getWriteStream() {
        long wsAddr = unsafe.getAddress(getAddress() + _write_stream_offset);
        if (wsAddr == 0) {
            return null;
        } else {
            long bufferAddr = unsafe.getAddress(wsAddr + _buffer_offset);
            return bufferAddr == 0 ? null : new CompressedWriteStream(bufferAddr);
        }
    }
}
