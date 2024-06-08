import miku.lib.InternalUtils;
import one.helfy.JVM;
import sun.misc.Unsafe;

public class Test {

    //[ClassLoaderData* _class_loader_data @ 144, Annotations* _annotations @ 200, Klass* _array_klasses @ 208, ConstantPool* _constants @ 216, Array<jushort>* _inner_classes @ 224, char* _source_debug_extension @ 232, int _nonstatic_field_size @ 248, int _static_field_size @ 252, u2 _generic_signature_index @ 256, u2 _source_file_name_index @ 258, u2 _static_oop_field_count @ 260, u2 _java_fields_count @ 262, int _nonstatic_oop_map_size @ 264, bool _is_marked_dependent @ 268, u2 _minor_version @ 274, u2 _major_version @ 276, Thread* _init_thread @ 280, int _vtable_len @ 288, int _itable_len @ 292, OopMapCache* _oop_map_cache @ 296, JNIid* _jni_ids @ 312, jmethodID* _methods_jmethod_ids @ 320, nmethodBucket* _dependencies @ 328, nmethod* _osr_nmethods_head @ 336, BreakpointInfo* _breakpoints @ 344, u2 _idnum_allocated_count @ 368, u1 _init_state @ 370, u1 _reference_type @ 371, Array<Method*>* _methods @ 384, Array<Method*>* _default_methods @ 392, Array<Klass*>* _local_interfaces @ 400, Array<Klass*>* _transitive_interfaces @ 408, Array<int>* _method_ordering @ 416, Array<int>* _default_vtable_indices @ 424, Array<u2>* _fields @ 432]

    private static void printHex(long hex){
        System.out.println("0x"+Long.toHexString(hex));
    }

    private static void exit(){
        Runtime.getRuntime().exit(0);
    }

    public static void main(String[] args) {
        //System.out.println(JVM.SymbolOffset("??_7InstanceKlass@@6B@"));
        //exit();
        try {
            long addr = getKlassAddr(Object.class);
            System.out.println(jvm.findDynamicTypeForAddress(addr,jvm.type("Metadata")).name);
            addr = getKlassAddr(Class.class);
            System.out.println(jvm.findDynamicTypeForAddress(addr,jvm.type("Metadata")).name);
            addr = getKlassAddr(Class[].class);
            System.out.println(jvm.findDynamicTypeForAddress(addr,jvm.type("Metadata")).name);
            addr = getKlassAddr(ClassLoader.class);
            System.out.println(jvm.findDynamicTypeForAddress(addr,jvm.type("Metadata")).name);
            addr = getKlassAddr(int[].class);
            System.out.println(jvm.findDynamicTypeForAddress(addr,jvm.type("Metadata")).name);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final Unsafe unsafe = InternalUtils.getUnsafe();
    private static final JVM jvm = JVM.getInstance();

    private static long getKlassAddr(Class<?> target){
        return jvm.intConstant("oopSize") == 8 ? unsafe.getLong(target, (long) jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(target, jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL;
    }

    private static class A {
    }

    private static class Static {
    }

    private class NotStatic {
    }
}
