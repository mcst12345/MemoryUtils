package miku.lib.jvm.hotspot.code;

public class ExceptionBlob extends SingletonBlob{
    public ExceptionBlob(long address) {
        super(address);
    }

    public boolean isExceptionStub() {
        return true;
    }

}
