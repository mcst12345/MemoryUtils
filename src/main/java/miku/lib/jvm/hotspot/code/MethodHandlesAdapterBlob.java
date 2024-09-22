package miku.lib.jvm.hotspot.code;

public class MethodHandlesAdapterBlob extends AdapterBlob{
    public MethodHandlesAdapterBlob(long address) {
        super(address);
    }

    public boolean isMethodHandlesAdapterBlob() {
        return true;
    }

    public String getName() {
        return "MethodHandlesAdapterBlob: " + super.getName();
    }

}
