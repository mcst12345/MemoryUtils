package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.classfile.ClassLoaderData;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.vmSymbols;
import miku.lib.jvm.hotspot.utilities.U2Array;
import one.helfy.Type;

public class InstanceKlass extends Klass {

    private static final int ACCESS_FLAGS_OFFSET;
    private static final int NAME_INDEX_OFFSET;
    private static final int SIGNATURE_INDEX_OFFSET;
    private static final int INITVAL_INDEX_OFFSET;
    private static final int LOW_OFFSET;
    private static final int HIGH_OFFSET;
    private static final int FIELD_SLOTS;
    private static final short FIELDINFO_TAG_SIZE;
    private static final short FIELDINFO_TAG_MASK;
    private static final short FIELDINFO_TAG_OFFSET;
    private static final int CLASS_STATE_ALLOCATED;
    private static final int CLASS_STATE_LOADED;
    private static final int CLASS_STATE_LINKED;
    private static final int CLASS_STATE_BEING_INITIALIZED;
    private static final int CLASS_STATE_FULLY_INITIALIZED;
    private static final int CLASS_STATE_INITIALIZATION_ERROR;

    private static final long _class_loader_data_offset;
    private static final long _fields_offset;
    private static final long _java_fields_count_offset;
    private static final long _methods_offset;
    private static final long _array_klasses_offset;
    private static final long _method_ordering_offset;
    private static final long _local_interfaces_offset;
    private static final long _transitive_interfaces_offset;
    private static final long _constants_offset;
    private static final long _source_debug_extension_offset;
    private static final long _inner_classes_offset;
    private static final long _source_file_name_index_offset;
    private static final long _nonstatic_field_size_offset;
    private static final long _static_field_size_offset;
    private static final long _static_oop_field_count_offset;
    private static final long _nonstatic_oop_map_size_offset;
    private static final long _is_marked_dependent_offset;
    private static final long _init_state_offset;
    private static final long _vtable_len_offset;
    private static final long _itable_len_offset;
    private static final long _breakpoints_offset;
    private static final long _generic_signature_index_offset;
    private static final long _major_version_offset;
    private static final long _minor_version_offset;

    private static final long headerSize;

    static {
        ACCESS_FLAGS_OFFSET = jvm.intConstant("FieldInfo::access_flags_offset");
        NAME_INDEX_OFFSET = jvm.intConstant("FieldInfo::name_index_offset");
        SIGNATURE_INDEX_OFFSET = jvm.intConstant("FieldInfo::signature_index_offset");
        INITVAL_INDEX_OFFSET = jvm.intConstant("FieldInfo::initval_index_offset");
        LOW_OFFSET = jvm.intConstant("FieldInfo::low_packed_offset");
        HIGH_OFFSET = jvm.intConstant("FieldInfo::high_packed_offset");
        FIELD_SLOTS = jvm.intConstant("FieldInfo::field_slots");
        FIELDINFO_TAG_SIZE = (short) jvm.intConstant("FIELDINFO_TAG_SIZE");
        FIELDINFO_TAG_MASK = (short) jvm.intConstant("FIELDINFO_TAG_MASK");
        FIELDINFO_TAG_OFFSET = (short) jvm.intConstant("FIELDINFO_TAG_OFFSET");
        CLASS_STATE_ALLOCATED = jvm.intConstant("InstanceKlass::allocated");
        CLASS_STATE_LOADED = jvm.intConstant("InstanceKlass::loaded");
        CLASS_STATE_LINKED = jvm.intConstant("InstanceKlass::linked");
        CLASS_STATE_BEING_INITIALIZED = jvm.intConstant("InstanceKlass::being_initialized");
        CLASS_STATE_FULLY_INITIALIZED = jvm.intConstant("InstanceKlass::fully_initialized");
        CLASS_STATE_INITIALIZATION_ERROR = jvm.intConstant("InstanceKlass::initialization_error");

        Type type = jvm.type("InstanceKlass");
        headerSize = Oop.alignObjectSize(type.size);
        _class_loader_data_offset = type.offset("_class_loader_data");
        _fields_offset = type.offset("_fields");
        _java_fields_count_offset = type.offset("_java_fields_count");
        _methods_offset = type.offset("_methods");
        _array_klasses_offset = type.offset("_array_klasses");
        _method_ordering_offset = type.offset("_method_ordering");
        _local_interfaces_offset = type.offset("_local_interfaces");
        _transitive_interfaces_offset = type.offset("_transitive_interfaces");
        _constants_offset = type.offset("_constants");
        _source_debug_extension_offset = type.offset("_source_debug_extension");
        _inner_classes_offset = type.offset("_inner_classes");
        _source_file_name_index_offset = type.offset("_source_file_name_index");
        _nonstatic_field_size_offset = type.offset("_nonstatic_field_size");
        _static_field_size_offset = type.offset("_static_field_size");
        _static_oop_field_count_offset = type.offset("_static_oop_field_count");
        _nonstatic_oop_map_size_offset = type.offset("_nonstatic_oop_map_size");
        _is_marked_dependent_offset = type.offset("_is_marked_dependent");
        _init_state_offset = type.offset("_init_state");
        _vtable_len_offset = type.offset("_vtable_len");
        _itable_len_offset = type.offset("_itable_len");
        _breakpoints_offset = type.offset("_breakpoints");
        _generic_signature_index_offset = type.offset("_generic_signature_index");
        _major_version_offset = type.offset("_major_version");
        _minor_version_offset = type.offset("_minor_version");

    }

    private ClassLoaderData _class_loader_data;
    private U2Array _fields;
    private int _java_fields_count;

    public InstanceKlass(long address) {
        super(address);
        Type type = jvm.type("InstanceKlass");
        _class_loader_data = new ClassLoaderData(unsafe.getAddress(address + _class_loader_data_offset));
        _fields = new U2Array(unsafe.getAddress(address + _fields_offset));
        _java_fields_count = unsafe.getInt(address + _java_fields_count_offset);
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
    public U2Array getInnerClasses(){
        return new U2Array(getAddress() + _inner_classes_offset);
    }

    public int getFieldOffset(int index) {
        U2Array fields = this.getFields();
        short lo = fields.at(index * FIELD_SLOTS + LOW_OFFSET);
        short hi = fields.at(index * FIELD_SLOTS + HIGH_OFFSET);
        if ((lo & FIELDINFO_TAG_MASK) == FIELDINFO_TAG_OFFSET) {
            return VM.buildIntFromShorts(lo, hi) >> FIELDINFO_TAG_SIZE;
        } else {
            throw new RuntimeException("should not reach here");
        }
    }

    public short getFieldAccessFlags(int index) {
        return this.getFields().at(index * FIELD_SLOTS + ACCESS_FLAGS_OFFSET);
    }

    public short getFieldNameIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        }
    }

    public Symbol getFieldName(int index) {
        int nameIndex = this.getFields().at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        return index < this.getJavaFieldsCount() ? this.getConstants().getSymbolAt(nameIndex) : vmSymbols.symbolAt(nameIndex);
    }

    public ConstantPool getConstants(){
        return new ConstantPool(getAddress() + _constants_offset);
    }

    public int getAllFieldsCount() {
        int len = this.getFields().length();

        int allFieldsCount;
        for(allFieldsCount = 0; allFieldsCount * FIELD_SLOTS < len; ++allFieldsCount) {
            short flags = this.getFieldAccessFlags(allFieldsCount);
            if ((((long) flags) & 2048L) != 0L) {
                --len;
            }
        }

        return allFieldsCount;
    }

    public Symbol getSourceFileName() {
        System.out.println(unsafe.getInt(getAddress() + _source_file_name_index_offset));
        return this.getConstants().getSymbolAt(unsafe.getShort(getAddress() + _source_file_name_index_offset));
    }

    public short getFieldSignatureIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + SIGNATURE_INDEX_OFFSET);
        }
    }

    public Symbol getFieldSignature(int index) {
        int signatureIndex = this.getFields().at(index * FIELD_SLOTS + SIGNATURE_INDEX_OFFSET);
        return index < this.getJavaFieldsCount() ? this.getConstants().getSymbolAt((long)signatureIndex) : vmSymbols.symbolAt(signatureIndex);
    }

    public short getFieldGenericSignatureIndex(int index) {
        int allFieldsCount = this.getAllFieldsCount();
        int generic_signature_slot = allFieldsCount * FIELD_SLOTS;

        for(int i = 0; i < allFieldsCount; ++i) {
            short flags = this.getFieldAccessFlags(i);
            if (i == index) {
                if ((((long)flags) & 2048L) != 0L) {
                    return this.getFields().at(generic_signature_slot);
                }

                return 0;
            }

            if ((((long)flags) & 2048L) != 0L) {
                ++generic_signature_slot;
            }
        }

        return 0;
    }

    public Symbol getFieldGenericSignature(int index) {
        short genericSignatureIndex = this.getFieldGenericSignatureIndex(index);
        return genericSignatureIndex != 0 ? this.getConstants().getSymbolAt(genericSignatureIndex) : null;
    }

    public short getFieldInitialValueIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + INITVAL_INDEX_OFFSET);
        }
    }

    public int getNonstaticFieldSize() {
        return unsafe.getInt(getAddress() + _nonstatic_field_size_offset);
    }
}
