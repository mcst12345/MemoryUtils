package miku.annihilation.memory;

import static sun.misc.Unsafe.ADDRESS_SIZE;

import miku.annihilation.memory.cst.ConstantPool;

public class ConstMethod extends TypePointer {
	private ConstantPool constants;
	private String name;
	private String desc;
	private short maxStack;
	private short maxLocals;
	private byte[] code;
	
	public ConstMethod(long ptr) {
		super(ptr);
		initialize(ptr);
	}
	
	private void initialize(long ptr) {
		constants = new ConstantPool(getAddress(
				ptr // _fingerprint
				+ 8 // _constants
		));
		int name_index = unsafe.getShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
		);
		name = constants.get(name_index).asUtf8String();
		int signature_index = unsafe.getShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
		);
		desc = constants.get(signature_index).asUtf8String();
		maxStack = unsafe.getShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
		);
		maxLocals = unsafe.getShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
				+ 2 // _max_locals
		);
		int codeSize = unsafe.getShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
		);
		code = new byte[codeSize];
		long codeOffset = 
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
				+ 2 // _max_locals
				+ 2 // _size_of_parameters
				+ 4 // _code[0]
		;
		int currentOffset = 0;
		while (currentOffset < codeSize) {
			byte b = unsafe.getByte(codeOffset + currentOffset);
			code[currentOffset] = b;
			currentOffset++;
		}
	}
	
	public ConstantPool getConstantPool() {
		return constants;
	}
	
	public void setConstantPool(ConstantPool newPool) {
		putAddress(
				address // _fingerprint
				+ 8 // _constants
		, newPool.getPointer());
		constants = newPool;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDesc() {
		return desc;
	}
	
	/** Notice that constant pool caches will not update. */
	public void setName(Symbol s) {
		name = s.toString();
		int which = constants.whichOf(s);
		if (which == -1) {
			constants.expand(1);
			int tail = constants.getFirstEmptyWhich();
			constants.putUtf8(tail, s);
			which = tail;
		}
		unsafe.putShort(
				address // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 2 // interp_kind (memory-intrinsic field?)
				+ 4 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
		, (short) which);
		unsafe.putObject(constants.getPoolHolder().getMirror(), 28L, null); // invalidate reflectionData
	}
	
	/** Notice that constant pool caches will not update. */
	public void setDesc(Symbol s) {
		desc = s.toString();
		int which = constants.whichOf(s);
		if (which == -1) {
			constants.expand(1);
			int tail = constants.getFirstEmptyWhich();
			constants.putUtf8(tail, s);
			which = tail;
		}
		unsafe.putShort(
				address // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 2 // interp_kind (memory-intrinsic field?)
				+ 4 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
		, (short) which);
		unsafe.putObject(constants.getPoolHolder().getMirror(), 28L, null); // invalidate reflectionData
	}
	
	/** Returns the bytecode instructions of this method body. */
	public byte[] getCode() {
		return code;
	}
	
	long expandCode(int expandDelta) {
		int expandInBytes = expandDelta;
		int oldSize = unsafe.getShort(
				address // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
		) * ADDRESS_SIZE;
		if (expandDelta % 8 != 0)
			expandDelta += 8 - (expandDelta % 8);
		long ptr = unsafe.allocateMemory(oldSize + expandDelta); // aligned in words
		int firstCodeOffset =
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
				+ 2 // _max_locals
				+ 2 // _size_of_parameters
				+ 4 // _code[0]
		;
		int i = 0;
		for (; i < firstCodeOffset; i++)
			unsafe.putByte(ptr + i, unsafe.getByte(address + i));
		i += expandInBytes + code.length; // skip _code
		for (; i < oldSize; i++)
			unsafe.putByte(ptr + i, unsafe.getByte(address + i - expandInBytes));
		unsafe.putShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
		, (short) (oldSize + expandDelta));
		unsafe.putShort(
				ptr // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 2 // interp_kind (memory-intrinsic field?)
				+ 4 // _flags
				+ 2 // _code_size
		, (short) (code.length + expandInBytes));
		
		address = ptr;
		initialize(ptr);
		return ptr;
	}
	
	public void putInCode(int offset, int data) {
		long codeBase = 
				address // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
				+ 2 // _max_locals
				+ 2 // _size_of_parameters
				+ 4 // _code[0]
		;
		unsafe.putByte(codeBase + offset, (byte) data);
		code[offset] = (byte) data;
	}
	
	public void increaseMaxStack(int maxStackDelta) {
		maxStack += maxStackDelta;
		unsafe.putShort(
				address // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
		, maxStack);
	}
	
	public void increaseMaxLocals(int maxLocalsDelta) {
		maxLocals += maxLocalsDelta;
		unsafe.putShort(
				address // _fingerprint
				+ 8 // _constants
				+ ADDRESS_SIZE // _stackmap_data
				+ ADDRESS_SIZE // _constMethod_size
				+ 4 // _interp_kind (memory-intrinsic field?)
				+ 2 // _flags
				+ 2 // _code_size
				+ 2 // _name_index
				+ 2 // _signature_index
				+ 2 // _method_idnum
				+ 2 // _max_stack
				+ 2 // _max_locals
		, maxLocals);
	}
}
