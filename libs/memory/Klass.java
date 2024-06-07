package miku.annihilation.memory;

import org.objectweb.asm.Opcodes;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import miku.annihilation.memory.cst.ConstantPool;
import static sun.misc.Unsafe.ADDRESS_SIZE;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

public class Klass extends TypePointer {
	private static final Long2ObjectMap<Klass> cache = new Long2ObjectOpenHashMap<>();
	static final int KLASS_SIZE = // C++ vtbl ptr
			ADDRESS_SIZE // _layout_helper
			+ 4 // _super_check_offset
			+ 4 // _name
			+ ADDRESS_SIZE // _secondary_super_cache
			+ ADDRESS_SIZE // _secondary_supers
			+ ADDRESS_SIZE // _primary_supers
			+ ADDRESS_SIZE * 8 // _java_mirror
			+ ADDRESS_SIZE // _super
			+ ADDRESS_SIZE // _subklass
			+ ADDRESS_SIZE // _next_sibling
			+ ADDRESS_SIZE // _next_link
			+ ADDRESS_SIZE // _class_loader_data
			+ ADDRESS_SIZE // _modifier_flags
			+ 4 // _access_flags._flags
			+ 4 // _last_biased_lock_bulk_revocation_time
			+ ADDRESS_SIZE // _prototype_header
			+ ADDRESS_SIZE // _biased_lock_revocation_count
			+ 4 // _trace_id
			+ 4 // _modified_oops
			+ 1 // _accumulated_modified_oops
			+ 2 // sizeof(Klass)
			+ ADDRESS_SIZE
			+ 5;
	public static final Klass CLASS_KLASS = new Klass(MemoryTest.getAddress(Class.class, 72L));
	public static final Klass OBJECT_KLASS = Klass.asKlass(Object.class);
	private static boolean wasClonePublic;
	private final int narrow_ptr;
	private Oop mirror;
	private Klass superklass;
	private Symbol name;
	private final int layoutHelper;
	private final int sizeHelper;
	private PointerArray<Klass> interfaces;
	private PointerArray<Klass> localInterfaces;
	private ClassLoaderData classLoaderData;
	private int accessFlags;
	private ConstantPool constants;
	private short miscFlags;
	private PointerArray<MethodPtr> methods;
	private FieldInfo[] fields;
	
	private Klass(long klass_ptr) {
		super(klass_ptr);
		cache.put(klass_ptr, this);
		//MemoryTest.printHex(klass_ptr);
		narrow_ptr = MemoryTest.encodeKlass(klass_ptr);
		mirror = Oop.getOop(getAddress(
				klass_ptr // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
		));
		name = new Symbol(getAddress(
				klass_ptr // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
		));
		layoutHelper = unsafe.getInt(address + ADDRESS_SIZE);
		sizeHelper = (layoutHelper >> 3) * 8;
		classLoaderData = ClassLoaderData.getOrCreateData(getAddress(
				address
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
				+ ADDRESS_SIZE // _next_sibling
				+ ADDRESS_SIZE // _next_link
				+ ADDRESS_SIZE // _class_loader_data
		));
		accessFlags = unsafe.getInt(
				address
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
				+ ADDRESS_SIZE // _next_sibling
				+ ADDRESS_SIZE // _next_link
				+ ADDRESS_SIZE // _class_loader_data
				+ ADDRESS_SIZE // _modifier_flags
				+ 4 // _access_flags._flags
		);
		// InstanceKlass specific
		if (layoutHelper > 0) {
			long cp = getAddress(
					address + KLASS_SIZE // _annotations
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
			);
			constants = new ConstantPool(cp);
			miscFlags = unsafe.getShort(
					address + Klass.KLASS_SIZE // _annotations
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
					+ 2 // _nonstatic_oop_map_size
					+ 4 // _is_marked_dependent
					+ 1 // _has_unloaded_dependent
					+ 1 // _misc_flags
			);
			methods = new PointerArray<>(getAddress(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
					+ 2 // _nonstatic_oop_map_size
					+ 4 // _is_marked_dependent
					+ 1 // _has_unloaded_dependent
					+ 1 // _misc_flags
					+ 2 // _minor_version
					+ 2 // _major_version
					+ 2 // _init_thread
					+ ADDRESS_SIZE // _vtable_len
					+ 4 // _itable_len
					+ 4 // _oop_map_cache
					+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
					+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
					+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
			), MethodPtr.class);
			if (methods.getPointer() != 0L)
				for (MethodPtr method : methods)
					method.setHolder(this);
			long fs = getAddress(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
					+ 2 // _nonstatic_oop_map_size
					+ 4 // _is_marked_dependent
					+ 1 // _has_unloaded_dependent
					+ 1 // _misc_flags
					+ 2 // _minor_version
					+ 2 // _major_version
					+ 2 // _init_thread
					+ ADDRESS_SIZE // _vtable_len
					+ 4 // _itable_len
					+ 4 // _oop_map_cache
					+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
					+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
					+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
					+ ADDRESS_SIZE // _default_methods
					+ ADDRESS_SIZE // _local_interfaces
					+ ADDRESS_SIZE // _transitive_interfaces
					+ ADDRESS_SIZE // _method_ordering
					+ ADDRESS_SIZE // _default_vtable_indices
					+ ADDRESS_SIZE // _fields
			);
			int fieldsCount = unsafe.getShort(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
			);
			fields = new FieldInfo[fieldsCount];
			for (int i = 0; i < fieldsCount; i++)
				fields[i] = new FieldInfo(constants, fs + 4 + i * 2 * 6);
		}
	}
	
	public static Klass getKlass(long klass_ptr) {
		if (cache.containsKey(klass_ptr))
			return cache.get(klass_ptr);
		return new Klass(klass_ptr);
	}
	
	public static Klass getKlass(int narrow_klass) {
		long klass_ptr = MemoryTest.decodeKlass(narrow_klass);
		if (cache.containsKey(klass_ptr))
			return cache.get(klass_ptr);
		return new Klass(klass_ptr);
	}
	
	public static Klass asKlass(Class<?> c) {
		long klass_ptr = MemoryTest.getAddress(c, 72L);
		if (cache.containsKey(klass_ptr))
			return cache.get(klass_ptr);
		return new Klass(klass_ptr);
	}
	
	public static Klass asKlassNoCache(Class<?> c) {
		long klass_ptr = MemoryTest.getAddress(c, 72L);
		return new Klass(klass_ptr);
	}
	
	public static void changeKlassOf(Class<?> c, Klass newKlass) {
		MemoryTest.putAddressVolatile(c, 72L, newKlass.getPointer());
	}
	
	public static Klass getKlass(Object obj) {
		if (obj instanceof Class<?>)
			throw new IllegalArgumentException("getKlass(Object) couldn't accept Class object");
		long klass_ptr = MemoryTest.decodeKlass(unsafe.getInt(obj, 8L));
		if (cache.containsKey(klass_ptr))
			return cache.get(klass_ptr);
		return new Klass(klass_ptr);
	}
	
	public int getNarrowAddress() {
		return narrow_ptr;
	}
	
	public Class<?> asClass() {
		return mirror.getObject();
	}
	
	public void setClass(Class<?> c) {
		mirror = Oop.asOop(c);
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
		, Oop.asOop(c).getPointer());
	}
	
	public Class<?> getMirror() {
		return mirror.getObject();
	}
	
	public void setMirror(Class<?> c) {
		mirror = Oop.asOop(c);
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
		, Oop.asOop(c).getPointer());
	}
	
	public Klass getSuperklass() {
		if (superklass == null) {
			long sup = getAddress(
					address // C++ vtbl ptr
					+ ADDRESS_SIZE // _layout_helper
					+ 4 // _super_check_offset
					+ 4 // _name
					+ ADDRESS_SIZE // _secondary_super_cache
					+ ADDRESS_SIZE // _secondary_supers
					+ ADDRESS_SIZE // primary_supers
					+ ADDRESS_SIZE * 8 // _java_mirror
					+ ADDRESS_SIZE // _super
			);
			if (sup != 0L)
				superklass = getKlass(sup);
			else
				superklass = null;
		}
		return superklass;
	}
	
	///** WARNING: May lead new object crash! */
	/*public void setSuperklass(Klass newSuperklass) {
		unsafe.ensureClassInitialized(newSuperklass.getMirror());
		unsafe.ensureClassInitialized(mirror.getObject());
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
		, newSuperklass.getPointer());
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 1 // [1]
		, newSuperklass.getPointer());
		new PointerLinkedList<>(newSuperklass.getPointer(), 
				  // C++ vtbl ptr
				8 // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
		, ADDRESS_SIZE // _layout_helper
			+ 4 // _super_check_offset
			+ 4 // _name
			+ ADDRESS_SIZE // _secondary_super_cache
			+ ADDRESS_SIZE // _secondary_supers
			+ ADDRESS_SIZE // primary_supers
			+ ADDRESS_SIZE * 8 // _java_mirror
			+ ADDRESS_SIZE // _super
			+ ADDRESS_SIZE // _subklass
			+ ADDRESS_SIZE // _next_sibling
		, Klass.class, 0).add(this);
		new PointerLinkedList<>(superklass.getPointer(), 
				  // C++ vtbl ptr
				8 // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
		, ADDRESS_SIZE // _layout_helper
			+ 4 // _super_check_offset
			+ 4 // _name
			+ ADDRESS_SIZE // _secondary_super_cache
			+ ADDRESS_SIZE // _secondary_supers
			+ ADDRESS_SIZE // primary_supers
			+ ADDRESS_SIZE * 8 // _java_mirror
			+ ADDRESS_SIZE // _super
			+ ADDRESS_SIZE // _subklass
			+ ADDRESS_SIZE // _next_sibling
		, Klass.class, 0).remove(this);
		superklass = newSuperklass;
	}*/
	
	public String getNameString() {
		return name.toString();
	}
	
	public Symbol getName() {
		return name;
	}
	
	public void setName(Symbol newName) {
		name = newName;
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
		, newName.getPointer());
		unsafe.putObject(mirror, 20L, newName.toString());
	}
	
	/** Return all interfaces this klass implements, contains interfaces declares directly by "implements"
	 * 	and all interfaces this klass implements transitively by class/interface inherition. */
	public Iterable<Klass> getAllInterfaces() {
		if (interfaces == null) {
			interfaces = new PointerArray<>(getAddress(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
					+ 2 // _nonstatic_oop_map_size
					+ 4 // _is_marked_dependent
					+ 1 // _has_unloaded_dependent
					+ 1 // _misc_flags
					+ 2 // _minor_version
					+ 2 // _major_version
					+ 2 // _init_thread
					+ ADDRESS_SIZE // _vtable_len
					+ 4 // _itable_len
					+ 4 // _oop_map_cache
					+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
					+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
					+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
					+ ADDRESS_SIZE // _default_methods
					+ ADDRESS_SIZE // _local_interfaces
					+ ADDRESS_SIZE // _transitive_interfaces
			), Klass.class);
		}
		return interfaces;
	}
	
	/** Only return interfaces this klass declares directly by "implements". */
	public Iterable<Klass> getLocalInterfaces() {
		if (localInterfaces == null) {
			localInterfaces = new PointerArray<>(getAddress(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
					+ 2 // _nonstatic_oop_map_size
					+ 4 // _is_marked_dependent
					+ 1 // _has_unloaded_dependent
					+ 1 // _misc_flags
					+ 2 // _minor_version
					+ 2 // _major_version
					+ 2 // _init_thread
					+ ADDRESS_SIZE // _vtable_len
					+ 4 // _itable_len
					+ 4 // _oop_map_cache
					+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
					+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
					+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
					+ ADDRESS_SIZE // _default_methods
					+ ADDRESS_SIZE // _local_interfaces
			), Klass.class);
		}
		return localInterfaces;
	}
	
	/*public void addInterface(Klass newInterface) {
		ensureGetInterfaces();
		newInterface.ensureInitialized();
		Klass[] arr = new Klass[interfaces.length + 1];
		for (int i = 0; i < interfaces.length; i++)
			arr[i] = interfaces.get(i);
		arr[arr.length - 1] = newInterface;
		PointerArray<Klass> newInterfaces = new PointerArray<>(arr, Klass.class);
		interfaces = newInterfaces;
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
		, newInterfaces.getPointer());
		putAddress(
				address + Klass.KLASS_SIZE // _annotations 
				+ ADDRESS_SIZE // _array_klasses
				+ ADDRESS_SIZE // _constants
				+ ADDRESS_SIZE // _inner_classes
				+ ADDRESS_SIZE // _source_debug_extension
				+ ADDRESS_SIZE // _array_name
				+ ADDRESS_SIZE // _nonstatic_field_size
				+ 4 // _static_field_size
				+ 4 // _generic_signature_index
				+ 2 // _source_file_name_index
				+ 2 // _static_oop_field_count
				+ 2 // _java_fields_count
				+ 2 // _nonstatic_oop_map_size
				+ 4 // _is_marked_dependent
				+ 1 // _has_unloaded_dependent
				+ 1 // _misc_flags
				+ 2 // _minor_version
				+ 2 // _major_version
				+ 2 // _init_thread
				+ ADDRESS_SIZE // _vtable_len
				+ 4 // _itable_len
				+ 4 // _oop_map_cache
				+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
				+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
				+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
				+ ADDRESS_SIZE // _default_methods
				+ ADDRESS_SIZE // _local_interfaces
				+ ADDRESS_SIZE // _transitive_interfaces
		, newInterfaces.getPointer());
		
		arr = new Klass[localInterfaces.length + 1];
		for (int i = 0; i < localInterfaces.length; i++)
			arr[i] = localInterfaces.get(i);
		arr[arr.length - 1] = newInterface;
		newInterfaces = new PointerArray<>(arr, Klass.class);
		localInterfaces = newInterfaces;
		putAddress(
				address + Klass.KLASS_SIZE // _annotations 
				+ ADDRESS_SIZE // _array_klasses
				+ ADDRESS_SIZE // _constants
				+ ADDRESS_SIZE // _inner_classes
				+ ADDRESS_SIZE // _source_debug_extension
				+ ADDRESS_SIZE // _array_name
				+ ADDRESS_SIZE // _nonstatic_field_size
				+ 4 // _static_field_size
				+ 4 // _generic_signature_index
				+ 2 // _source_file_name_index
				+ 2 // _static_oop_field_count
				+ 2 // _java_fields_count
				+ 2 // _nonstatic_oop_map_size
				+ 4 // _is_marked_dependent
				+ 1 // _has_unloaded_dependent
				+ 1 // _misc_flags
				+ 2 // _minor_version
				+ 2 // _major_version
				+ 2 // _init_thread
				+ ADDRESS_SIZE // _vtable_len
				+ 4 // _itable_len
				+ 4 // _oop_map_cache
				+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
				+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
				+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
				+ ADDRESS_SIZE // _default_methods
				+ ADDRESS_SIZE // _local_interfaces
		, newInterfaces.getPointer());
		onUpdateInterfaces();
	}*/
	
	public void makeCloneable() {
		setAccessFlags(getAccessFlags() | 0x80000000); // JVM_ACC_IS_CLONEABLE
		//addInterface(Klass.asKlass(Cloneable.class));
		if (!wasClonePublic) {
			OBJECT_KLASS.getMethod("clone", "()Ljava/lang/Object;").setAccessFlags(ModifierHelper.PUBLIC | ModifierHelper.NATIVE);
			wasClonePublic = true;
		}
	}
	
	///** oldInterface must be a local interface! See {@link memory.Klass#getLocalInterfaces()}} */
	/*public void replaceInterface(Klass oldInterface, Klass newInterface) {
		ensureGetInterfaces();
		unsafe.ensureClassInitialized(newInterface.getMirror());
		int localIndex = localInterfaces.indexOf(oldInterface);
		if (localIndex == -1)
			throw new IllegalArgumentException("Klass " + this.getName() + " doesn't have local interface " + oldInterface.getName());
		
		localInterfaces.set(localIndex, newInterface);
		interfaces.set(interfaces.indexOf(oldInterface), newInterface);
		onUpdateInterfaces();
	}*/

	/*private void ensureGetInterfaces() {
		if (interfaces == null)
			getAllInterfaces();
		if (localInterfaces == null)
			getLocalInterfaces();
	}
	
	private void onUpdateInterfaces() {
		putAddress(
				address // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
		, 0L);
		unsafe.putObject(mirror, 28L, null); // invalidate reflectionData
	}*/
	
	// Include padding.
	public int getInstanceSize() {
		return sizeHelper;
	}
	
	public boolean isArray() {
		return layoutHelper < 0; 
	}
	
	public boolean isInstanceKlass() {
		return layoutHelper > 0;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T allocateInstance() {
		unsafe.ensureClassInitialized(mirror.getObject());
		byte[] container = new byte[sizeHelper - 12];
		unsafe.putIntVolatile(container, 8L, narrow_ptr); // _compressed_klass
		unsafe.putIntVolatile(container, 12L, 0); // clear "length".
		return (T) (Object) container;
	}
	
	/** Result of (objectOfThisKlass instanceof ToKlass) */
	public boolean isAssignableTo(Klass toKlass) {
		unsafe.ensureClassInitialized(mirror.getObject());
		unsafe.ensureClassInitialized(toKlass.getMirror());
		if (toKlass.equals(this) || toKlass.equals(OBJECT_KLASS))
			return true;
		Klass current = this;
		while (!(current = current.superklass).equals(OBJECT_KLASS))
			if (current.equals(toKlass))
				return true;
		if (toKlass.isInterface())
			for (Klass itf : interfaces)
				if (itf.equals(toKlass))
					return true;
		return false;
	}
	
	public boolean isInterface() {
		return ModifierHelper.isInterface(accessFlags);
	}
	
	/** 
	 * Iterate each level of loaded subklasses in the inheritance tree below this klass. 
	 * Including subklasses' subklasses, and their subklasses, etc.
	 */
	public Iterable<Klass> getLoadedSubklasses() {
		return recursiveSubklassesOf(this);
	}

	private static List<Klass> recursiveSubklassesOf(Klass k) {
		PointerLinkedList<Klass> subklasses = new PointerLinkedList<>(k.getPointer(),
				    // C++ vtbl ptr
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
		, ADDRESS_SIZE // _layout_helper
			+ 4 // _super_check_offset
			+ 4 // _name
			+ ADDRESS_SIZE // _secondary_super_cache
			+ ADDRESS_SIZE // _secondary_supers
			+ ADDRESS_SIZE // primary_supers
			+ ADDRESS_SIZE * 8 // _java_mirror
			+ ADDRESS_SIZE // _super
			+ ADDRESS_SIZE // _subklass
			+ ADDRESS_SIZE // _next_sibling
		, Klass.class, 0);
		List<Klass> list = subklasses.asList();
		list.remove(k);
		if (list.size() > 0) {
			for (Klass klass : new HashSet<>(list))
				list.addAll(recursiveSubklassesOf(klass));
			return list;
		} else
			return new LinkedList<>();
	}
	
	public ClassLoaderData getClassLoaderData() {
		return classLoaderData;
	}
	
	/** Move this klass from original class loader to a new class loader. */
	public void setClassLoaderData(ClassLoaderData toLoader) {
		classLoaderData.getKlasses();
		toLoader.getKlasses();
		classLoaderData.klasses.remove(this);
		toLoader.klasses.add(this);
		putAddress(
				address
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
				+ ADDRESS_SIZE // _next_sibling
				+ ADDRESS_SIZE // _next_link
				+ ADDRESS_SIZE // _class_loader_data
		, toLoader.getPointer());
		classLoaderData = toLoader;
		unsafe.putObject(getMirror(), 8L + 4L + 12L, toLoader.getClassLoader());
	}
	
	public int getAccessFlags() {
		return accessFlags;
	}
	
	public void setAccessFlags(int newAccessFlags) {
		accessFlags = newAccessFlags;
		unsafe.putInt(
				address
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
				+ ADDRESS_SIZE // _next_sibling
				+ ADDRESS_SIZE // _next_link
				+ ADDRESS_SIZE // _class_loader_data
				+ ADDRESS_SIZE // _modifier_flags
				+ 4 // _access_flags._flags
		, newAccessFlags);
		if ((newAccessFlags & Opcodes.ACC_ENUM) != 0)
			newAccessFlags &= ~Opcodes.ACC_ENUM;
		if ((newAccessFlags & Opcodes.ACC_SUPER) != 0)
			newAccessFlags &= ~Opcodes.ACC_SUPER;
		unsafe.putInt(
				address
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
				+ ADDRESS_SIZE // _next_sibling
				+ ADDRESS_SIZE // _next_link
				+ ADDRESS_SIZE // _class_loader_data
				+ ADDRESS_SIZE // _modifier_flags
		, newAccessFlags);
	}
	
	public void setModifierFlags(int modifierFlags) {
		unsafe.putInt(
				address
				+ ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ ADDRESS_SIZE // _secondary_super_cache
				+ ADDRESS_SIZE // _secondary_supers
				+ ADDRESS_SIZE // primary_supers
				+ ADDRESS_SIZE * 8 // _java_mirror
				+ ADDRESS_SIZE // _super
				+ ADDRESS_SIZE // _subklass
				+ ADDRESS_SIZE // _next_sibling
				+ ADDRESS_SIZE // _next_link
				+ ADDRESS_SIZE // _class_loader_data
				+ ADDRESS_SIZE // _modifier_flags
		, modifierFlags);
	}
	
	public ConstantPool getConstantPool() {
		return constants;
	}
	
	public void setConstantPool(ConstantPool cp) {
		putAddress(
				address + KLASS_SIZE // _annotations
				+ ADDRESS_SIZE // _array_klasses
				+ ADDRESS_SIZE // _constants
		, cp.getPointer());
		for (MethodPtr method : methods)
			method.getConstMethod().setConstantPool(cp);
		constants = cp;
	}
	
	public boolean isAnonymous() {
		return (miscFlags & (1 << 3)) != 0;
	}
	
	public Iterable<MethodPtr> getMethods() {
		return methods;
	}
	
	public MethodPtr getMethod(String name, String desc) {
		for (MethodPtr m : methods) {
			ConstMethod cm = m.getConstMethod();
			if (name.equals(cm.getName()) && desc.equals(cm.getDesc()))
				return m;
		}
		throw new NoSuchMethodError(getName() + "." + name + desc);
	}
	
	public void ensureInitialized() {
		unsafe.ensureClassInitialized(mirror.getObject());
	}
	
	public FieldInfo[] getFields() {
		return Arrays.copyOf(fields, fields.length);
	}
	
	public FieldInfo getField(String name, String desc) {
		for (FieldInfo f : fields)
			if (name.equals(f.getName()) && desc.equals(f.getDesc()))
				return f;
		throw new NoSuchFieldError(getName() + "." + name + desc);
	}
	
	/*public void addField(int accessFlags, int nameWhich, int descWhich) {
		int oldFieldsCount = fields.length;
		int newFieldsCount = oldFieldsCount + 1;
		unsafe.putShort(
				address + Klass.KLASS_SIZE // _annotations 
				+ ADDRESS_SIZE // _array_klasses
				+ ADDRESS_SIZE // _constants
				+ ADDRESS_SIZE // _inner_classes
				+ ADDRESS_SIZE // _source_debug_extension
				+ ADDRESS_SIZE // _array_name
				+ ADDRESS_SIZE // _nonstatic_field_size
				+ 4 // _static_field_size
				+ 4 // _generic_signature_index
				+ 2 // _source_file_name_index
				+ 2 // _static_oop_field_count
				+ 2 // _java_fields_count
		, (short) newFieldsCount);
		if (ModifierHelper.isStatic(accessFlags)) {
			int staticFieldSize = unsafe.getInt(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
			);
			unsafe.putInt(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
			, staticFieldSize + ADDRESS_SIZE);
			String desc = constants.get(descWhich).asUtf8String();
			if (desc.startsWith("L") || desc.startsWith("[")) {
				short staticOopFieldCount = unsafe.getShort(
						address + Klass.KLASS_SIZE // _annotations 
						+ ADDRESS_SIZE // _array_klasses
						+ ADDRESS_SIZE // _constants
						+ ADDRESS_SIZE // _inner_classes
						+ ADDRESS_SIZE // _source_debug_extension
						+ ADDRESS_SIZE // _array_name
						+ ADDRESS_SIZE // _nonstatic_field_size
						+ 4 // _static_field_size
						+ 4 // _generic_signature_index
						+ 2 // _source_file_name_index
						+ 2 // _static_oop_field_count
				);
				unsafe.putShort(
						address + Klass.KLASS_SIZE // _annotations 
						+ ADDRESS_SIZE // _array_klasses
						+ ADDRESS_SIZE // _constants
						+ ADDRESS_SIZE // _inner_classes
						+ ADDRESS_SIZE // _source_debug_extension
						+ ADDRESS_SIZE // _array_name
						+ ADDRESS_SIZE // _nonstatic_field_size
						+ 4 // _static_field_size
						+ 4 // _generic_signature_index
						+ 2 // _source_file_name_index
						+ 2 // _static_oop_field_count
				, (short) (staticOopFieldCount + 1));
			}
		} else {
			int nonstaticFieldSize = unsafe.getInt(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
			);
			unsafe.putInt(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
			, nonstaticFieldSize + ADDRESS_SIZE);
		}
		U2Array oldFs = new U2Array(getAddress(
					address + Klass.KLASS_SIZE // _annotations 
					+ ADDRESS_SIZE // _array_klasses
					+ ADDRESS_SIZE // _constants
					+ ADDRESS_SIZE // _inner_classes
					+ ADDRESS_SIZE // _source_debug_extension
					+ ADDRESS_SIZE // _array_name
					+ ADDRESS_SIZE // _nonstatic_field_size
					+ 4 // _static_field_size
					+ 4 // _generic_signature_index
					+ 2 // _source_file_name_index
					+ 2 // _static_oop_field_count
					+ 2 // _java_fields_count
					+ 2 // _nonstatic_oop_map_size
					+ 4 // _is_marked_dependent
					+ 1 // _has_unloaded_dependent
					+ 1 // _misc_flags
					+ 2 // _minor_version
					+ 2 // _major_version
					+ 2 // _init_thread
					+ ADDRESS_SIZE // _vtable_len
					+ 4 // _itable_len
					+ 4 // _oop_map_cache
					+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
					+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
					+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
					+ ADDRESS_SIZE // _default_methods
					+ ADDRESS_SIZE // _local_interfaces
					+ ADDRESS_SIZE // _transitive_interfaces
					+ ADDRESS_SIZE // _method_ordering
					+ ADDRESS_SIZE // _default_vtable_indices
					+ ADDRESS_SIZE // _fields
		));
		U2Array newFs = new U2Array(oldFs.length + 6);
		int i = 0;
		for (; i < oldFieldsCount * 6; i++)
			newFs.set(i, oldFs.get(i));
		FieldInfo last = fields[fields.length - 1];
		int offset = last.getOffset() + getSize(last.getDesc());
		fields = Arrays.copyOf(fields, newFieldsCount);
		int ic = i;
		//Start to layout memory structure.
		newFs.set(i++, (short) accessFlags); // access_flags
		newFs.set(i++, (short) nameWhich); // name_index
		newFs.set(i++, (short) descWhich); // signature_index
		newFs.set(i++, (short) 0); // initval_index
		newFs.set(i++, (short) (offset & 0xFFFF0000)); // low_packed
		newFs.set(i++, (short) (offset & 0x0000FFFF)); // high_packed
		fields[newFieldsCount - 1] = new FieldInfo(constants, newFs.getPointer() + 4 + ic * 2);
		putAddress(
				address + Klass.KLASS_SIZE // _annotations 
				+ ADDRESS_SIZE // _array_klasses
				+ ADDRESS_SIZE // _constants
				+ ADDRESS_SIZE // _inner_classes
				+ ADDRESS_SIZE // _source_debug_extension
				+ ADDRESS_SIZE // _array_name
				+ ADDRESS_SIZE // _nonstatic_field_size
				+ 4 // _static_field_size
				+ 4 // _generic_signature_index
				+ 2 // _source_file_name_index
				+ 2 // _static_oop_field_count
				+ 2 // _java_fields_count
				+ 2 // _nonstatic_oop_map_size
				+ 4 // _is_marked_dependent
				+ 1 // _has_unloaded_dependent
				+ 1 // _misc_flags
				+ 2 // _minor_version
				+ 2 // _major_version
				+ 2 // _init_thread
				+ ADDRESS_SIZE // _vtable_len
				+ 4 // _itable_len
				+ 4 // _oop_map_cache
				+ ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE + ADDRESS_SIZE 
				+ ADDRESS_SIZE + ADDRESS_SIZE // _idnum_allocated_count
				+ 2 + 1 + 1 + ADDRESS_SIZE // _methods
				+ ADDRESS_SIZE // _default_methods
				+ ADDRESS_SIZE // _local_interfaces
				+ ADDRESS_SIZE // _transitive_interfaces
				+ ADDRESS_SIZE // _method_ordering
				+ ADDRESS_SIZE // _default_vtable_indices
				+ ADDRESS_SIZE // _fields
		, newFs);
		for (; i < oldFs.length; i++)
			newFs.set(i, oldFs.get(i - 6));
		unsafe.putObject(mirror.getObject(), 28L, null); // invalidate reflectionData
	}
	
	private static int getSize(String desc) {
		if ("Z".equals(desc) || "B".equals(desc))
			return 1;
		if ("S".equals(desc) || "C".equals(desc))
			return 2;
		if ("I".equals(desc) || "F".equals(desc) || desc.startsWith("L") || desc.startsWith("["))
			return 4;
		if ("J".equals(desc) || "D".equals(desc))
			return 8;
		throw new IllegalArgumentException("Invalid field descriptor: " + desc);
	}*/
	
	@Override
	public String toString() {
		return super.toString() + " " + name;
	}
	
	public static Iterable<Klass> getAllLoadedKlasses() {
		Set<Klass> klasses = new HashSet<>();
		for (ClassLoaderData cld : ClassLoaderData.getAllActiveClassLoaders())
			for (Klass k : cld.getKlasses())
				klasses.add(k);
		return Collections.unmodifiableSet(klasses);
	}
}
