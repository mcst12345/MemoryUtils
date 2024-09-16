package me.xdark.shell;

import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

import java.nio.charset.StandardCharsets;

public final class ShellcodeRunner {
    public static final JVM jvm = JVM.getInstance();


    private ShellcodeRunner() {
    }

    public static void main(String[] args) throws Throwable {
        System.out.println(JVM.intConstant("oopSize"));
    }

    public static void inject(Class<?> target, String name, String descriptor, byte[] payload) {
        // Before execution: prepare the method to match the exact size of payload you want to execute
        //  it ~~20000 times let JIT do it's work
        // After injection call the method again
        Unsafe unsafe = InternalUtils.getUnsafe();
        JVM jvm = ShellcodeRunner.jvm;
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(target, klassOffset)
                : unsafe.getInt(target, klassOffset) & 0xffffffffL;

        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");

        long constMethodOffset = JVM.type("Method").offset("_constMethod");
        Type constMethodType = JVM.type("ConstMethod");
        Type constantPoolType = JVM.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = JVM.type("Method").offset("_from_compiled_entry");

        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            long constMethod = JVM.getAddress(method + constMethodOffset);

            long constantPool = JVM.getAddress(constMethod + constantPoolOffset);
            int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;

            if (name.equals(getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize))
                    && descriptor.equals(getSymbol(
                    constantPool + constantPoolType.size + (long) signatureIndex * oopSize))) {
                long address = JVM.getAddress(method + _from_compiled_entry);
                // let's rock!
                for (int j = 0, k = payload.length; j < k; j++) {
                    unsafe.putByte(address + j, payload[j]);
                }
                return;
            }
        }
        throw new InternalError(target + "." + name + descriptor);
    }

    public static byte[] print(Class<?> target, String name, String descriptor, int size) {
        byte[] ret = new byte[size];
        // Before execution: prepare the method to match the exact size of payload you want to execute
        //  it ~~20000 times let JIT do it's work
        // After injection call the method again
        Unsafe unsafe = InternalUtils.getUnsafe();
        JVM jvm = ShellcodeRunner.jvm;
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(target, klassOffset)
                : unsafe.getInt(target, klassOffset) & 0xffffffffL;

        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");

        long constMethodOffset = JVM.type("Method").offset("_constMethod");
        Type constMethodType = JVM.type("ConstMethod");
        Type constantPoolType = JVM.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = JVM.type("Method").offset("_from_compiled_entry");

        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            long constMethod = JVM.getAddress(method + constMethodOffset);

            long constantPool = JVM.getAddress(constMethod + constantPoolOffset);
            int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;

            if (name.equals(getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize))
                    && descriptor.equals(getSymbol(
                    constantPool + constantPoolType.size + (long) signatureIndex * oopSize))) {
                long address = JVM.getAddress(method + _from_compiled_entry);
                // let's rock!
                for (int j = 0; j < size; j++) {
                    ret[i] = unsafe.getByte(address + j);
                    System.out.println(unsafe.getByte(address + j));
                }
                return ret;
            }
        }
        throw new InternalError(target + "." + name + descriptor);
    }

    public static long GetCompiledEntry(Class<?> target, String name, String descriptor) {
        // Before execution: prepare the method to match the exact size of payload you want to execute
        //  it ~~20000 times let JIT do it's work
        // After injection call the method again
        Unsafe unsafe = InternalUtils.getUnsafe();
        JVM jvm = ShellcodeRunner.jvm;
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(target, klassOffset)
                : unsafe.getInt(target, klassOffset) & 0xffffffffL;

        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");

        long constMethodOffset = JVM.type("Method").offset("_constMethod");
        Type constMethodType = JVM.type("ConstMethod");
        Type constantPoolType = JVM.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = JVM.type("Method").offset("_from_compiled_entry");

        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            long constMethod = JVM.getAddress(method + constMethodOffset);

            long constantPool = JVM.getAddress(constMethod + constantPoolOffset);
            int nameIndex = jvm.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = jvm.getShort(constMethod + signatureIndexOffset) & 0xffff;

            if (name.equals(getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize))
                    && descriptor.equals(getSymbol(
                    constantPool + constantPoolType.size + (long) signatureIndex * oopSize))) {
                return JVM.getAddress(method + _from_compiled_entry);
            }
        }
        throw new InternalError(target + "." + name + descriptor);
    }

    public static void SetCompiledEntry(Class<?> target, String name, String descriptor, long neo) {
        // Before execution: prepare the method to match the exact size of payload you want to execute
        //  it ~~20000 times let JIT do it's work
        // After injection call the method again
        Unsafe unsafe = InternalUtils.getUnsafe();
        JVM jvm = ShellcodeRunner.jvm;
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLong(target, klassOffset)
                : unsafe.getInt(target, klassOffset) & 0xffffffffL;

        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");

        long constMethodOffset = JVM.type("Method").offset("_constMethod");
        Type constMethodType = JVM.type("ConstMethod");
        Type constantPoolType = JVM.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        long _from_compiled_entry = JVM.type("Method").offset("_from_compiled_entry");

        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            long constMethod = JVM.getAddress(method + constMethodOffset);

            long constantPool = JVM.getAddress(constMethod + constantPoolOffset);
            int nameIndex = JVM.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = JVM.getShort(constMethod + signatureIndexOffset) & 0xffff;

            if (name.equals(getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize))
                    && descriptor.equals(getSymbol(
                    constantPool + constantPoolType.size + (long) signatureIndex * oopSize))) {
                InternalUtils.getUnsafe().putAddress(method + _from_compiled_entry, neo);
                return;
            }
        }
        throw new InternalError(target + "." + name + descriptor);
    }

    public static String getSymbol(long symbolAddress) {
        Type symbolType = JVM.type("Symbol");
        long symbol = JVM.getAddress(symbolAddress);
        long body = symbol + symbolType.offset("_body");
        int length = JVM.getShort(symbol + symbolType.offset("_length")) & 0xffff;

        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = JVM.getByte(body + i);
        }
        return new String(b, StandardCharsets.UTF_8);
    }
}
