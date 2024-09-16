package miku.lib.utils.memory;

import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.ProtectionDomain;

public class NativeMemoryHelper implements MemoryHelper {

    static {
        unsafeInit();
    }

    private static String getSymbol(long symbolAddress) {
        Unsafe unsafe = InternalUtils.getUnsafe();
        Type symbolType = JVM.type("Symbol");
        long symbol = unsafe.getAddress(symbolAddress);
        long body = symbol + symbolType.offset("_body");
        int length = unsafe.getShort(symbol + symbolType.offset("_length")) & 0xffff;

        byte[] b = new byte[length];
        for (int i = 0; i < length; i++) {
            b[i] = unsafe.getByte(body + i);
        }
        return new String(b, StandardCharsets.UTF_8);
    }

    private static void nativeInit(){

    }

    private static void unsafeInit() {
        Unsafe unsafe = InternalUtils.getUnsafe();
        int oopSize = JVM.intConstant("oopSize");
        long klassOffset = JVM.getInt(JVM.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8
                ? unsafe.getLongVolatile(NativeMemoryHelper.class, klassOffset)
                : unsafe.getIntVolatile(NativeMemoryHelper.class, klassOffset) & 0xffffffffL;
        long methodArray = JVM.getAddress(klass + JVM.type("InstanceKlass").offset("_methods"));
        int methodCount = JVM.getInt(methodArray);
        long methods = methodArray + JVM.type("Array<Method*>").offset("_data");
        long size = JVM.type("Method").size;
        long constMethodOffset = JVM.type("Method").offset("_constMethod");
        Type constMethodType = JVM.type("ConstMethod");
        Type constantPoolType = JVM.type("ConstantPool");
        long constantPoolOffset = constMethodType.offset("_constants");
        long nameIndexOffset = constMethodType.offset("_name_index");
        long signatureIndexOffset = constMethodType.offset("_signature_index");
        for(int i = 0; i < methodCount;i ++){
            long method = unsafe.getAddress(methods + (long) i * oopSize);
            long constMethod = unsafe.getAddress(method + constMethodOffset);
            long constantPool = unsafe.getAddress(constMethod + constantPoolOffset);
            int nameIndex = unsafe.getShort(constMethod + nameIndexOffset) & 0xffff;
            int signatureIndex = unsafe.getShort(constMethod + signatureIndexOffset) & 0xffff;

            String name = getSymbol(constantPool + constantPoolType.size + (long) nameIndex * oopSize);
            String desc = getSymbol(constantPool + constantPoolType.size + (long) signatureIndex * oopSize);
            if (name.equals("getInt0") && desc.equals("(Ljava/lang/Object;J)I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetInt"));
            } else if (name.equals("putInt0") && desc.equals("(Ljava/lang/Object;JI)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetInt"));
            } else if (name.equals("getObject0") && desc.equals("(Ljava/lang/Object;J)Ljava/lang/Object;")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetObject"));
            } else if (name.equals("putObject0") && desc.equals("(Ljava/lang/Object;JLjava/lang/Object;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetObject"));
            } else if (name.equals("getBoolean0") && desc.equals("(Ljava/lang/Object;J)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetBoolean"));
            } else if (name.equals("setBoolean0") && desc.equals("(Ljava/lang/Object;JZ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetBoolean"));
            } else if (name.equals("getByte0") && desc.equals("(Ljava/lang/Object;J)B")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetByte"));
            } else if (name.equals("setByte0") && desc.equals("(Ljava/lang/Object;JB)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetByte"));
            } else if (name.equals("getShort0") && desc.equals("(Ljava/lang/Object;J)S")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetShort"));
            } else if (name.equals("putShort0") && desc.equals("(Ljava/lang/Object;JS)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetShort"));
            } else if (name.equals("getChar0") && desc.equals("(Ljava/lang/Object;J)C")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetChar"));
            } else if (name.equals("setChar0") && desc.equals("(Ljava/lang/Object;JC)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetChar"));
            } else if (name.equals("getLong0") && desc.equals("(Ljava/lang/Object;J)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetLong"));
            } else if (name.equals("putLong0") && desc.equals("(Ljava/lang/Object;JJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetLong"));
            } else if (name.equals("getLong0") && desc.equals("(J)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeLong"));
            } else if (name.equals("putLong0") && desc.equals("(JJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeLong"));
            } else if (name.equals("getInt0") && desc.equals("(J)I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeInt"));
            } else if (name.equals("putInt0") && desc.equals("(JI)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeInt"));
            } else if (name.equals("getFloat0") && desc.equals("(J)F")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeFloat"));
            } else if (name.equals("putFloat0") && desc.equals("(JF)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeFloat"));
            } else if (name.equals("getDouble0") && desc.equals("(J)D")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeDouble"));
            } else if (name.equals("putDouble0") && desc.equals("(JD)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeDouble"));
            } else if (name.equals("getByte0") && desc.equals("(J)B")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeByte"));
            } else if (name.equals("putByte0") && desc.equals("(JB)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeByte"));
            } else if (name.equals("getShort0") && desc.equals("(J)S")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeShort"));
            } else if (name.equals("putShort0") && desc.equals("(JS)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeShort"));
            } else if (name.equals("getChar0") && desc.equals("(J)C")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeChar"));
            } else if (name.equals("putChar0") && desc.equals("(JC)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeChar"));
            } else if (name.equals("getAddress0") && desc.equals("(J)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetNativeAddress"));
            } else if (name.equals("putAddress0") && desc.equals("(JJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetNativeAddress"));
            } else if (name.equals("allocateMemory0") && desc.equals("(J)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_AllocateMemory"));
            } else if (name.equals("reallocateMemory0") && desc.equals("(JJ)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_ReallocateMemory"));
            } else if (name.equals("setMemory0") && desc.equals("(Ljava/lang/Object;JJB)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetMemory"));
            } else if (name.equals("copyMemory0") && desc.equals("(Ljava/lang/Object;JLjava/lang/Object;JJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_CopyMemory"));
            } else if (name.equals("freeMemory0") && desc.equals("(J)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_FreeMemory"));
            } else if (name.equals("staticFieldOffset0") && desc.equals("(Ljava/lang/reflect/Field;)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_StaticFieldOffset"));
            } else if (name.equals("staticFieldBase0") && desc.equals("(Ljava/lang/reflect/Field;)Ljava/lang/Object;")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_StaticFieldBaseFromField"));
            } else if (name.equals("objectFieldOffset0") && desc.equals("(Ljava/lang/reflect/Field;)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_ObjectFieldOffset"));
            } else if (name.equals("shouldBeInitialized0") && desc.equals("(Ljava/lang/Class;)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_ShouldBeInitialized"));
            } else if (name.equals("ensureClassInitialized0") && desc.equals("(Ljava/lang/Class;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_EnsureClassInitialized"));
            } else if (name.equals("arrayBaseOffset0") && desc.equals("(Ljava/lang/Class;)I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_ArrayBaseOffset"));
            } else if (name.equals("arrayIndexScale0") && desc.equals("(Ljava/lang/Class;)I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_ArrayIndexScale"));
            } else if (name.equals("addressSize0") && desc.equals("()I)")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_AddressSize"));
            } else if (name.equals("pageSize0") && desc.equals("()I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_PageSize"));
            } else if (name.equals("defineClass0") && desc.equals("(Ljava/lang/String;[BIILjava/lang/ClassLoader;Ljava/security/ProtectionDomain;)Ljava/lang/Class;")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_DefineClass"));
            } else if (name.equals("defineAnonymousClass0") && desc.equals("(Ljava/lang/Class;[B[Ljava/lang/Object;)Ljava/lang/Class;")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_DefineAnonymousClass"));
            } else if (name.equals("allocateInstance0") && desc.equals("(Ljava/lang/Class;)Ljava/lang/Object;")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_AllocateInstance"));
            } else if (name.equals("monitorEnter0") && desc.equals("(Ljava/lang/Object;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_MonitorEnter"));
            } else if (name.equals("monitorExit0") && desc.equals("(Ljava/lang/Object;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_MonitorExit"));
            } else if (name.equals("tryMonitorEnter0") && desc.equals("(Ljava/lang/Object;)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_TryMonitorEnter"));
            } else if (name.equals("throwException0") && desc.equals("(Ljava/lang/Throwable;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_ThrowException"));
            } else if (name.equals("compareAndSwapObject0") && desc.equals("(Ljava/lang/Object;JLjava/lang/Object;Ljava/lang/Object;)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_CompareAndSwapObject"));
            } else if (name.equals("compareAndSwapInt0") && desc.equals("(Ljava/lang/Object;JII)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_CompareAndSwapInt"));
            } else if (name.equals("compareAndSwapLong0") && desc.equals("(Ljava/lang/Object;JJJ)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_CompareAndSwapLong"));
            } else if (name.equals("getIntVolatile0") && desc.equals("(Ljava/lang/Object;J)I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetIntVolatile"));
            } else if (name.equals("putIntVolatile0") && desc.equals("(Ljava/lang/Object;JI)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetIntVolatile"));
            } else if (name.equals("getObjectVolatile0") && desc.equals("(Ljava/lang/Object;J)Ljava/lang/Object;")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetObjectVolatile"));
            } else if (name.equals("putObjectVolatile0") && desc.equals("(Ljava/lang/Object;JLjava/lang/Object;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetObjectVolatile"));
            } else if (name.equals("getBooleanVolatile0") && desc.equals("(Ljava/lang/Object;J)Z")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetBooleanVolatile"));
            } else if (name.equals("setBooleanVolatile0") && desc.equals("(Ljava/lang/Object;JZ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetBooleanVolatile"));
            } else if (name.equals("getByteVolatile0") && desc.equals("(Ljava/lang/Object;J)B")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetByteVolatile"));
            } else if (name.equals("setByteVolatile0") && desc.equals("(Ljava/lang/Object;JB)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetByteVolatile"));
            } else if (name.equals("getShortVolatile0") && desc.equals("(Ljava/lang/Object;J)S")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetShortVolatile"));
            } else if (name.equals("putShortVolatile0") && desc.equals("(Ljava/lang/Object;JS)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetShortVolatile"));
            } else if (name.equals("getCharVolatile0") && desc.equals("(Ljava/lang/Object;J)C")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetCharVolatile"));
            } else if (name.equals("setCharVolatile0") && desc.equals("(Ljava/lang/Object;JC)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetCharVolatile"));
            } else if (name.equals("getLongVolatile0") && desc.equals("(Ljava/lang/Object;J)J")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_GetLongVolatile"));
            } else if (name.equals("putLongVolatile0") && desc.equals("(Ljava/lang/Object;JJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetLongVolatile"));
            } else if (name.equals("putOrderedObject0") && desc.equals("(Ljava/lang/Object;JLjava/lang/Object;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetOrderedObject"));
            } else if (name.equals("putOrderedInt0") && desc.equals("(Ljava/lang/Object;JI)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetOrderedInt"));
            } else if (name.equals("putOrderedLong0") && desc.equals("(Ljava/lang/Object;JJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_SetOrderedLong"));
            } else if (name.equals("unpark0") && desc.equals("(Ljava/lang/Object;)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_Unpark"));
            } else if (name.equals("park0") && desc.equals("(ZJ)V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_Park"));
            } else if (name.equals("loadFence0") && desc.equals("()V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_LoadFence"));
            } else if (name.equals("getLoadAverage0") && desc.equals("([DI)I")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_Loadavg"));
            } else if (name.equals("storeFence0") && desc.equals("()V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_StoreFence"));
            } else if (name.equals("fullFence0") && desc.equals("()V")) {
                unsafe.putAddress(method + size,JVM.getSymbol("Unsafe_FullFence"));
            }
        }
    }

    private native int getInt0(Object var1,long var2);

    @Override
    public int getInt(Object var1, long var2) {
        return getInt0(var1, var2);
    }

    private native void putInt0(Object var1, long var2, int var4);

    @Override
    public void putInt(Object var1, long var2, int var4) {
        putInt0(var1, var2, var4);
    }

    private native Object getObject0(Object var1, long var2);

    @Override
    public Object getObject(Object var1, long var2) {
        return getObject0(var1, var2);
    }

    private native void putObject0(Object var1, long var2, Object var4);

    @Override
    public void putObject(Object var1, long var2, Object var4) {
        putObject0(var1, var2, var4);
    }

    private native boolean getBoolean0(Object var1, long var2);

    @Override
    public boolean getBoolean(Object var1, long var2) {
        return getBoolean0(var1,var2);
    }

    private native void putBoolean0(Object var1, long var2, boolean var4);

    @Override
    public void putBoolean(Object var1, long var2, boolean var4) {
        putBoolean0(var1, var2, var4);
    }

    private native byte getByte0(Object var1, long var2);

    @Override
    public byte getByte(Object var1, long var2) {
        return getByte0(var1, var2);
    }

    private native void putByte0(Object var1, long var2, byte var4);

    @Override
    public void putByte(Object var1, long var2, byte var4) {
        putByte0(var1, var2, var4);
    }

    private native short getShort0(Object var1, long var2);

    @Override
    public short getShort(Object var1, long var2) {
        return getShort0(var1, var2);
    }

    private native void putShort0(Object var1, long var2, short var4);

    @Override
    public void putShort(Object var1, long var2, short var4) {
        putShort0(var1, var2, var4);
    }

    private native char getChar0(Object var1, long var2);

    @Override
    public char getChar(Object var1, long var2) {
        return getChar0(var1, var2);
    }

    private native void putChar0(Object var1, long var2, char var4);

    @Override
    public void putChar(Object var1, long var2, char var4) {
        putChar0(var1, var2, var4);
    }

    private native long getLong0(Object var1, long var2);

    @Override
    public long getLong(Object var1, long var2) {
        return getLong0(var1, var2);
    }

    private native void putLong0(Object var1, long var2, long var4);

    @Override
    public void putLong(Object var1, long var2, long var4) {
            putLong0(var1, var2, var4);
    }

    private native float getFloat0(Object var1, long var2);

    @Override
    public float getFloat(Object var1, long var2) {
        return getFloat0(var1, var2);
    }

    private native void putFloat0(Object var1, long var2, float var4);

    @Override
    public void putFloat(Object var1, long var2, float var4) {
        putFloat0(var1, var2, var4);
    }

    private native double getDouble0(Object var1, long var2);

    @Override
    public double getDouble(Object var1, long var2) {
        return getDouble0(var1, var2);
    }

    private native void putDouble0(Object var1, long var2, double var4);

    @Override
    public void putDouble(Object var1, long var2, double var4) {
        putDouble0(var1, var2, var4);
    }

    private native byte getByte0(long var1);

    @Override
    public byte getByte(long var1) {
        return getByte0(var1);
    }

    private native void putByte0(long var1, byte var3);

    @Override
    public void putByte(long var1, byte var3) {
        putByte0(var1,var3);
    }

    private native short getShort0(long var1);

    @Override
    public short getShort(long var1) {
        return getShort0(var1);
    }

    private native void putShort0(long var1, short var3);

    @Override
    public void putShort(long var1, short var3) {
        putShort0(var1,var3);
    }

    private native char getChar0(long var1);

    @Override
    public char getChar(long var1) {
        return getChar0(var1);
    }

    private native void putChar0(long var1, char var3);

    @Override
    public void putChar(long var1, char var3) {
        putChar0(var1,var3);
    }

    private native int getInt0(long var1);

    @Override
    public int getInt(long var1) {
        return getInt0(var1);
    }

    private native void putInt0(long var1, int var3);

    @Override
    public void putInt(long var1, int var3) {
        putInt0(var1,var3);
    }

    private native long getLong0(long var1);

    @Override
    public long getLong(long var1) {
        return getLong0(var1);
    }

    private native void putLong0(long var1, long var3);

    @Override
    public void putLong(long var1, long var3) {
        putLong0(var1,var3);
    }

    private native float getFloat0(long var1);

    @Override
    public float getFloat(long var1) {
        return getFloat0(var1);
    }

    private native void putFloat0(long var1, float var3);

    @Override
    public void putFloat(long var1, float var3) {
        putFloat0(var1,var3);
    }

    private native double getDouble0(long var1);

    @Override
    public double getDouble(long var1) {
        return getDouble0(var1);
    }

    private native void putDouble0(long var1, double var3);

    @Override
    public void putDouble(long var1, double var3) {
        putDouble0(var1,var3);
    }

    private native long getAddress0(long var1);

    @Override
    public long getAddress(long var1) {
        return getAddress0(var1);
    }

    private native void putAddress0(long var1, long var3);

    @Override
    public void putAddress(long var1, long var3) {
        putAddress0(var1, var3);
    }

    private native long allocateMemory0(long var1);

    @Override
    public long allocateMemory(long var1) {
        return allocateMemory0(var1);
    }

    private native long reallocateMemory0(long var1, long var3);

    @Override
    public long reallocateMemory(long var1, long var3) {
        return reallocateMemory0(var1, var3);
    }

    private native void setMemory0(Object var1, long var2, long var4, byte var6);

    @Override
    public void setMemory(Object var1, long var2, long var4, byte var6) {
        setMemory0(var1, var2, var4, var6);
    }

    private native void copyMemory0(Object var1, long var2, Object var4, long var5, long var7);

    @Override
    public void copyMemory(Object var1, long var2, Object var4, long var5, long var7) {
        copyMemory0(var1, var2, var4, var5, var7);
    }

    private native void freeMemory0(long var1);

    @Override
    public void freeMemory(long var1) {
        freeMemory0(var1);
    }

    private native long staticFieldOffset0(Field var1);

    @Override
    public long staticFieldOffset(Field var1) {
        return staticFieldOffset0(var1);
    }

    private native long objectFieldOffset0(Field var1);

    @Override
    public long objectFieldOffset(Field var1) {
        return objectFieldOffset0(var1);
    }

    private native Object staticFieldBase0(Field var1);

    @Override
    public Object staticFieldBase(Field var1) {
        return staticFieldBase0(var1);
    }

    private native boolean shouldBeInitialized0(Class<?> var1);

    @Override
    public boolean shouldBeInitialized(Class<?> var1) {
        return shouldBeInitialized0(var1);
    }

    private native void ensureClassInitialized0(Class<?> var1);

    @Override
    public void ensureClassInitialized(Class<?> var1) {
        ensureClassInitialized0(var1);
    }

    private native int arrayBaseOffset0(Class<?> var1);

    @Override
    public int arrayBaseOffset(Class<?> var1) {
        return arrayBaseOffset0(var1);
    }

    private native int arrayIndexScale0(Class<?> var1);

    @Override
    public int arrayIndexScale(Class<?> var1) {
        return arrayIndexScale0(var1);
    }

    private static native int addressSize0();

    @Override
    public int addressSize() {
        return addressSize0();
    }

    private native int pageSize0();

    @Override
    public int pageSize() {
        return pageSize0();
    }

    private native Class<?> defineClass0(String var1, byte[] var2, int var3, int var4, ClassLoader var5, ProtectionDomain var6);

    @Override
    public Class<?> defineClass(String var1, byte[] var2, int var3, int var4, ClassLoader var5, ProtectionDomain var6) {
        return defineClass0(var1, var2, var3, var4, var5, var6);
    }

    private native Class<?> defineAnonymousClass0(Class<?> var1, byte[] var2, Object[] var3);

    @Override
    public Class<?> defineAnonymousClass(Class<?> var1, byte[] var2, Object[] var3) {
        return defineAnonymousClass0(var1, var2, var3);
    }

    private native Object allocateInstance0(Class<?> var1) throws InstantiationException;

    @Override
    public Object allocateInstance(Class<?> var1) throws InstantiationException {
        return allocateInstance0(var1);
    }

    private native void monitorEnter0(Object var1);

    @Override
    public void monitorEnter(Object var1) {
        monitorEnter0(var1);
    }

    private native void monitorExit0(Object var1);

    @Override
    public void monitorExit(Object var1) {
        monitorExit0(var1);
    }

    private native boolean tryMonitorEnter0(Object var1);

    @Override
    public boolean tryMonitorEnter(Object var1) {
        return tryMonitorEnter0(var1);
    }

    private native void throwException0(Throwable var1);

    @Override
    public void throwException(Throwable var1) {
        throwException0(var1);
    }

    private native boolean compareAndSwapObject0(Object var1, long var2, Object var4, Object var5);

    @Override
    public boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5) {
        return compareAndSwapObject0(var1, var2, var4, var5);
    }

    private native boolean compareAndSwapInt0(Object var1, long var2, int var4, int var5);

    @Override
    public boolean compareAndSwapInt(Object var1, long var2, int var4, int var5) {
        return compareAndSwapInt0(var1, var2, var4, var5);
    }

    private native boolean compareAndSwapLong0(Object var1, long var2, long var4, long var6);

    @Override
    public boolean compareAndSwapLong(Object var1, long var2, long var4, long var6) {
        return compareAndSwapLong0(var1, var2, var4, var6);
    }

    private native Object getObjectVolatile0(Object var1, long var2);

    @Override
    public Object getObjectVolatile(Object var1, long var2) {
        return getObjectVolatile0(var1, var2);
    }

    private native void putObjectVolatile0(Object var1, long var2, Object var4);

    @Override
    public void putObjectVolatile(Object var1, long var2, Object var4) {
        putObjectVolatile0(var1, var2, var4);
    }

    private native int getIntVolatile0(Object var1, long var2);

    @Override
    public int getIntVolatile(Object var1, long var2) {
        return getIntVolatile0(var1, var2);
    }

    private native void putIntVolatile0(Object var1, long var2, int var4);

    @Override
    public void putIntVolatile(Object var1, long var2, int var4) {
        putIntVolatile0(var1, var2, var4);
    }

    private native boolean getBooleanVolatile0(Object var1, long var2);

    @Override
    public boolean getBooleanVolatile(Object var1, long var2) {
        return getBooleanVolatile0(var1, var2);
    }

    private native void putBooleanVolatile0(Object var1, long var2, boolean var4);

    @Override
    public void putBooleanVolatile(Object var1, long var2, boolean var4) {
        putBooleanVolatile0(var1, var2, var4);
    }

    private native byte getByteVolatile0(Object var1, long var2);

    @Override
    public byte getByteVolatile(Object var1, long var2) {
        return getByteVolatile0(var1, var2);
    }

    private native void putByteVolatile0(Object var1, long var2, byte var4);

    @Override
    public void putByteVolatile(Object var1, long var2, byte var4) {
        putByteVolatile0(var1, var2, var4);
    }

    private native short getShortVolatile0(Object var1, long var2);

    @Override
    public short getShortVolatile(Object var1, long var2) {
        return getShortVolatile0(var1, var2);
    }

    private native void putShortVolatile0(Object var1, long var2, short var4);

    @Override
    public void putShortVolatile(Object var1, long var2, short var4) {
        putShortVolatile0(var1, var2, var4);
    }

    private native char getCharVolatile0(Object var1, long var2);

    @Override
    public char getCharVolatile(Object var1, long var2) {
        return getCharVolatile0(var1, var2);
    }

    private native void putCharVolatile0(Object var1, long var2, char var4);

    @Override
    public void putCharVolatile(Object var1, long var2, char var4) {
        putCharVolatile0(var1, var2, var4);
    }

    private native long getLongVolatile0(Object var1, long var2);

    @Override
    public long getLongVolatile(Object var1, long var2) {
        return getLongVolatile0(var1, var2);
    }

    private native void putLongVolatile0(Object var1, long var2, long var4);

    @Override
    public void putLongVolatile(Object var1, long var2, long var4) {
        putLongVolatile0(var1, var2, var4);
    }

    private native float getFloatVolatile0(Object var1, long var2);

    @Override
    public float getFloatVolatile(Object var1, long var2) {
        return getFloatVolatile0(var1, var2);
    }

    private native void putFloatVolatile0(Object var1, long var2, float var4);

    @Override
    public void putFloatVolatile(Object var1, long var2, float var4) {
        putFloatVolatile0(var1, var2, var4);
    }

    private native double getDoubleVolatile0(Object var1, long var2);

    @Override
    public double getDoubleVolatile(Object var1, long var2) {
        return getDoubleVolatile0(var1, var2);
    }

    private native void putDoubleVolatile0(Object var1, long var2, double var4);

    @Override
    public void putDoubleVolatile(Object var1, long var2, double var4) {
        putDoubleVolatile0(var1, var2, var4);
    }

    private native void putOrderedObject0(Object var1, long var2, Object var4);

    @Override
    public void putOrderedObject(Object var1, long var2, Object var4) {
        putOrderedObject0(var1, var2, var4);
    }

    private native void putOrderedInt0(Object var1, long var2, int var4);

    @Override
    public void putOrderedInt(Object var1, long var2, int var4) {
        putOrderedInt0(var1, var2, var4);
    }

    private native void putOrderedLong0(Object var1, long var2, long var4);

    @Override
    public void putOrderedLong(Object var1, long var2, long var4) {
        putOrderedLong0(var1, var2, var4);
    }

    private native void unpark0(Object var1);

    @Override
    public void unpark(Object var1) {
        unpark0(var1);
    }

    private native void park0(boolean var1, long var2);

    @Override
    public void park(boolean var1, long var2) {
        park0(var1, var2);
    }

    private native int getLoadAverage0(double[] var1, int var2);

    @Override
    public int getLoadAverage(double[] var1, int var2) {
        return getLoadAverage0(var1, var2);
    }

    private native void loadFence0();

    @Override
    public void loadFence() {
        loadFence0();
    }

    private native void storeFence0();

    @Override
    public void storeFence() {
        storeFence0();
    }

    private native void fullFence0();

    @Override
    public void fullFence() {
        fullFence0();
    }
}
