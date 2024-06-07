package miku.lib.jvm.hotspot.oops;

public class Field {
    private long offset;
    private FieldIdentifier id;
    private boolean isVMField;
    private InstanceKlass holder;
    private FieldType fieldType;
    private Symbol signature;
    private Symbol genericSignature;
    private AccessFlags accessFlags;
    private int fieldIndex;

    Field(FieldIdentifier id, long offset, boolean isVMField) {
        this.offset = offset;
        this.id = id;
        this.isVMField = isVMField;
    }
}
