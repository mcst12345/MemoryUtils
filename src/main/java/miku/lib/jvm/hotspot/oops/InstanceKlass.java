package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.classfile.ClassLoaderData;
import miku.lib.jvm.hotspot.utilities.U2Array;
import one.helfy.Type;

public class InstanceKlass extends Klass {
    private static int FIELD_SLOTS;
    private static int NAME_INDEX_OFFSET;

    static {
        FIELD_SLOTS = jvm.intConstant("FieldInfo::field_slots");
        NAME_INDEX_OFFSET = jvm.intConstant("FieldInfo::name_index_offset");
    }

    private ClassLoaderData _class_loader_data;
    private U2Array _fields;
    private int _java_fields_count;

    InstanceKlass(long address) {
        super(address);
        Type type = jvm.type("InstanceKlass");
        long offset = type.offset("_class_loader_data");
        _class_loader_data = new ClassLoaderData(unsafe.getAddress(address + offset));
        _fields = new U2Array(unsafe.getAddress(address + type.offset("_fields")));
        _java_fields_count = unsafe.getInt(address + type.offset("_java_fields_count"));
    }

    public InstanceKlass(Class<?> clazz) {
        this(jvm.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL);
    }

    public int getJavaFieldsCount() {
        return _java_fields_count;
    }

    public ClassLoaderData getClassLoaderData() {
        return _class_loader_data;
    }

    public U2Array getFields() {
        return _fields;
    }
}
