package miku.lib.jvm.hotspot.code;

public class DeoptimizationBlob extends SingletonBlob{
    public DeoptimizationBlob(long address) {
        super(address);
    }

    public boolean isDeoptimizationStub() {
        return true;
    }
}
