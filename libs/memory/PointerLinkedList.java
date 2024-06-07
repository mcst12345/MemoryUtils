package miku.annihilation.memory;

import java.lang.reflect.Constructor;
import java.util.Iterator;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import java.util.LinkedList;

public class PointerLinkedList<E extends TypePointer> extends TypePointer implements Iterable<E> {
	private final long top;
	private final long offsetFirst;
	private final long offsetNext;
	private int size;
	private final Constructor<E> factory;
	private final int bitoffset;
	private final Long2ObjectMap<E> ptr2Object = new Long2ObjectOpenHashMap<>();
	
	public PointerLinkedList(long topContainer, long firstOffset, long offsetOfNext, Class<E> type, int bitOffsetOnGetting) {
		super(getAddress(topContainer + firstOffset));
		top = topContainer;
		bitoffset = bitOffsetOnGetting;
		offsetFirst = firstOffset;
		offsetNext = offsetOfNext;
		try {
			factory = type.getDeclaredConstructor(long.class);
			factory.setAccessible(true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		long current = address;
		while (current != 0L) {
			++size;
			current = elementAt(current + offsetOfNext);
		}
	}
	
	public PointerLinkedList(long topContainer, long firstOffset, long offsetOfNext, Class<E> type, int bitOffsetOnGetting, int firstBitoffset) {
		super((firstBitoffset > 0) ? (getAddress(topContainer + firstOffset) << firstBitoffset) : 
				(firstBitoffset < 0) ? (getAddress(topContainer + firstOffset) >> -firstBitoffset) : 
					(getAddress(topContainer + firstOffset)));
		top = topContainer;
		bitoffset = bitOffsetOnGetting;
		offsetFirst = firstOffset;
		offsetNext = offsetOfNext;
		try {
			factory = type.getDeclaredConstructor(long.class);
			factory.setAccessible(true);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		long current = address;
		while (current != 0L) {
			++size;
			current = elementAt(current + offsetOfNext);
		}
	}
	
	public int size() {
		return size;
	}
	
	public void add(E value) {
		long last = 0L;
		long current = address;
		while (current != 0L) {
			last = current;
			current = elementAt(current + offsetNext);
		}
		if (size == 0) {
			address = value.address;
			putValue(top + offsetFirst, value.getPointer());
		} else
			putValue(last + offsetNext, value.getPointer());
		putValue(value.getPointer() + offsetNext, 0L);
	}
	
	public boolean remove(E value) {
		boolean removed = false;
		long valuePtr = value.getPointer();
		long current = address;
		if (current == valuePtr) {
			long newNext = elementAt(current + offsetNext);
			putValue(top + offsetFirst, newNext);
			putValue(valuePtr + offsetNext, 0L);
			return true;
		}
		while (current != 0L) {
			long next = elementAt(current + offsetNext);
			if (next == valuePtr) {
				long newNext = elementAt(next + offsetNext);
				putValue(current + offsetNext, newNext);
				putValue(valuePtr + offsetNext, 0L);
				removed = true;
			}
			current = next;
		}
		return removed;
	}
	
	public boolean contains(E value) {
		long current = address;
		long valuePtr = value.getPointer();
		while (current != 0L) {
			if (current == valuePtr)
				return true;
			current = elementAt(current + offsetNext);
		}
		return false;
	}
	
	private long elementAt(long addr) {
		if (bitoffset > 0) {
			return getAddress(addr) << bitoffset;
		} else if (bitoffset < 0) {
			return getAddress(addr) >> -bitoffset;
		} else {
			return getAddress(addr);
		}
	}
	
	private void putValue(long addr, long value) {
		if (bitoffset > 0) {
			putAddress(addr, value >> bitoffset);
		} else if (bitoffset < 0) {
			putAddress(addr, value << -bitoffset);
		} else {
			putAddress(addr, value);
		}
	}
	
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			private long current = address;
	
			@Override
			public boolean hasNext() {
				return current != 0L;
			}

			@Override
			public E next() {
				if (ptr2Object.containsKey(current))
					return ptr2Object.get(current);
				try {
					E ret = factory.newInstance(current);
					ptr2Object.put(current, ret);
					if (bitoffset > 0) {
						current = getAddress(current + offsetNext) << bitoffset;
					} else if (bitoffset < 0) {
						current = getAddress(current + offsetNext) >> -bitoffset;
					} else {
						current = getAddress(current + offsetNext);
					}
					return ret;
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		};
	}

	public List<E> asList() {
		List<E> list = new LinkedList<>();
		for (E ptr : this)
			list.add(ptr);
		return list;
	}
	
	@Override
	public String toString() {
		if (size == 0)
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
