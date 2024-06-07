package miku.annihilation.memory;

public class U1Array extends TypePointer {
	public final int length;
	
	public U1Array(long ptrArray) {
		super(ptrArray);
		if (ptrArray == 0L)
			length = 0;
		else
			length = unsafe.getInt(ptrArray);
	}
	
	public U1Array(byte[] array) {
		super(unsafe.allocateMemory(4 + array.length));
		length = array.length;
		//Start to layout memory structure.
		unsafe.putInt(address, array.length); // _length
		for (int i = 0; i < array.length; i++)
			putAddress(address + 4 + i, array[i]); // _data
	}
	
	public U1Array expand(int expandDelta) {
		int newLength = length + expandDelta;
		long ptr = unsafe.allocateMemory(4 + newLength);
		//Start to layout memory structure.
		unsafe.putInt(ptr, newLength); // _length
		for (int i = 0; i < length; i++)
			putAddress(ptr + 4 + i, get(i)); // _data
		return new U1Array(ptr);
	}
	
	public byte get(int index) {
		checkBound(index);
		return unsafe.getByte(address + 4 + index);
	}
	
	public void set(int index, byte value) {
		checkBound(index);
		unsafe.putByte(address + 4 + index, value);
	}
	
	public byte[] toByteArray() {
		byte[] arr = new byte[length];
		for (int i = 0; i < length; i++)
			arr[i] = get(i);
		return arr;
	}
	
	public int indexOf(byte b) {
		for (int i = 0; i < length; i++)
			if (get(i) == b)
				return i;
		return -1;
	}
	
	public boolean contains(byte b) {
		for (int i = 0; i < length; i++)
			if (get(i) == b)
				return true;
		return false;
	}
	
	private void checkBound(int index) {
		if (index < 0 || index >= length)
			throw new ArrayIndexOutOfBoundsException(index);
	}
}
