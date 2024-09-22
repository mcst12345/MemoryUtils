package miku.lib.jvm.hotspot.code;

public class AdapterBlob extends CodeBlob{
    public AdapterBlob(long address) {
        super(address);
    }


    public boolean isAdapterBlob() {
        return true;
    }

    public String getName() {
        return "AdapterBlob: " + super.getName();
    }

}
