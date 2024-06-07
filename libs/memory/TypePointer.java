package miku.annihilation.memory;

import sun.misc.Unsafe;

public abstract class TypePointer {
	protected static final Unsafe unsafe = MemoryTest.unsafe;
	protected static final boolean IS_64BIT_JVM = unsafe.addressSize() == 8;
	protected long address;
	
	protected TypePointer(long ptr) {
		address = ptr;
	}
	
	public long getPointer() {
		return address;
	}
	
	public static long getAddress(long address) {
		if (IS_64BIT_JVM)
			return unsafe.getLong(address);
		else
			return unsafe.getInt(address);
	}
	
	public static void putAddress(long address, long value) {
		if (IS_64BIT_JVM)
			unsafe.putLong(address, value);
		else
			unsafe.putInt(address, (int) value);
	}
	
	public static void putAddress(long address, TypePointer value) {
		if (IS_64BIT_JVM)
			unsafe.putLong(address, value.address);
		else
			unsafe.putInt(address, (int) value.address);
	}
	
	@Override
	public int hashCode() {
		return (int) address;
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof TypePointer))
			return false;
		return ((TypePointer) o).address == address;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + String.format(" 0x%s", Long.toHexString(address));
	}
}
