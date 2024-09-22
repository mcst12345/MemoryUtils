package miku.lib.jvm.hotspot.code;

public class SingletonBlob extends CodeBlob{
    public SingletonBlob(long address) {
        super(address);
    }

    public boolean isSingletonBlob() {
        return true;
    }

}
