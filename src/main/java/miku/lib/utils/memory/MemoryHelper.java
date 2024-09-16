package miku.lib.utils.memory;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

@SuppressWarnings("unused")
public interface MemoryHelper {

    static MemoryHelper getInstance(){
        try {
            return new NativeMemoryHelper();
        } catch (Throwable t){
            t.printStackTrace();
            return new UnsafeMemoryHelper();
        }
    }

    int getInt(Object var1, long var2);

    void putInt(Object var1, long var2, int var4);

    Object getObject(Object var1, long var2);

    void putObject(Object var1, long var2, Object var4);

    boolean getBoolean(Object var1, long var2);

    void putBoolean(Object var1, long var2, boolean var4);

    byte getByte(Object var1, long var2);

    void putByte(Object var1, long var2, byte var4);

    short getShort(Object var1, long var2);

    void putShort(Object var1, long var2, short var4);

    char getChar(Object var1, long var2);

    void putChar(Object var1, long var2, char var4);

    long getLong(Object var1, long var2);

    void putLong(Object var1, long var2, long var4);

    float getFloat(Object var1, long var2);

    void putFloat(Object var1, long var2, float var4);

    double getDouble(Object var1, long var2);

    void putDouble(Object var1, long var2, double var4);

    byte getByte(long var1);

    void putByte(long var1, byte var3);

    short getShort(long var1);

    void putShort(long var1, short var3);

    char getChar(long var1);

    void putChar(long var1, char var3);

    int getInt(long var1);

    void putInt(long var1, int var3);

    long getLong(long var1);

    void putLong(long var1, long var3);

    float getFloat(long var1);

    void putFloat(long var1, float var3);

    double getDouble(long var1);

    void putDouble(long var1, double var3);

    long getAddress(long var1);

    void putAddress(long var1, long var3);

    long allocateMemory(long var1);

    long reallocateMemory(long var1, long var3);

    void setMemory(Object var1, long var2, long var4, byte var6);

    default void setMemory(long var1, long var3, byte var5) {
        this.setMemory(null, var1, var3, var5);
    }

    void copyMemory(Object var1, long var2, Object var4, long var5, long var7);

    default void copyMemory(long var1, long var3, long var5) {
        this.copyMemory(null, var1, null, var3, var5);
    }

    void freeMemory(long var1);

    long staticFieldOffset(Field var1);

    long objectFieldOffset(Field var1);

    Object staticFieldBase(Field var1);

    boolean shouldBeInitialized(Class<?> var1);

    void ensureClassInitialized(Class<?> var1);

    int arrayBaseOffset(Class<?> var1);

    int arrayIndexScale(Class<?> var1);

    int addressSize();

    int pageSize();

    Class<?> defineClass(String var1, byte[] var2, int var3, int var4, ClassLoader var5, ProtectionDomain var6);

    Class<?> defineAnonymousClass(Class<?> var1, byte[] var2, Object[] var3);

    Object allocateInstance(Class<?> var1) throws InstantiationException;

    void monitorEnter(Object var1);

    void monitorExit(Object var1);

    boolean tryMonitorEnter(Object var1);

    void throwException(Throwable var1);

    boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5);

    boolean compareAndSwapInt(Object var1, long var2, int var4, int var5);

    boolean compareAndSwapLong(Object var1, long var2, long var4, long var6);

    Object getObjectVolatile(Object var1, long var2);

    void putObjectVolatile(Object var1, long var2, Object var4);

    int getIntVolatile(Object var1, long var2);

    void putIntVolatile(Object var1, long var2, int var4);

    boolean getBooleanVolatile(Object var1, long var2);

    void putBooleanVolatile(Object var1, long var2, boolean var4);

    byte getByteVolatile(Object var1, long var2);

    void putByteVolatile(Object var1, long var2, byte var4);

    short getShortVolatile(Object var1, long var2);

    void putShortVolatile(Object var1, long var2, short var4);

    char getCharVolatile(Object var1, long var2);

    void putCharVolatile(Object var1, long var2, char var4);

    long getLongVolatile(Object var1, long var2);

    void putLongVolatile(Object var1, long var2, long var4);

    float getFloatVolatile(Object var1, long var2);

    void putFloatVolatile(Object var1, long var2, float var4);

    double getDoubleVolatile(Object var1, long var2);

    void putDoubleVolatile(Object var1, long var2, double var4);

    void putOrderedObject(Object var1, long var2, Object var4);

    void putOrderedInt(Object var1, long var2, int var4);

    void putOrderedLong(Object var1, long var2, long var4);

    void unpark(Object var1);

    void park(boolean var1, long var2);

    int getLoadAverage(double[] var1, int var2);

    default int getAndAddInt(Object var1, long var2, int var4) {
        int var5;
        do {
            var5 = this.getIntVolatile(var1, var2);
        } while(!this.compareAndSwapInt(var1, var2, var5, var5 + var4));

        return var5;
    }

    default long getAndAddLong(Object var1, long var2, long var4) {
        long var6;
        do {
            var6 = this.getLongVolatile(var1, var2);
        } while(!this.compareAndSwapLong(var1, var2, var6, var6 + var4));

        return var6;
    }

    default int getAndSetInt(Object var1, long var2, int var4) {
        int var5;
        do {
            var5 = this.getIntVolatile(var1, var2);
        } while(!this.compareAndSwapInt(var1, var2, var5, var4));

        return var5;
    }

    default long getAndSetLong(Object var1, long var2, long var4) {
        long var6;
        do {
            var6 = this.getLongVolatile(var1, var2);
        } while(!this.compareAndSwapLong(var1, var2, var6, var4));

        return var6;
    }

    default Object getAndSetObject(Object var1, long var2, Object var4) {
        Object var5;
        do {
            var5 = this.getObjectVolatile(var1, var2);
        } while(!this.compareAndSwapObject(var1, var2, var5, var4));

        return var5;
    }

    void loadFence();

    void storeFence();

    void fullFence();
}
