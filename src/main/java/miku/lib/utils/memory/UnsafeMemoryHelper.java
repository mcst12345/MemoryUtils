package miku.lib.utils.memory;

import miku.lib.utils.InternalUtils;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public class UnsafeMemoryHelper implements MemoryHelper{
    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    @Override
    public int getInt(Object var1, long var2) {
        return unsafe.getInt(var1,var2);
    }

    @Override
    public void putInt(Object var1, long var2, int var4) {
        unsafe.putInt(var1,var2,var4);
    }

    @Override
    public Object getObject(Object var1, long var2) {
        return unsafe.getObject(var1,var2);
    }

    @Override
    public void putObject(Object var1, long var2, Object var4) {
        unsafe.putObject(var1,var2,var4);
    }

    @Override
    public boolean getBoolean(Object var1, long var2) {
        return unsafe.getBoolean(var1,var2);
    }

    @Override
    public void putBoolean(Object var1, long var2, boolean var4) {
        unsafe.putBoolean(var1,var2,var4);
    }

    @Override
    public byte getByte(Object var1, long var2) {
        return unsafe.getByte(var1,var2);
    }

    @Override
    public void putByte(Object var1, long var2, byte var4) {
        unsafe.putByte(var1,var2,var4);
    }

    @Override
    public short getShort(Object var1, long var2) {
        return unsafe.getShort(var1,var2);
    }

    @Override
    public void putShort(Object var1, long var2, short var4) {
        unsafe.putShort(var1, var2, var4);
    }

    @Override
    public char getChar(Object var1, long var2) {
        return unsafe.getChar(var1,var2);
    }

    @Override
    public void putChar(Object var1, long var2, char var4) {
        unsafe.putChar(var1,var2,var4);
    }

    @Override
    public long getLong(Object var1, long var2) {
        return unsafe.getLong(var1,var2);
    }

    @Override
    public void putLong(Object var1, long var2, long var4) {
        unsafe.putLong(var1, var2, var4);
    }

    @Override
    public float getFloat(Object var1, long var2) {
        return unsafe.getFloat(var1,var2);
    }

    @Override
    public void putFloat(Object var1, long var2, float var4) {
        unsafe.putFloat(var1,var2,var4);
    }

    @Override
    public double getDouble(Object var1, long var2) {
        return unsafe.getDouble(var1,var2);
    }

    @Override
    public void putDouble(Object var1, long var2, double var4) {
        unsafe.putDouble(var1,var2,var4);
    }

    @Override
    public byte getByte(long var1) {
        return unsafe.getByte(var1);
    }

    @Override
    public void putByte(long var1, byte var3) {
        unsafe.putByte(var1, var3);
    }

    @Override
    public short getShort(long var1) {
        return unsafe.getShort(var1);
    }

    @Override
    public void putShort(long var1, short var3) {
        unsafe.putShort(var1,var3);
    }

    @Override
    public char getChar(long var1) {
        return unsafe.getChar(var1);
    }

    @Override
    public void putChar(long var1, char var3) {
        unsafe.putChar(var1,var3);
    }

    @Override
    public int getInt(long var1) {
        return 0;
    }

    @Override
    public void putInt(long var1, int var3) {
        unsafe.putInt(var1,var3);
    }

    @Override
    public long getLong(long var1) {
        return unsafe.getLong(var1);
    }

    @Override
    public void putLong(long var1, long var3) {
        unsafe.putLong(var1,var3);
    }

    @Override
    public float getFloat(long var1) {
        return unsafe.getFloat(var1);
    }

    @Override
    public void putFloat(long var1, float var3) {
        unsafe.putFloat(var1, var3);
    }

    @Override
    public double getDouble(long var1) {
        return unsafe.getDouble(var1);
    }

    @Override
    public void putDouble(long var1, double var3) {
        unsafe.putDouble(var1,var3);
    }

    @Override
    public long getAddress(long var1) {
        return unsafe.getAddress(var1);
    }

    @Override
    public void putAddress(long var1, long var3) {
        unsafe.putAddress(var1, var3);
    }

    @Override
    public long allocateMemory(long var1) {
        return unsafe.allocateMemory(var1);
    }

    @Override
    public long reallocateMemory(long var1, long var3) {
        return unsafe.reallocateMemory(var1,var3);
    }

    @Override
    public void setMemory(Object var1, long var2, long var4, byte var6) {
        unsafe.setMemory(var1,var2,var4,var6);
    }

    @Override
    public void copyMemory(Object var1, long var2, Object var4, long var5, long var7) {
        unsafe.copyMemory(var1,var2,var4,var5,var7);
    }

    @Override
    public void freeMemory(long var1) {
        unsafe.freeMemory(var1);
    }

    @Override
    public long staticFieldOffset(Field var1) {
        return unsafe.staticFieldOffset(var1);
    }

    @Override
    public long objectFieldOffset(Field var1) {
        return unsafe.objectFieldOffset(var1);
    }

    @Override
    public Object staticFieldBase(Field var1) {
        return unsafe.staticFieldBase(var1);
    }

    @Override
    public boolean shouldBeInitialized(Class<?> var1) {
        return unsafe.shouldBeInitialized(var1);
    }

    @Override
    public void ensureClassInitialized(Class<?> var1) {
        unsafe.ensureClassInitialized(var1);
    }

    @Override
    public int arrayBaseOffset(Class<?> var1) {
        return unsafe.arrayBaseOffset(var1);
    }

    @Override
    public int arrayIndexScale(Class<?> var1) {
        return unsafe.arrayIndexScale(var1);
    }

    @Override
    public int addressSize() {
        return unsafe.addressSize();
    }

    @Override
    public int pageSize() {
        return unsafe.pageSize();
    }

    @Override
    public Class<?> defineClass(String var1, byte[] var2, int var3, int var4, ClassLoader var5, ProtectionDomain var6) {
        return unsafe.defineClass(var1, var2, var3, var4, var5, var6);
    }

    @Override
    public Class<?> defineAnonymousClass(Class<?> var1, byte[] var2, Object[] var3) {
        return unsafe.defineAnonymousClass(var1, var2, var3);
    }

    @Override
    public Object allocateInstance(Class<?> var1) throws InstantiationException {
        return unsafe.allocateInstance(var1);
    }

    @Override
    public void monitorEnter(Object var1) {
        unsafe.monitorEnter(var1);
    }

    @Override
    public void monitorExit(Object var1) {
        unsafe.monitorExit(var1);
    }

    @Override
    public boolean tryMonitorEnter(Object var1) {
        return unsafe.tryMonitorEnter(var1);
    }

    @Override
    public void throwException(Throwable var1) {
        unsafe.throwException(var1);
    }

    @Override
    public boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5) {
        return unsafe.compareAndSwapObject(var1, var2, var4, var5);
    }

    @Override
    public boolean compareAndSwapInt(Object var1, long var2, int var4, int var5) {
        return unsafe.compareAndSwapInt(var1, var2, var4, var5);
    }

    @Override
    public boolean compareAndSwapLong(Object var1, long var2, long var4, long var6) {
        return unsafe.compareAndSwapLong(var1, var2, var4, var6);
    }

    @Override
    public Object getObjectVolatile(Object var1, long var2) {
        return unsafe.getObjectVolatile(var1, var2);
    }

    @Override
    public void putObjectVolatile(Object var1, long var2, Object var4) {
        unsafe.putObjectVolatile(var1, var2, var4);
    }

    @Override
    public int getIntVolatile(Object var1, long var2) {
        return unsafe.getIntVolatile(var1,var2);
    }

    @Override
    public void putIntVolatile(Object var1, long var2, int var4) {
        unsafe.putIntVolatile(var1, var2, var4);
    }

    @Override
    public boolean getBooleanVolatile(Object var1, long var2) {
        return unsafe.getBooleanVolatile(var1, var2);
    }

    @Override
    public void putBooleanVolatile(Object var1, long var2, boolean var4) {
        unsafe.putBooleanVolatile(var1, var2, var4);
    }

    @Override
    public byte getByteVolatile(Object var1, long var2) {
        return unsafe.getByteVolatile(var1, var2);
    }

    @Override
    public void putByteVolatile(Object var1, long var2, byte var4) {
        unsafe.putByteVolatile(var1, var2, var4);
    }

    @Override
    public short getShortVolatile(Object var1, long var2) {
        return unsafe.getShortVolatile(var1, var2);
    }

    @Override
    public void putShortVolatile(Object var1, long var2, short var4) {
        unsafe.putShortVolatile(var1, var2, var4);
    }

    @Override
    public char getCharVolatile(Object var1, long var2) {
        return unsafe.getCharVolatile(var1, var2);
    }

    @Override
    public void putCharVolatile(Object var1, long var2, char var4) {
        unsafe.putCharVolatile(var1, var2, var4);
    }

    @Override
    public long getLongVolatile(Object var1, long var2) {
        return unsafe.getLongVolatile(var1, var2);
    }

    @Override
    public void putLongVolatile(Object var1, long var2, long var4) {
        unsafe.putLongVolatile(var1, var2, var4);
    }

    @Override
    public float getFloatVolatile(Object var1, long var2) {
        return unsafe.getFloatVolatile(var1, var2);
    }

    @Override
    public void putFloatVolatile(Object var1, long var2, float var4) {
        unsafe.putFloatVolatile(var1, var2, var4);
    }

    @Override
    public double getDoubleVolatile(Object var1, long var2) {
        return unsafe.getDoubleVolatile(var1, var2);
    }

    @Override
    public void putDoubleVolatile(Object var1, long var2, double var4) {
        unsafe.putDoubleVolatile(var1, var2, var4);
    }

    @Override
    public void putOrderedObject(Object var1, long var2, Object var4) {
        unsafe.putOrderedObject(var1, var2, var4);
    }

    @Override
    public void putOrderedInt(Object var1, long var2, int var4) {
        unsafe.putOrderedInt(var1, var2, var4);
    }

    @Override
    public void putOrderedLong(Object var1, long var2, long var4) {
        unsafe.putOrderedLong(var1, var2, var4);
    }

    @Override
    public void unpark(Object var1) {
        unsafe.unpark(var1);
    }

    @Override
    public void park(boolean var1, long var2) {
        unsafe.park(var1, var2);
    }

    @Override
    public int getLoadAverage(double[] var1, int var2) {
        return unsafe.getLoadAverage(var1, var2);
    }

    @Override
    public void loadFence() {
        unsafe.loadFence();
    }

    @Override
    public void storeFence() {
        unsafe.storeFence();
    }

    @Override
    public void fullFence() {
        unsafe.fullFence();
    }
}
