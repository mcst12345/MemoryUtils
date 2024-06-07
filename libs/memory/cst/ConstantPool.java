package miku.annihilation.memory.cst;

import static sun.misc.Unsafe.ADDRESS_SIZE;

import java.lang.reflect.Field;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import miku.annihilation.memory.Klass;
import miku.annihilation.memory.Oop;
import miku.annihilation.memory.Symbol;
import miku.annihilation.memory.TypePointer;
import miku.annihilation.memory.U1Array;
import miku.annihilation.memory.U2Array;

public class ConstantPool extends TypePointer {
	// Universal constant tags: from classfile_constants.h
	public static final byte JVM_CONSTANT_Utf8                   = 1;
	public static final byte JVM_CONSTANT_Unicode                = 2; /* unused */
	public static final byte JVM_CONSTANT_Integer                = 3;
	public static final byte JVM_CONSTANT_Float                  = 4;
	public static final byte JVM_CONSTANT_Long                   = 5;
	public static final byte JVM_CONSTANT_Double                 = 6;
	/** Resolved klass. */
	public static final byte JVM_CONSTANT_Class                  = 7;
	public static final byte JVM_CONSTANT_String                 = 8;
	public static final byte JVM_CONSTANT_Fieldref               = 9;
	public static final byte JVM_CONSTANT_Methodref              = 10;
	public static final byte JVM_CONSTANT_InterfaceMethodref     = 11;
	public static final byte JVM_CONSTANT_NameAndType            = 12;
	public static final byte JVM_CONSTANT_MethodHandle           = 15;  // JSR 292
	public static final byte JVM_CONSTANT_MethodType             = 16;   // JSR 292
	public static final byte JVM_CONSTANT_InvokeDynamic          = 18;
	// Specific constant tags for Hotspot JVM: from constantTag.hpp
	public static final byte JVM_CONSTANT_Invalid                  = 0;    // For bad value initialization
	public static final byte JVM_CONSTANT_InternalMin              = 100;  // First implementation tag (aside from bad value of course)
	public static final byte JVM_CONSTANT_UnresolvedClass          = 100;  // Temporary tag until actual use
	public static final byte JVM_CONSTANT_ClassIndex               = 101;  // Temporary tag while constructing constant pool
	public static final byte JVM_CONSTANT_StringIndex              = 102;  // Temporary tag while constructing constant pool
	public static final byte JVM_CONSTANT_UnresolvedClassInError   = 103;  // Error tag due to resolution error
	public static final byte JVM_CONSTANT_MethodHandleInError      = 104;  // Error tag due to resolution error
	public static final byte JVM_CONSTANT_MethodTypeInError        = 105;  // Error tag due to resolution error
	public static final byte JVM_CONSTANT_InternalMax              = 105;  // Last implementation tag
		    
	private U1Array tags; // which -> tag
	private Klass poolHolder;
	private int length;
	private Object[] resolvedReferences;
	private U2Array referenceMap; // resolved -> which
	
	static final Byte2ObjectMap<String> backmap = new Byte2ObjectOpenHashMap<>();
	static {
		try {
			for (Field f : ConstantPool.class.getFields())
				if (f.getType() == byte.class)
					backmap.put(f.getByte(null), f.getName().replace("JVM_CONSTANT_", ""));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public ConstantPool(long ptr) {
		super(ptr);
		tags = new U1Array(getAddress(ptr + ADDRESS_SIZE));
		length = unsafe.getInt(
				address + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
				+ ADDRESS_SIZE // _flags
				+ 4 // _length
		);
		long p = getAddress(
				ptr + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
		);
		if (p == 0L)
			resolvedReferences = null;
		else
			resolvedReferences = Oop.getOop(getAddress(p)).getObject();
		referenceMap = new U2Array(getAddress(
				ptr + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
		));
	}
	
	public Klass getPoolHolder() {
		if (poolHolder == null) {
			poolHolder = Klass.getKlass(getAddress(
					address + ADDRESS_SIZE // _tags
					+ ADDRESS_SIZE // _cache
					+ ADDRESS_SIZE // _pool_holder
			));
		}
		return poolHolder;
	}
	
	long base() {
		return address + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
				+ ADDRESS_SIZE // _flags
				+ 4 // _length
				+ 4 // _saved
				+ 4 // _lock
				+ ADDRESS_SIZE; // &constant_pool[0]
	}
	
	public long addressOf(int which) {
		return base() + which * ADDRESS_SIZE;
	}
	
	private void checkBound(int which) {
		if (!isWithinBounds(which))
			throw new ArrayIndexOutOfBoundsException(String.format("which=%d, but length is %d", which, length()));
	}
	
	// Mapping resolved object array indexes to cp indexes and back.
	public int objectToCpIndex(int index) {
		return referenceMap.get(index);
	}
	
	// Mapping resolved cp indexes to resolved object array indexes and back.
	public int cpToObjectIndex(int which) {
		return referenceMap.indexOf((short) which);
	}
	
	public int putResolvedKlass(int which, Klass k) {
		checkBound(which);
		putAddress(base() + which * ADDRESS_SIZE + 4, k.getPointer());
		tags.set(which, JVM_CONSTANT_Class);
		return which;
	}
	
	// For temporary use while constructing constant pool
	public int putKlassIndex(int which, int name_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_ClassIndex);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, name_index);
		return which;
	}
	
	// Temporary until actual use
	public int putUnresolvedKlass(int which, Symbol klassName) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_UnresolvedClass);
		putAddress(base() + which * ADDRESS_SIZE + 4, klassName.getPointer() | 1);
		return which;
	}
	
	/** Support for invokedynamic features.
	 *  @param which the constant pool index of the constant.
	 *  @param ref_kind see REF_XXXs in {@link memory.cst.MethodHandleConstant}
	 *  @param ref_index a (NameAndTypeWhich << 16) | KlassWhich. Use {@link memory.cst.MethodHandleConstant#computeRefIndex(int, int)} to convert easier.
	 */
	public int putMethodHandle(int which, int ref_kind, int ref_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_MethodHandle);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, (ref_index << 16) | ref_kind);
		return which;
	}
	
	/** Support for invokedynamic features.
	 *  @param which the constant pool index of the constant.
	 *  @param ref_index a which of a (NameAndTypeWhich << 16) | KlassWhich. Use {@link memory.cst.MethodHandleConstant#computeRefIndex(int, int)} to convert easier.
	 */
	public int putMethodType(int which, int ref_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_MethodType);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, ref_index);
		return which;
	}
	
	/** Support for invokedynamic features.
	 *  Put arguments of the bytecode instruction "invokedynamic".
	 *  @param which the constant pool index of the constant.
	 *  @param bootstrap_specifier_index It isn't a which! It is an index in the bootstrap_methods[] in the classbytes of the klass.
	 *  @param name_and_type_index a which of the NameAndType of invoked method if the callsite is a ConstantCallSite.
	 */
	public int putInvokeDynamic(int which, int bootstrap_specifier_index, int name_and_type_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_InvokeDynamic);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, (name_and_type_index << 16) | bootstrap_specifier_index);
		return which;
	}
	
	public int putInt(int which, int i) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_Integer);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, i);
		return which;
	}
	
	public int putLong(int which, long l) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_Long);
		unsafe.putLong(base() + which * ADDRESS_SIZE + 4, l);
		return which;
	}
	
	public int putFloat(int which, float f) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_Float);
		unsafe.putFloat(base() + which * ADDRESS_SIZE + 4, f);
		return which;
	}
	
	public int putDouble(int which, double d) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_Double);
		unsafe.putDouble(base() + which * ADDRESS_SIZE + 4, d);
		return which;
	}
	
	/** Put a constant of bytecode-level string. */
	public int putUtf8(int which, Symbol utf8) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_Utf8);
		putAddress(base() + which * ADDRESS_SIZE + 4, utf8.getPointer());
		return which;
	}
	
	/** Put a constant of resolved(or active) java string object.
	 *  About the definition of "active java string object", see cp_patches of {@link sun.misc.Unsafe#defineAnonymousClass(Class, byte[], Object[])}. 
	 *  @param an index to {@link memory.cst.ConstantPool#resolvedObjects()}}
	 *  @param str a resolved java string object. Supports for unsafe.defineAnonymousClass() cp_patches pseudo-string features.
	 */
	public void putString(int obj_index, Object str) {
		checkBound(objectToCpIndex(obj_index));
		resolvedReferences[obj_index] = str; // Directly take effect to C level.
	}
	
	// For temporary use while constructing constant pool
	public int putStringIndex(int which, int string_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_StringIndex);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, string_index);
		return which;
	}
	
	public int putFieldReference(int which, int class_index, int name_and_type_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_Fieldref);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, (name_and_type_index << 16) | class_index);
		return which;
	}
	
	public int putMethodReference(int which, int class_index, int name_and_type_index, boolean itf) {
		checkBound(which);
		if (itf)
			tags.set(which, JVM_CONSTANT_InterfaceMethodref);
		else
			tags.set(which, JVM_CONSTANT_Methodref);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, (name_and_type_index << 16) | class_index);
		return which;
	}
	
	public int putNameAndType(int which, int name_index, int desc_index) {
		checkBound(which);
		tags.set(which, JVM_CONSTANT_NameAndType);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, (desc_index << 16) | name_index); // Not so nice
		return which;
	}
	
	public int putCustomConstant(byte tag, int which, int value) {
		checkBound(which);
		tags.set(which, tag);
		unsafe.putInt(base() + which * ADDRESS_SIZE + 4, value);
		return which;
	}
	
	public int getFirstEmptyWhich() {
		for (int i = 1; i < length; i++)
			if (tags.get(i) == JVM_CONSTANT_Invalid)
				return i;
		return -1; // full
	}
	
	/** Gets the number of constants in this constant pool. */
	public int length() {
		return length;
	}
	
	/** 
	 * Contains resolved java string objects, MethodHandle objects, and CallSite objects from this constant pool.
	 * We return the resolvedReferences directly so that you can also modify it, the modification will take effect to C level directly.
	 */
	public Object[] resolvedObjects() {
		return resolvedReferences;
	}
	
	/** Make an expanded copy of this constant pool with the given constants count, be null for expanded constants.
	  * It will reset the pointer to ConstantPool of C so your original ConstantPool will be lost.
	  * @param expandDelta equals to (newLength - oldLength)
	  */
	public void expand(int expandDelta) {
		getPoolHolder();
		if (expandDelta < 0)
			throw new IllegalArgumentException("expandDelta is negative");
		int newLength = length + expandDelta;
		int sizeofConstantPool = ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
				+ ADDRESS_SIZE // _flags
				+ 4 // _length
				+ 4 // _saved
				+ 4 // _lock
				+ ADDRESS_SIZE; // sizeof(ConstantPool)
		int totalLength = sizeofConstantPool + newLength * ADDRESS_SIZE;
		long ptr = unsafe.allocateMemory(totalLength);
		totalLength = Math.min(totalLength, sizeofConstantPool + length * ADDRESS_SIZE);
		for (int i = 0; i < totalLength; i++)
			unsafe.putByte(ptr + i, unsafe.getByte(address + i));
		putAddress(ptr + ADDRESS_SIZE, tags.expand(expandDelta));
		unsafe.putInt(
				ptr + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
				+ ADDRESS_SIZE // _flags
				+ 4 // _length
		, newLength);
		address = ptr;
		getPoolHolder().setConstantPool(this);
		
		tags = new U1Array(getAddress(ptr + ADDRESS_SIZE));
		length = unsafe.getInt(
				address + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
				+ ADDRESS_SIZE // _flags
				+ 4 // _length
		);
		long p = getAddress(
				ptr + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
		);
		if (p == 0L)
			resolvedReferences = null;
		else
			resolvedReferences = Oop.getOop(getAddress(p)).getObject();
		referenceMap = new U2Array(getAddress(
				ptr + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
				+ ADDRESS_SIZE // _pool_holder
				+ ADDRESS_SIZE // _operands
				+ ADDRESS_SIZE // _resolved_references
				+ ADDRESS_SIZE // _reference_map
		));
	}
	
	// Will make a copy of constant items, so you needn't to deal with CMEs.
	public ConstantItem[] getAllConstants() {
		ConstantItem[] items = new ConstantItem[length()];
		for (int i = 0; i < items.length; i++) {
			byte tag = tags.get(i);
			items[i] = new ConstantItem(this, i, tag, base() + i * ADDRESS_SIZE);
		}
		return items;
	}
	
	/** Note: In the constant pool, the index is also called "which". */
	public ConstantItem get(int which) {
		checkBound(which);
		byte tag = tags.get(which);
		return new ConstantItem(this, which, tag, base() + which * ADDRESS_SIZE);
	}
	
	public Symbol searchSymbol(String content) {
		for (ConstantItem ci : getAllConstants()) {
			if (ci.tag() == JVM_CONSTANT_UnresolvedClass || ci.tag() == JVM_CONSTANT_UnresolvedClassInError
					|| ci.tag() == JVM_CONSTANT_Utf8 || ci.tag() == JVM_CONSTANT_String) {
				Symbol s = new Symbol(TypePointer.getAddress(addressOf(ci.which()) + 4));
				if (content.equals(s.toString()))
					return s;
			}
			if (ci.tag() == JVM_CONSTANT_Class) {
				Symbol s = Klass.getKlass(TypePointer.getAddress(addressOf(ci.which()) + 4)).getName();
				if (content.equals(s.toString()))
					return s;
			}
		}
		return null;
	}
	
	public int whichOf(Symbol symbol) {
		for (ConstantItem ci : getAllConstants()) {
			if (ci.tag() == JVM_CONSTANT_UnresolvedClass || ci.tag() == JVM_CONSTANT_UnresolvedClassInError
					|| ci.tag() == JVM_CONSTANT_Utf8 || ci.tag() == JVM_CONSTANT_String) {
				Symbol s = new Symbol(TypePointer.getAddress(addressOf(ci.which()) + 4));
				if (symbol.equals(s))
					return ci.which();
			}
		}
		return -1;
	}
	
	/** Make all resolved klasses, field references and method references in the pool unresolved, 
	 *  and they have to resolve once again. Java strings and MethodHandles will not become unresolved.
	 */
	public void clearResolvedCaches() {
		for (ConstantItem cst : getAllConstants()) {
			if (cst.tag() == JVM_CONSTANT_Class && !cst.asResolvedClass().equals(getPoolHolder())) {
				putUnresolvedKlass(cst.which(), cst.asResolvedClass().getName());
			}
		}
		long cache = getAddress(
				address + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
		);
		if (cache != 0L) {
			int len = unsafe.getInt(cache);
			for (int i = 0; i < len; i++)
				putAddress(cache + 4 + ADDRESS_SIZE + i * 4 * ADDRESS_SIZE, getAddress(cache + 4 + ADDRESS_SIZE + i * 4 * ADDRESS_SIZE) & 0x0000FFFFFFFFFFFFL);
		}
		/*if (resolvedReferences != null)
			for (int i = 0; i < resolvedReferences.length; i++)
				resolvedReferences[i] = null;
		if (referenceMap != null)
			for (int i = 0; i < referenceMap.length; i++)
				referenceMap.set(i, (short) 0);*/
	}
	
	/** When you are adding a new field/method reference to the constant pool, use this method to record your reference to cpCache. */
	public void addCacheRecord(int which) {
		long cache = getAddress(
				address + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
		);
		if (cache != 0L) {
			int len = unsafe.getInt(cache) + 1;
			int i = len - 1;
			int size = 4 + ADDRESS_SIZE + len * 4 * ADDRESS_SIZE;
			long ptr = unsafe.allocateMemory(size);
			int t = 0;
			if (len > 1)
				for (; t < (size - 4 * ADDRESS_SIZE); t++)
					unsafe.putByte(ptr + t, unsafe.getByte(cache + t));
			for (; t < size; t++)
				unsafe.putByte(ptr + t, (byte) 0);
			unsafe.putInt(ptr, len);
			unsafe.putInt(ptr + 4 + ADDRESS_SIZE + i * 4 * ADDRESS_SIZE, which);
			putAddress(
					address + ADDRESS_SIZE // _tags
					+ ADDRESS_SIZE // _cache
			, ptr);
		}
	}
	
	public boolean resetCache(int which) {
		long cache = getAddress(
				address + ADDRESS_SIZE // _tags
				+ ADDRESS_SIZE // _cache
		);
		if (cache != 0L) {
			int len = unsafe.getInt(cache);
			for (int i = 0; i < len; i++) {
				long offset = cache + 4 + ADDRESS_SIZE + i * 4 * ADDRESS_SIZE;
				int w = unsafe.getInt(offset) & 0x0000FFFF;
				if (w == which) {
					unsafe.putInt(offset, w);
					return true;
				}
			}
		}
		return false;
	}
	
	/** Tells whether index is within bounds. */
	public boolean isWithinBounds(int index) {
		return 0 <= index && index < length;
	}
}
