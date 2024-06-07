package miku.annihilation.memory;

import sun.misc.Unsafe;

public class ArrayPointerConverter {
	private static final Unsafe unsafe = MemoryTest.unsafe;

	public static char[] asCharArray(long ptr, int nElements) {
		char[] arr = new char[nElements];
		for (int i = 0; i < nElements; i++)
			arr[i] = unsafe.getChar(ptr + (i * 2));
		return arr;
	}

	public static long toCharPointer(char[] arr) {
		long ptr = unsafe.allocateMemory(arr.length * 2);
		for (int i = 0; i < arr.length; i++)
			unsafe.putChar(ptr + (i * 2), arr[i]);
		return ptr;
	}

	/** Also for C char pointer. */
	public static byte[] asByteArray(long ptr, int nElements) {
		byte[] arr = new byte[nElements];
		for (int i = 0; i < nElements; i++)
			arr[i] = unsafe.getByte(ptr + i);
		return arr;
	}

	/** Also for C char array. */
	public static long toBytePointer(byte[] arr) {
		long ptr = unsafe.allocateMemory(arr.length);
		for (int i = 0; i < arr.length; i++)
			unsafe.putByte(ptr + i, arr[i]);
		return ptr;
	}
}
