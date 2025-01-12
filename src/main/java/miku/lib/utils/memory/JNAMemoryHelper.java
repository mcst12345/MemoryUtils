package miku.lib.utils.memory;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public class JNAMemoryHelper implements MemoryHelper{
    @Override
    public int getInt(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putInt(Object var1, long var2, int var4) {

    }

    @Override
    public Object getObject(Object var1, long var2) {
        return null;
    }

    @Override
    public void putObject(Object var1, long var2, Object var4) {

    }

    @Override
    public boolean getBoolean(Object var1, long var2) {
        return false;
    }

    @Override
    public void putBoolean(Object var1, long var2, boolean var4) {

    }

    @Override
    public byte getByte(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putByte(Object var1, long var2, byte var4) {

    }

    @Override
    public short getShort(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putShort(Object var1, long var2, short var4) {

    }

    @Override
    public char getChar(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putChar(Object var1, long var2, char var4) {

    }

    @Override
    public long getLong(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putLong(Object var1, long var2, long var4) {

    }

    @Override
    public float getFloat(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putFloat(Object var1, long var2, float var4) {

    }

    @Override
    public double getDouble(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putDouble(Object var1, long var2, double var4) {

    }

    @Override
    public byte getByte(long var1) {
        return 0;
    }

    @Override
    public void putByte(long var1, byte var3) {

    }

    @Override
    public short getShort(long var1) {
        return 0;
    }

    @Override
    public void putShort(long var1, short var3) {

    }

    @Override
    public char getChar(long var1) {
        return 0;
    }

    @Override
    public void putChar(long var1, char var3) {

    }

    @Override
    public int getInt(long var1) {
        return 0;
    }

    @Override
    public void putInt(long var1, int var3) {

    }

    @Override
    public long getLong(long var1) {
        return 0;
    }

    @Override
    public void putLong(long var1, long var3) {

    }

    @Override
    public float getFloat(long var1) {
        return 0;
    }

    @Override
    public void putFloat(long var1, float var3) {

    }

    @Override
    public double getDouble(long var1) {
        return 0;
    }

    @Override
    public void putDouble(long var1, double var3) {

    }

    @Override
    public long getAddress(long var1) {
        return 0;
    }

    @Override
    public void putAddress(long var1, long var3) {

    }

    @Override
    public long allocateMemory(long var1) {
        return 0;
    }

    @Override
    public long reallocateMemory(long var1, long var3) {
        return 0;
    }

    @Override
    public void setMemory(Object var1, long var2, long var4, byte var6) {

    }

    @Override
    public void copyMemory(Object var1, long var2, Object var4, long var5, long var7) {

    }

    @Override
    public void freeMemory(long var1) {

    }

    @Override
    public long staticFieldOffset(Field var1) {
        return 0;
    }

    @Override
    public long objectFieldOffset(Field var1) {
        return 0;
    }

    @Override
    public Object staticFieldBase(Field var1) {
        return null;
    }

    @Override
    public boolean shouldBeInitialized(Class<?> var1) {
        return false;
    }

    @Override
    public void ensureClassInitialized(Class<?> var1) {

    }

    @Override
    public int arrayBaseOffset(Class<?> var1) {
        return 0;
    }

    @Override
    public int arrayIndexScale(Class<?> var1) {
        return 0;
    }

    @Override
    public int addressSize() {
        return 0;
    }

    @Override
    public int pageSize() {
        return 0;
    }

    @Override
    public Class<?> defineClazz(String var1, byte[] var2, int var3, int var4, ClassLoader var5, ProtectionDomain var6) {
        return null;
    }

    @Override
    public Class<?> defineAnonymousClazz(Class<?> var1, byte[] var2, Object[] var3) {
        return null;
    }

    @Override
    public Object allocateInstance(Class<?> var1) throws InstantiationException {
        return null;
    }

    @Override
    public void monitorEnter(Object var1) {

    }

    @Override
    public void monitorExit(Object var1) {

    }

    @Override
    public boolean tryMonitorEnter(Object var1) {
        return false;
    }

    @Override
    public void throwException(Throwable var1) {

    }

    @Override
    public boolean compareAndSwapObject(Object var1, long var2, Object var4, Object var5) {
        return false;
    }

    @Override
    public boolean compareAndSwapInt(Object var1, long var2, int var4, int var5) {
        return false;
    }

    @Override
    public boolean compareAndSwapLong(Object var1, long var2, long var4, long var6) {
        return false;
    }

    @Override
    public Object getObjectVolatile(Object var1, long var2) {
        return null;
    }

    @Override
    public void putObjectVolatile(Object var1, long var2, Object var4) {

    }

    @Override
    public int getIntVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putIntVolatile(Object var1, long var2, int var4) {

    }

    @Override
    public boolean getBooleanVolatile(Object var1, long var2) {
        return false;
    }

    @Override
    public void putBooleanVolatile(Object var1, long var2, boolean var4) {

    }

    @Override
    public byte getByteVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putByteVolatile(Object var1, long var2, byte var4) {

    }

    @Override
    public short getShortVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putShortVolatile(Object var1, long var2, short var4) {

    }

    @Override
    public char getCharVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putCharVolatile(Object var1, long var2, char var4) {

    }

    @Override
    public long getLongVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putLongVolatile(Object var1, long var2, long var4) {

    }

    @Override
    public float getFloatVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putFloatVolatile(Object var1, long var2, float var4) {

    }

    @Override
    public double getDoubleVolatile(Object var1, long var2) {
        return 0;
    }

    @Override
    public void putDoubleVolatile(Object var1, long var2, double var4) {

    }

    @Override
    public void putOrderedObject(Object var1, long var2, Object var4) {

    }

    @Override
    public void putOrderedInt(Object var1, long var2, int var4) {

    }

    @Override
    public void putOrderedLong(Object var1, long var2, long var4) {

    }

    @Override
    public void unpark(Object var1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void park(boolean var1, long var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLoadAverage(double[] var1, int var2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void loadFence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void storeFence() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fullFence() {
        throw new UnsupportedOperationException();
    }
}
