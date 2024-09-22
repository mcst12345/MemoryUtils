package miku.lib.jvm.hotspot.code;

public class BufferBlob extends CodeBlob{
    public BufferBlob(long address) {
        super(address);
    }

    public boolean isBufferBlob() {
        return true;
    }
}
