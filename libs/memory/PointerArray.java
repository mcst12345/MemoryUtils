package miku.annihilation.memory;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import sun.misc.Unsafe;

public class PointerArray<E extends TypePointer> extends TypePointer implements Iterable<E> {
	public final int length;
	private final Constructor<E> factory;
	private final Int2ObjectMap<E> cache = new Int2ObjectOpenHashMap<>();
	
	public PointerArray(long ptrArray, Class<E> type) {
		super(ptrArray);
		if (ptrArray != 0L) {
			length = unsafe.getInt(ptrArray);
			try {
				factory = type.getDeclaredConstructor(long.class);
				factory.setAccessible(true);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} else {
			length = 0;
			factory = null;
		}
	}
	
	public PointerArray(E[] array, Class<E> type) {
		super(unsafe.allocateMemory(4 + array.length * Unsafe.ADDRESS_SIZE));
		length = array.length;
		try {
			factory = type.getDeclaredConstructor(long.class);
			factory.setAccessible(true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		//Start to layout memory structure.
		unsafe.putInt(address, array.length); // _length
		for (int i = 0; i < array.length; i++)
			putAddress(address + 4 + i * Unsafe.ADDRESS_SIZE + 4, array[i].getPointer()); // _data
	}
	
	public E get(int index) {
		if (index > length)
			throw new ArrayIndexOutOfBoundsException(String.format("index=%d, but length is %d", index, length));
		if (cache.containsKey(index))
			return cache.get(index);
		try {
			E ret = factory.newInstance(getAddress(address + 4 + index * Unsafe.ADDRESS_SIZE + 4));
			cache.put(index, ret);
			return ret;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public void set(int index, E value) {
		if (index > length)
			throw new ArrayIndexOutOfBoundsException(String.format("index=%d, but length is %d", index, length));
		if (cache.containsKey(index))
			cache.put(index, value);
		putAddress(address + 4 + index * Unsafe.ADDRESS_SIZE + 4, value.getPointer());
	}
	
	public boolean contains(E value) {
		long valuePtr = value.getPointer();
		for (E ptr : this)
			if (valuePtr == ptr.getPointer())
				return true;
		return false;
	}
	
	public int indexOf(E value) {
		for (int i = 0; i < length; i++)
			if (value.equals(get(i)))
				return i;
		return -1;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private int index;
	
			@Override
			public boolean hasNext() {
				return index < length;
			}

			@Override
			public E next() {
				return get(index++);
			}
		};
	}
	
	@Override
	public String toString() {
		if (length == 0)
			return super.toString() + " []";
		StringBuilder sb = new StringBuilder(super.toString());
		sb.append(" [");
		for (E ptr : this) {
			sb.append(ptr.toString());
			sb.append(", ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append(']');
		return sb.toString();
	}
}
