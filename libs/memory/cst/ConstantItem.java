package miku.annihilation.memory.cst;

import miku.annihilation.memory.Klass;
import miku.annihilation.memory.MemoryTest;
import miku.annihilation.memory.Symbol;
import miku.annihilation.memory.TypePointer;
import sun.misc.Unsafe;

public class ConstantItem {
	protected static final Unsafe unsafe = MemoryTest.unsafe;
	private final ConstantPool holder;
	private final int which;
	private final byte tag;
	private final long address;
	
	public ConstantItem(ConstantPool cp, int which, byte tag, long address) {
		this.holder = cp;
		this.which = which;
		this.tag = tag;
		this.address = address;
	}
	
	public byte tag() {
		return tag;
	}
	
	public int which() {
		return which;
	}
	
	public Symbol asKlassName() {
		if (tag == ConstantPool.JVM_CONSTANT_Class)
			return asResolvedClass().getName();
		if (tag == ConstantPool.JVM_CONSTANT_UnresolvedClass || tag == ConstantPool.JVM_CONSTANT_UnresolvedClassInError)
			return asUnresolvedClass();
		throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
	}
	
	public Klass asResolvedClass() {
		if (tag != ConstantPool.JVM_CONSTANT_Class)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return Klass.getKlass(TypePointer.getAddress(address + 4));
	}
	
	public Symbol asUnresolvedClass() {
		if (tag != ConstantPool.JVM_CONSTANT_UnresolvedClass && tag != ConstantPool.JVM_CONSTANT_UnresolvedClassInError)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new Symbol(TypePointer.getAddress(address + 4));
	}
	
	public boolean isClass() {
		return tag == ConstantPool.JVM_CONSTANT_Class || tag == ConstantPool.JVM_CONSTANT_UnresolvedClass || tag == ConstantPool.JVM_CONSTANT_UnresolvedClassInError;
	}
	
	public ClassConstant asClass() {
		if (tag != ConstantPool.JVM_CONSTANT_Class && tag != ConstantPool.JVM_CONSTANT_UnresolvedClass && tag != ConstantPool.JVM_CONSTANT_UnresolvedClassInError)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new ClassConstant(holder, which);
	}
	
	public int asInt() {
		if (tag != ConstantPool.JVM_CONSTANT_Integer)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return unsafe.getInt(address + 4);
	}
	
	public long asLong() {
		if (tag != ConstantPool.JVM_CONSTANT_Long)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return unsafe.getLong(address + 4);
	}
	
	public float asFloat() {
		if (tag != ConstantPool.JVM_CONSTANT_Float)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return unsafe.getFloat(address + 4);
	}
	
	public double asDouble() {
		if (tag != ConstantPool.JVM_CONSTANT_Double)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return unsafe.getDouble(address + 4);
	}
	
	public Utf8Constant asUtf8() {
		if (tag != ConstantPool.JVM_CONSTANT_Utf8)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new Utf8Constant(holder, which);
	}
	
	/** Convenient method of asUtf8().utf8.toString() */
	public String asUtf8String() {
		return asUtf8().utf8.toString();
	}
	
	public String asString() {
		if (tag != ConstantPool.JVM_CONSTANT_String)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		Object resolved = holder.resolvedObjects()[holder.cpToObjectIndex(which)];
		if (resolved != null) {
			if (resolved instanceof String)
				return (String) resolved;
			else 
				return resolved.toString(); // for pseudo-strings.
		} else {
			return new Symbol(TypePointer.getAddress(address + 4)).toString();
		}
	}
	
	// Also for pseudo-strings. 
	public Object asResolvedString() {
		if (tag != ConstantPool.JVM_CONSTANT_String)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return holder.resolvedObjects()[holder.cpToObjectIndex(which)];
	}
	
	public boolean isPseudoString() {
		return TypePointer.getAddress(address + 4) == 0L;
	}
	
	public Symbol asUnresolvedString() {
		if (tag != ConstantPool.JVM_CONSTANT_String)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new Symbol(TypePointer.getAddress(address + 4));
	}
	
	int asNameAndTypeInternal() {
		if (tag != ConstantPool.JVM_CONSTANT_NameAndType)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return unsafe.getInt(address + 4);
	}
	
	public NameAndTypeConstant asNameAndType() {
		if (tag != ConstantPool.JVM_CONSTANT_NameAndType)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new NameAndTypeConstant(holder, which);
	}
	
	/** Get low 16 bits as an int. Usually it is "p1" in ConstantPool.putXXX(int which, int p1, int p2). */
	static int getLow16(int complexBits) {
		return complexBits & 0x0000FFFF;
	}
	
	/** Get high 16 bits as an int. Usually it is "p2" in ConstantPool.putXXX(int which, int p1, int p2). */
	static int getHigh16(int complexBits) {
		return complexBits >> 16;
	}
	
	/** Support for invokedynamic features. */
	public MethodHandleConstant asMethodHandle() {
		if (tag != ConstantPool.JVM_CONSTANT_MethodHandle)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new MethodHandleConstant(holder, which);
	}
	
	/** Support for invokedynamic features. */
	public MethodTypeConstant asMethodType() {
		if (tag != ConstantPool.JVM_CONSTANT_MethodType)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new MethodTypeConstant(holder, which);
	}
	
	/** Support for invokedynamic features. */
	public InvokeDynamicConstant asInvokeDynamic() {
		if (tag != ConstantPool.JVM_CONSTANT_InvokeDynamic)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new InvokeDynamicConstant(holder, which);
	}
	
	public FieldReferenceConstant asFieldReference() {
		if (tag != ConstantPool.JVM_CONSTANT_Fieldref)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return new FieldReferenceConstant(holder, which);
	}
	
	/** Include InterfaceMethodref. */
	public MethodReferenceConstant asMethodReference() {
		if (tag == ConstantPool.JVM_CONSTANT_Methodref)
			return new MethodReferenceConstant(holder, which, false);
		if (tag == ConstantPool.JVM_CONSTANT_InterfaceMethodref)
			return new MethodReferenceConstant(holder, which, true);
		throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
	}
	
	/*public int asInterfaceMethodReference() {
		if (tag != ConstantPool.JVM_CONSTANT_InterfaceMethodref)
			throw new IllegalStateException("constantPool[which] doesn't have a correct tag. In fact, it is " + ConstantPool.backmap.get(tag));
		return unsafe.getInt(address + 4);
	}*/
	
	public int asCustomConstant() {
		return unsafe.getInt(address + 4);
	}
	
	@Override
	public String toString() {
		return super.toString() + " which=" + which + ", tag=" + ConstantPool.backmap.get(tag);
	}
}
