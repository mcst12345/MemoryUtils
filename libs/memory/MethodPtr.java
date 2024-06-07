package miku.annihilation.memory;

import static sun.misc.Unsafe.ADDRESS_SIZE;

public class MethodPtr extends TypePointer {
	private Klass holder;
	private ConstMethod constMethod;
	private int accessFlags;
	
	public MethodPtr(long ptr) {
		super(ptr);
		constMethod = new ConstMethod(getAddress(
				ptr + ADDRESS_SIZE // _constMethod
		));
		accessFlags = unsafe.getInt(
				ptr + ADDRESS_SIZE // _constMethod
				+ ADDRESS_SIZE // _method_data
				+ ADDRESS_SIZE // _method_counters
				+ ADDRESS_SIZE // _access_flags
		);
	}
	
	void setHolder(Klass k) {
		holder = k;
	}
	
	public ConstMethod getConstMethod() {
		return constMethod;
	}
	
	public int getAccessFlags() {
		return accessFlags;
	}
	
	public void setAccessFlags(int newAccessFlags) {
		unsafe.putInt(
				address + ADDRESS_SIZE // _constMethod
				+ ADDRESS_SIZE // _method_data
				+ ADDRESS_SIZE // _method_counters
				+ ADDRESS_SIZE // _access_flags
		, newAccessFlags);
		accessFlags = newAccessFlags;
		unsafe.putObject(holder.getMirror(), 28L, null); // invalidate reflectionData
	}
	
	/** It will expand the size of the ConstMethod and will CLEAR the _code field. So please save the code before expanding.
	 *  It will also clear execution code cache.
	 *  @param expandDelta calculate in bytes. */
	public void expandCode(int expandDelta) {
		putAddress(
				address + ADDRESS_SIZE // _constMethod
		, constMethod.expandCode(expandDelta));
		unlinkMethod();
	}
	
	public void unlinkMethod() {
		putAddress(
				address + ADDRESS_SIZE // _constMethod
				+ ADDRESS_SIZE // _method_data
				+ ADDRESS_SIZE // _method_counters
				+ ADDRESS_SIZE // _access_flags
				+ 4 // _vtable_index
				+ 4 // _result_index
				+ 4 // _method_size
				+ 2 // _intrinsic_id
				+ 1 // _flags
				+ 1 // _i2i_entry
				+ ADDRESS_SIZE // _adapter
				+ ADDRESS_SIZE // _from_compiled_entry
				+ ADDRESS_SIZE // _code
		, 0L);
		putAddress(
				address + ADDRESS_SIZE // _constMethod
				+ ADDRESS_SIZE // _method_data
				+ ADDRESS_SIZE // _method_counters
				+ ADDRESS_SIZE // _access_flags
				+ 4 // _vtable_index
				+ 4 // _result_index
				+ 4 // _method_size
				+ 2 // _intrinsic_id
				+ 1 // _flags
				+ 1 // _i2i_entry
				+ ADDRESS_SIZE // _adapter
		, 0L);
		putAddress(
				address + ADDRESS_SIZE // _constMethod
				+ ADDRESS_SIZE // _method_data
		, 0L);
		putAddress(
				address + ADDRESS_SIZE // _constMethod
				+ ADDRESS_SIZE // _method_data
				+ ADDRESS_SIZE // _method_counters
		, 0L);
	}
	
	@Override
	public String toString() {
		return super.toString() + " " + holder.getName() + "." + constMethod.getName() + constMethod.getDesc(); 
	}
}
