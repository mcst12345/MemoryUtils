package miku.annihilation.memory;

public class U2Array extends TypePointer {
	public final int length;
	
	public U2Array(long ptrArray) {
		super(ptrArray);
		if (ptrArray == 0L)
			length = 0;
		else
			length = unsafe.getInt(ptrArray);
	}
	
	public U2Array(short[] array) {
		super(unsafe.allocateMemory(4 + array.length * 2));
		length = array.length;
		//Start to layout memory structure.
		unsafe.putInt(address, array.length); // _length
		for (int i = 0; i < array.length; i++)
			putAddress(address + 4 + i * 2, array[i]); // _data
	}
	
	public U2Array(int length) {
		super(unsafe.allocateMemory(4 + length * 2));
		this.length = length;
		//Start to layout memory structure.
		unsafe.putInt(address, length); // _length
		for (int i = 0; i < length; i++)
			putAddress(address + 4 + i * 2, 0L); // _data
	}
	
	public short get(int index) {
		return unsafe.getShort(address + 4 + index * 2);
	}
	
	public void set(int index, short value) {
		unsafe.putShort(address + 4 + index * 2, value);
	}
	
	public short[] toShortArray() {
		short[] arr = new short[length];
		for (int i = 0; i < length; i++)
			arr[i] = get(i);
		return arr;
	}
	
	public int indexOf(short b) {
		for (int i = 0; i < length; i++)
			if (get(i) == b)
				return i;
		return -1;
	}
	
	public boolean contains(short b) {
		for (int i = 0; i < length; i++)
			if (get(i) == b)
				return true;
		return false;
	}
}
