package miku.annihilation.memory;

//jobject is a oop**.
public class Oop extends TypePointer {
	private final int narrow_oop;
	private final Object object;
	private Klass klass;
	
	private Oop(long oop_ptr) {
		super(oop_ptr);
		if (oop_ptr == 0L) {
			narrow_oop = 0;
			object = null;
			klass = null;
		} else {
			narrow_oop = MemoryTest.encodeOop(oop_ptr);
			object = MemoryTest.object(narrow_oop);
		}
	}
	
	private Oop(int narrow_oop) {
		this(MemoryTest.decodeOop(narrow_oop));
	}
	
	private Oop(Object obj) {
		this(MemoryTest.decodeOop(MemoryTest.addressNarrow(obj)));
	}
	
	public static Oop getOop(long ptr) {
		return new Oop(ptr);
	}
	
	public static Oop getOop(int narrow_oop) {
		return new Oop(narrow_oop);
	}
	
	public static Oop asOop(Object obj) {
		return new Oop(obj);
	}
	
	public int getNarrowAddress() {
		return narrow_oop;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getObject() {
		return (T) object;
	}
	
	public Klass getKlass() {
		if (klass == null)
			klass = Klass.getKlass(object);
		return klass;
	}
	
	public boolean isNull() {
		return narrow_oop == 0;
	}
	
	public boolean isInstanceOf(Class<?> c) {
		return getKlass().isAssignableTo(Klass.asKlass(c));
	}
	
	public boolean isInstanceOf(Klass k) {
		return getKlass().isAssignableTo(k);
	}
	
	public void setKlass(Klass k) {
		klass = k;
		unsafe.ensureClassInitialized(k.getMirror());
		unsafe.putIntVolatile(object, 8L, k.getNarrowAddress());
	}
	
	@SuppressWarnings("unchecked")
	public <T> T makeShallowCopy() {
		Object copy = getKlass().allocateInstance();
		for (long i = 12L; i < getKlass().getInstanceSize(); i++)
			unsafe.putByteVolatile(copy, i, unsafe.getByteVolatile(object, i));
		return (T) copy;
	}
	
	@Override
	public String toString() {
		return getKlass().getName() + " " + super.toString();
	}
}
