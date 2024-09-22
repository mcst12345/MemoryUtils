package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.classfile.ClassLoaderData;
import miku.lib.jvm.hotspot.runtime.VM;
import miku.lib.jvm.hotspot.runtime.vmSymbols;
import miku.lib.jvm.hotspot.utilities.KlassArray;
import miku.lib.jvm.hotspot.utilities.MethodArray;
import miku.lib.jvm.hotspot.utilities.U2Array;
import one.helfy.JVM;
import one.helfy.Type;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

//InstanceKlass extends Klass @ 440
//  ClassLoaderData* _class_loader_data @ 144
//  Annotations* _annotations @ 200
//  Klass* _array_klasses @ 208
//  ConstantPool* _constants @ 216
//  Array<jushort>* _inner_classes @ 224
//  char* _source_debug_extension @ 232
//  int _nonstatic_field_size @ 248
//  int _static_field_size @ 252
//  u2 _generic_signature_index @ 256
//  u2 _source_file_name_index @ 258
//  u2 _static_oop_field_count @ 260
//  u2 _java_fields_count @ 262
//  int _nonstatic_oop_map_size @ 264
//  bool _is_marked_dependent @ 268
//  u2 _minor_version @ 274
//  u2 _major_version @ 276
//  Thread* _init_thread @ 280
//  int _vtable_len @ 288
//  int _itable_len @ 292
//  OopMapCache* _oop_map_cache @ 296
//  JNIid* _jni_ids @ 312
//  jmethodID* _methods_jmethod_ids @ 320
//  nmethodBucket* _dependencies @ 328
//  nmethod* _osr_nmethods_head @ 336
//  BreakpointInfo* _breakpoints @ 344
//  u2 _idnum_allocated_count @ 368
//  u1 _init_state @ 370
//  u1 _reference_type @ 371
//  Array<Method*>* _methods @ 384
//  Array<Method*>* _default_methods @ 392
//  Array<Klass*>* _local_interfaces @ 400
//  Array<Klass*>* _transitive_interfaces @ 408
//  Array<int>* _method_ordering @ 416
//  Array<int>* _default_vtable_indices @ 424
//  Array<u2>* _fields @ 432

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

    private static final long _dependencies_offset;
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
    private static final long _oop_map_cache_offset;
    private static final long _default_vtable_indices_offset;
    private static final long _osr_nmethods_head_offset;
    private static final long _default_methods_offset;
    private static final long _methods_jmethod_ids_offset;
    private static final long _annotations_offset;

    private static final long headerSize;

    static {
        ACCESS_FLAGS_OFFSET = JVM.intConstant("FieldInfo::access_flags_offset");
        NAME_INDEX_OFFSET = JVM.intConstant("FieldInfo::name_index_offset");
        SIGNATURE_INDEX_OFFSET = JVM.intConstant("FieldInfo::signature_index_offset");
        INITVAL_INDEX_OFFSET = JVM.intConstant("FieldInfo::initval_index_offset");
        LOW_OFFSET = JVM.intConstant("FieldInfo::low_packed_offset");
        HIGH_OFFSET = JVM.intConstant("FieldInfo::high_packed_offset");
        FIELD_SLOTS = JVM.intConstant("FieldInfo::field_slots");
        FIELDINFO_TAG_SIZE = (short) JVM.intConstant("FIELDINFO_TAG_SIZE");
        FIELDINFO_TAG_MASK = (short) JVM.intConstant("FIELDINFO_TAG_MASK");
        FIELDINFO_TAG_OFFSET = (short) JVM.intConstant("FIELDINFO_TAG_OFFSET");
        CLASS_STATE_ALLOCATED = JVM.intConstant("InstanceKlass::allocated");
        CLASS_STATE_LOADED = JVM.intConstant("InstanceKlass::loaded");
        CLASS_STATE_LINKED = JVM.intConstant("InstanceKlass::linked");
        CLASS_STATE_BEING_INITIALIZED = JVM.intConstant("InstanceKlass::being_initialized");
        CLASS_STATE_FULLY_INITIALIZED = JVM.intConstant("InstanceKlass::fully_initialized");
        CLASS_STATE_INITIALIZATION_ERROR = JVM.intConstant("InstanceKlass::initialization_error");

        Type type = JVM.type("InstanceKlass");
        headerSize = Oop.alignObjectSize(type.size);
        _osr_nmethods_head_offset = type.offset("_osr_nmethods_head");
        _dependencies_offset = type.offset("_dependencies");
        _default_vtable_indices_offset = type.offset("_default_vtable_indices");
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
        _oop_map_cache_offset = type.offset("_oop_map_cache");
        _default_methods_offset = type.offset("_default_methods");
        _methods_jmethod_ids_offset = type.offset("_methods_jmethod_ids");
        _annotations_offset = type.offset("_annotations");
    }

    private ClassLoaderData _class_loader_data;
    private U2Array _fields;

    public InstanceKlass(long address) {
        super(address);
        _class_loader_data = new ClassLoaderData(unsafe.getAddress(address + _class_loader_data_offset));
        _fields = new U2Array(unsafe.getAddress(address + _fields_offset));
    }

    public InstanceKlass(Class<?> clazz) {
        this(JVM.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL);
    }

    public short getJavaFieldsCount() {
        return unsafe.getShort(getAddress() + _java_fields_count_offset);
    }

    public void  setJavaFieldsCount(short neo) {
        unsafe.putShort(getAddress() + _java_fields_count_offset,neo);
    }

    public ClassLoaderData getClassLoaderData() {
        return _class_loader_data;
    }

    public U2Array getFields() {
        return _fields;
    }

    public void setFields(U2Array neo){
        this._fields = neo;
        unsafe.putAddress(getAddress() + _fields_offset,neo.getAddress());
    }

    public U2Array getInnerClasses(){
        return new U2Array(unsafe.getAddress(getAddress() + _inner_classes_offset));
    }

    public void setInnerClasses(U2Array neo){
        unsafe.putAddress(getAddress() + _inner_classes_offset,neo.getAddress());
    }

    public int getNonstaticFieldSize(){
        return unsafe.getInt(getAddress() + _nonstatic_field_size_offset);
    }

    public void setNonstaticFieldSize(int neo){
        unsafe.putInt(getAddress() + _nonstatic_field_size_offset,neo);
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

    public MethodArray getMethods(){
        return new MethodArray(unsafe.getAddress(getAddress() + _methods_offset));
    }

    public short getFieldNameIndex(int index) {
        if (index >= this.getJavaFieldsCount()) {
            throw new IndexOutOfBoundsException("not a Java field;");
        } else {
            return this.getFields().at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        }
    }

    public BreakpointInfo getBreakpoints(){
        long address = unsafe.getAddress(getAddress() + _breakpoints_offset);
        return address == 0 ? null : new BreakpointInfo(address);
    }

    public void setBreakpoints(BreakpointInfo bp){
        unsafe.putAddress(getAddress() + _breakpoints_offset,bp.getAddress());
    }

    public Symbol getFieldName(int index) {
        int nameIndex = this.getFields().at(index * FIELD_SLOTS + NAME_INDEX_OFFSET);
        return index < this.getJavaFieldsCount() ? this.getConstants().getSymbolAt(nameIndex) : vmSymbols.symbolAt(nameIndex);
    }

    public Method findMethod(Symbol name, Symbol sig) {
        return findMethod(this.getMethods(), name, sig);
    }

    private static Method findMethod(MethodArray methods, Symbol name,Symbol signature) {
        int len = methods.length();
        int l = 0;
        int h = len - 1;

        int mid;
        while(l <= h) {
            mid = l + h >> 1;
            Method m = methods.at(mid);
            int res = m.getName().fastCompare(name);
            if (res == 0) {
                if (m.getSignature().equals(signature)) {
                    return m;
                }

                int i;
                Method m1;
                for(i = mid - 1; i >= l; --i) {
                    m1 = methods.at(i);
                    if (!m1.getName().equals(name)) {
                        break;
                    }

                    if (m1.getSignature().equals(signature)) {
                        return m1;
                    }
                }

                for(i = mid + 1; i <= h; ++i) {
                    m1 = methods.at(i);
                    if (!m1.getName().equals(name)) {
                        break;
                    }

                    if (m1.getSignature().equals(signature)) {
                        return m1;
                    }
                }

                return null;
            }

            if (res < 0) {
                l = mid + 1;
            } else {
                h = mid - 1;
            }
        }

        return null;
    }

    public Field findInterfaceField(Symbol name, Symbol sig) {
        KlassArray interfaces = this.getLocalInterfaces();
        int n = interfaces.length();

        for(int i = 0; i < n; ++i) {
            InstanceKlass intf1 = (InstanceKlass)interfaces.getAt(i);

            Field f = intf1.findLocalField(name, sig);
            if (f != null) {

                return f;
            }

            f = intf1.findInterfaceField(name, sig);
            if (f != null) {
                return f;
            }
        }

        return null;
    }

    public Field findLocalField(Symbol name, Symbol sig) {
        int length = this.getJavaFieldsCount();

        for(int i = 0; i < length; ++i) {
            Symbol f_name = this.getFieldName(i);
            Symbol f_sig = this.getFieldSignature(i);
            if (name.equals(f_name) && sig.equals(f_sig)) {
                return this.newField(i);
            }
        }

        return null;
    }

    public Field findField(Symbol name, Symbol sig) {
        Field f = this.findLocalField(name, sig);
        if (f != null) {
            return f;
        } else {
            f = this.findInterfaceField(name, sig);
            if (f != null) {
                return f;
            } else {
                InstanceKlass supr = (InstanceKlass)this.getSuper();
                return supr != null ? supr.findField(name, sig) : null;
            }
        }
    }

    public ConstantPool getConstants(){
        return new ConstantPool(unsafe.getAddress(getAddress() + _constants_offset));
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

    public short getSourceFileNameIndex(){
        return unsafe.getShort(getAddress() + _source_file_name_index_offset);
    }

    public void setSourceFileNameIndex(short neo){
        unsafe.putShort(getAddress() + _source_file_name_index_offset,neo);
    }

    public Symbol getSourceFileName() {
        return this.getConstants().getSymbolAt(getSourceFileNameIndex());
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

    public short getStaticOopFieldCount(){
        return unsafe.getShort(getAddress() + _static_oop_field_count_offset);
    }

    public int getNonstaticOopMapSize(){
        return unsafe.getInt(getAddress() + _nonstatic_oop_map_size_offset);
    }

    public void setNonstaticOopMapSize(int neo){
        unsafe.putInt(getAddress() + _nonstatic_oop_map_size_offset,neo);
    }

    public boolean getIsMarkedDependent(){
        return unsafe.getByte(getAddress() + _is_marked_dependent_offset) != 0;
    }

    public int getVtableLen(){
        return unsafe.getInt(getAddress() + _vtable_len_offset);
    }

    public void setVtableLen(int neo){
        unsafe.putInt(getAddress() + _vtable_len_offset,neo);
    }
    public void setItableLen(int neo){
        unsafe.putInt(getAddress() + _itable_len_offset,neo);
    }

    public int getItableLen(){
        return unsafe.getInt(getAddress() + _itable_len_offset);
    }

    public short majorVersion(){
        return unsafe.getShort(getAddress() + _major_version_offset);
    }

    public short minorVersion(){
        return unsafe.getShort(getAddress() + _minor_version_offset);
    }

    public short getGenericSignatureIndex(){
        return unsafe.getShort(getAddress() + _generic_signature_index_offset);
    }

    public void setGenericSignatureIndex(short neo){
        unsafe.putShort(getAddress() + _generic_signature_index_offset,neo);
    }

    public Symbol getGenericSignature() {
        short index = getGenericSignatureIndex();
        return index != 0L ? this.getConstants().getSymbolAt(index) : null;
    }

    public long getSizeHelper() {
        int lh = this.getLayoutHelper();
        if(lh <= 0){
            throw new RuntimeException("layout helper initialized for instance class");
        }
        return (long)lh / unsafe.addressSize();
    }

    public Field[] getStaticFields(){
        U2Array fields = this.getFields();
        int length = this.getJavaFieldsCount();
        ArrayList<Field> result = new ArrayList<>();

        for(int index = 0; index < length; ++index) {
            Field f = this.newField(index);
            if (f.isStatic()) {
                result.add(f);
            }
        }

        return result.toArray(new Field[0]);
    }

    private Field newField(int index){
        FieldType type = new FieldType(getFieldSignature(index));
        if(type.isOop()){
            //return new
        }
        return null;
    }

    public void setLocalInterfaces(KlassArray neo){
        unsafe.putAddress(getAddress() + _local_interfaces_offset,neo.getAddress());
    }

    public KlassArray getLocalInterfaces(){
        return new KlassArray(unsafe.getAddress(getAddress() + _local_interfaces_offset));
    }

    public KlassArray getTransitiveInterfaces(){
        return new KlassArray(unsafe.getAddress(getAddress() + _transitive_interfaces_offset));
    }

    public void setTransitiveInterfaces(KlassArray neo){
        unsafe.putAddress(getAddress() + _transitive_interfaces_offset,neo.getAddress());
    }

    public long getObjectSize(Oop oop) {
        return getSizeHelper() * unsafe.addressSize();
    }

    public void redefineClass(byte[] clazz_bytes,ClassLoader cl){
        unsafe.ensureClassInitialized((Class<?>) getMirror().getObject());
        ClassReader cr = new ClassReader(clazz_bytes);
        final String[] name = new String[1];
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5) {
            @Override
            public void visit(int i, int i1, String s, String s1, String s2, String[] strings) {
                name[0] = s;
                super.visit(i, i1, s, s1, s2, strings);
            }
        };
        cr.accept(cv,0);
        if(!name[0].equals(this.getName())){
            throw new ClassFormatError("Class name not the same!");
        }

        cl = cl != null ? cl : (ClassLoader) getClassLoaderData().getClassLoader().getObject();
        ClassLoader finalCl = cl;
        ClassLoader new_cl = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                return finalCl.loadClass(name);
            }

            @Override
            public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                return finalCl.loadClass(name, resolve);
            }

            @Override
            public Class<?> findClass(String name) throws ClassNotFoundException {
                return finalCl.findClass(name);
            }

            @Override
            public Package[] getPackages() {
                return finalCl.getPackages();
            }

            @Override
            public Package getPackage(String name) {
                return finalCl.getPackage(name);
            }

            @Override
            public InputStream getResourceAsStream(String name) {
                return finalCl.getResourceAsStream(name);
            }

            @Override
            public Enumeration<URL> findResources(String name) throws IOException {
                return finalCl.findResources(name);
            }

            @Override
            public URL findResource(String name) {
                return finalCl.findResource(name);
            }

            @Override
            public Enumeration<URL> getResources(String name) throws IOException {
                return finalCl.getResources(name);
            }
        };
        Class<?> tmp = unsafe.defineClass(name[0].replace('/','.'),clazz_bytes,0,clazz_bytes.length,new_cl,((Class<?>)getMirror().getObject()).getProtectionDomain());
        unsafe.ensureClassInitialized(tmp);
        InstanceKlass neo = (InstanceKlass) Klass.getKlass(tmp);
        Map<String,Long> pointers = new HashMap<>();
        InstanceKlass oldK = this;
        MethodArray methods = oldK.getMethods();
        for(int i = 0 ; i < methods.length();i++){
            miku.lib.jvm.hotspot.oops.Method method = methods.at(i);
            method.clear_all_breakpoints();
            if(method.getNativeMethod() != null){
                method.getNativeMethod().setMarkedForDeoptimization();
            }
            if(method.getAccessFlags().isNative()){
                pointers.put(method.getName().toString(),method.getCODE());
            }
        }
        neo.setClassLoaderData(oldK.getClassLoaderData());
        methods = neo.getMethods();
        for(int i = 0; i < methods.length(); i++){
            miku.lib.jvm.hotspot.oops.Method m = methods.at(i);
            String m_name = m.getName().toString();
            if(pointers.containsKey(m_name)){
                m.setCODE(pointers.get(m_name));
            }
            m.getMethodData();
            m.getConstMethod().getConstants().setPoolHolder(oldK);
        }
        ConstantPool old_const = oldK.getConstants();
        neo.getConstants().setPoolHolder(oldK);
        neo.setConstants(old_const);
        MethodArray old_methods = oldK.getMethods();
        oldK.setMethods(neo.getMethods());
        neo.setMethods(old_methods);
        oldK.setConstants(neo.getConstants());
        oldK.setFields(neo.getFields());
        oldK.setJavaFieldsCount(neo.getJavaFieldsCount());
        oldK.setVtableLen(neo.getVtableLen());
        oldK.setItableLen(neo.getItableLen());
        oldK.setAccessFlags(neo.getAccessFlags().getFlags());
        oldK.setNonstaticFieldSize(neo.getNonstaticFieldSize());
        oldK.setNonstaticOopMapSize(neo.getNonstaticOopMapSize());
        oldK.setLocalInterfaces(neo.getLocalInterfaces());
        oldK.setSourceFileNameIndex(neo.getSourceFileNameIndex());
        oldK.setGenericSignatureIndex(neo.getGenericSignatureIndex());
        oldK.setInnerClasses(neo.getInnerClasses());
        unsafe.putAddress(getAddress() + _methods_jmethod_ids_offset, unsafe.getAddress(neo.getAddress() + _methods_jmethod_ids_offset) );
        oldK.setTransitiveInterfaces(neo.getTransitiveInterfaces());
        unsafe.putAddress(getAddress() + _oop_map_cache_offset, unsafe.getAddress(neo.getAddress() + _oop_map_cache_offset) );
        unsafe.putAddress(getAddress() + _default_vtable_indices_offset, unsafe.getAddress(neo.getAddress() + _default_vtable_indices_offset) );
        unsafe.putAddress(getAddress() + _dependencies_offset, unsafe.getAddress(neo.getAddress() + _dependencies_offset) );
        unsafe.putAddress(getAddress() + _osr_nmethods_head_offset, unsafe.getAddress(neo.getAddress() + _osr_nmethods_head_offset) );
        unsafe.putAddress(getAddress() + _method_ordering_offset, unsafe.getAddress(neo.getAddress() + _method_ordering_offset) );
        unsafe.putAddress(getAddress() + _default_methods_offset, unsafe.getAddress(neo.getAddress() + _default_methods_offset) );
        unsafe.putAddress(getAddress() + _source_debug_extension_offset, unsafe.getAddress(neo.getAddress() + _source_debug_extension_offset) );
        unsafe.putAddress(getAddress() + _annotations_offset, unsafe.getAddress(neo.getAddress() + _annotations_offset) );

    }

    public void setClassLoaderData(ClassLoaderData classLoaderData) {
        this._class_loader_data = classLoaderData;
        unsafe.putAddress(getAddress() + _class_loader_data_offset,classLoaderData.getAddress());
    }

    public void setConstants(ConstantPool cp){
        unsafe.putAddress(getAddress() + _constants_offset,cp.getAddress());
    }


    public void setMethods(MethodArray methods){
        unsafe.putAddress(getAddress() + _methods_offset,methods.getAddress());
    }
}
