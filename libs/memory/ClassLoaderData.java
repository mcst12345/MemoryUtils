package miku.annihilation.memory;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import sun.misc.Unsafe;

public class ClassLoaderData extends TypePointer {
	private static ClassLoaderData bootstrapCLD;
	private Oop classLoader;
	PointerLinkedList<Klass> klasses;
	private static final Long2ObjectMap<ClassLoaderData> cache = new Long2ObjectOpenHashMap<>();
	
	private ClassLoaderData(long ptr) {
		super(ptr);
		cache.put(ptr, this);
		classLoader = Oop.getOop(unsafe.getLong(ptr));
	}
	
	public static ClassLoaderData getOrCreateData(long ptr) {
		if (cache.containsKey(ptr))
			return cache.get(ptr);
		return new ClassLoaderData(ptr);
	}
	
	public static ClassLoaderData bootstrapClassLoaderData() {
		if (bootstrapCLD == null)
			bootstrapCLD = Klass.asKlass(Object.class).getClassLoaderData();
		return bootstrapCLD;
	}
	
	public static ClassLoaderData asClassLoaderData(ClassLoader activeLoader) {
		for (ClassLoaderData cld : getAllActiveClassLoaders())
			if (cld.getClassLoader() == activeLoader)
				return cld;
		return null;
	}
	
	public static PointerLinkedList<ClassLoaderData> getAllActiveClassLoaders() {
		Klass bootstrapKlass = Klass.asKlass(ClassLoaderData.class);
		PointerLinkedList<ClassLoaderData> list = new PointerLinkedList<>(bootstrapKlass.getPointer(), 
				Unsafe.ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ Unsafe.ADDRESS_SIZE // _secondary_super_cache
				+ Unsafe.ADDRESS_SIZE // _secondary_supers
				+ Unsafe.ADDRESS_SIZE // primary_supers
				+ Unsafe.ADDRESS_SIZE * 8 // _java_mirror
				+ Unsafe.ADDRESS_SIZE // _super
				+ Unsafe.ADDRESS_SIZE // _subklass
				+ Unsafe.ADDRESS_SIZE // _next_sibling
				+ Unsafe.ADDRESS_SIZE // _next_link
				+ Unsafe.ADDRESS_SIZE // _class_loader_data
			,
				0 // _class_loader
				+ Unsafe.ADDRESS_SIZE // _dependencies
				+ Unsafe.ADDRESS_SIZE // _metaspace
				+ Unsafe.ADDRESS_SIZE // _metaspace_lock
				+ Unsafe.ADDRESS_SIZE // _unloading
				+ 1 // _keep_alive
				+ 1 // _is_anonymous
				+ 1 // _claimed
				+ 4 // _klasses
				+ Unsafe.ADDRESS_SIZE // _handles
				+ Unsafe.ADDRESS_SIZE // _jmethod_ids
				+ Unsafe.ADDRESS_SIZE // _deallocate_list
				+ Unsafe.ADDRESS_SIZE // _next
		, ClassLoaderData.class, -(2 * 4));
		return list;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader.getObject();
	}
	
	public PointerLinkedList<Klass> getKlasses() {
		if (klasses == null) {
			klasses = new PointerLinkedList<>(address, 
					0   // _class_loader
					+ Unsafe.ADDRESS_SIZE // _dependencies
					+ Unsafe.ADDRESS_SIZE // _metaspace
					+ Unsafe.ADDRESS_SIZE // _metaspace_lock
					+ Unsafe.ADDRESS_SIZE // _unloading
					+ 1 // _keep_alive
					+ 1 // _is_anonymous
					+ 1 // _claimed
					+ 4 // _klasses
			, 
				Unsafe.ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ Unsafe.ADDRESS_SIZE // _secondary_super_cache
				+ Unsafe.ADDRESS_SIZE // _secondary_supers
				+ Unsafe.ADDRESS_SIZE // primary_supers
				+ Unsafe.ADDRESS_SIZE * 8 // _java_mirror
				+ Unsafe.ADDRESS_SIZE // _super
				+ Unsafe.ADDRESS_SIZE // _subklass
				+ Unsafe.ADDRESS_SIZE // _next_sibling
				+ Unsafe.ADDRESS_SIZE // _next_link
			, Klass.class, 0, -(2 * 4));
		}
		return klasses;
	}
	
	public boolean isLoadedAnyAnonymousClass() {
		if (unsafe.getByte(
				address 
				+ Unsafe.ADDRESS_SIZE // _dependencies
				+ Unsafe.ADDRESS_SIZE // _metaspace
				+ Unsafe.ADDRESS_SIZE // _metaspace_lock
				+ Unsafe.ADDRESS_SIZE // _unloading
				+ 1// _keep_alive
				+ 1 // _is_anonymous
			) == 0)
			return false;
		return true;
	}
}
