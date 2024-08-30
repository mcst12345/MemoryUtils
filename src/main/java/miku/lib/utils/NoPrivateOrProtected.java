package miku.lib.utils;

import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.tools.jcore.ClassWriter;
import one.helfy.JVM;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NoPrivateOrProtected implements Opcodes {

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    private static int makePublic(int access){
        if(Modifier.isProtected(access)){
            access = access & ~ACC_PROTECTED;
        }
        if(Modifier.isPrivate(access)){
            access = access & ~ACC_PRIVATE;
        }
        if(Modifier.isFinal(access)){
            access = access & ~ACC_FINAL;
        }
        if(Modifier.isTransient(access)){
            access = access & ~ACC_TRANSIENT;
        }
        if(!Modifier.isPublic(access)){
            access |= ACC_PUBLIC;
        }
        return access;
    }

    public static void FuckAccess(Class<?> cls){
        InternalUtils.getUnsafe().ensureClassInitialized(cls);

        long _fields = JVM.type("InstanceKlass").offset("_fields");
        long _java_fields_count = JVM.type("InstanceKlass").offset("_java_fields_count");
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long _access_flags_offset = JVM.type("Method").offset("_access_flags");
        long klass = oopSize == 8 ? unsafe.getLong(cls, klassOffset) : unsafe.getInt(cls, klassOffset) & 0xffffffffL;
        int access = unsafe.getInt(klass + JVM.type("Klass").offset("_access_flags"));
        unsafe.putInt(klass + JVM.type("Klass").offset("_access_flags"), makePublic(access));
        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");
        for (int i = 0; i < methodCount; i++) {
            long method = JVM.getAddress(methods + (long) i * oopSize);
            int old = unsafe.getShort(method + _access_flags_offset);
            int neo = makePublic(old);
            unsafe.putInt(method+_access_flags_offset, neo);
        }
        long fields = unsafe.getAddress(klass + _fields);
        int fieldCount = unsafe.getShort(klass+_java_fields_count);
        for (int i = 0; i < fieldCount; i++) {
            long field = fields + 4 + i * 2 * 6;
            int old = unsafe.getShort(field);
            int neo = Opcodes.ACC_PUBLIC;
            if (Modifier.isStatic(old)) {
                neo |= Opcodes.ACC_STATIC;
            }
            if (Modifier.isVolatile(old)) {
                neo |= Opcodes.ACC_VOLATILE;
            }
            unsafe.putShort(field, (short) neo);
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            new ClassWriter((InstanceKlass) Klass.getKlass(cls),bos).write();
            byte[] data = bos.toByteArray();
            String name = cls.getName().replace('.','/')+"Fucked.class";
            Path path = Paths.get(name);
            if(!Files.exists(path)){
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }
            Files.write(path,data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
