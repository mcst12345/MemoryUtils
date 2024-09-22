package miku.lib.jvm.hotspot.code;

public class SafepointBlob extends SingletonBlob {
    public SafepointBlob(long address) {
        super(address);
    }

    public boolean isSafepointStub() {
        return true;
    }

}
