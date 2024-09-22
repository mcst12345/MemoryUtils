package miku.lib.jvm.hotspot.code;

public class UncommonTrapBlob extends SingletonBlob{
    public UncommonTrapBlob(long address) {
        super(address);
    }

    public boolean isUncommonTrapStub() {
        return true;
    }
}
