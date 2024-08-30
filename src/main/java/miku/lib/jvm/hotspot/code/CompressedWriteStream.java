package miku.lib.jvm.hotspot.code;

public class CompressedWriteStream extends CompressedStream{
    public CompressedWriteStream(long buffer) {
        super(buffer);
    }

    public CompressedWriteStream(long buffer, int position) {
        super(buffer, position);
    }
}
