package miku.annihilation.memory;

import miku.annihilation.memory.cst.ConstantPool;

public class FieldInfo extends TypePointer {
	private final ConstantPool constants;
	private int accessFlags;
	private Symbol name;
	private Symbol desc;
	private final int offset;
	
	public FieldInfo(ConstantPool cp, long fakePtr) {
		super(fakePtr);
		this.constants = cp;
		this.accessFlags = unsafe.getShort(fakePtr);
		this.name = cp.get(unsafe.getShort(fakePtr + 2)).asUtf8().utf8;
		this.desc = cp.get(unsafe.getShort(fakePtr + 4)).asUtf8().utf8;
		this.offset = unsafe.getInt(fakePtr + 8) >> 2;
	}

	public int getAccessFlags() {
		return accessFlags;
	}

	public void setAccessFlags(int accessFlags) {
		unsafe.putShort(address, (short) accessFlags);
		this.accessFlags = accessFlags;
		unsafe.putObject(constants.getPoolHolder().getMirror(), 28L, null); // invalidate reflectionData
	}

	public String getName() {
		return name.toString();
	}
	
	/** Notice that constant pool caches will not update. */
	public void setName(int nameWhich) {
		unsafe.putShort(address + 2, (short) nameWhich);
		this.name = constants.get(nameWhich).asUtf8().utf8;
		unsafe.putObject(constants.getPoolHolder().getMirror(), 28L, null); // invalidate reflectionData
	}

	public String getDesc() {
		return desc.toString();
	}
	
	/** Notice that constant pool caches will not update. */
	public void setDesc(int descWhich) {
		unsafe.putShort(address + 4, (short) descWhich);
		this.name = constants.get(descWhich).asUtf8().utf8;
		unsafe.putObject(constants.getPoolHolder().getMirror(), 28L, null); // invalidate reflectionData
	}
	
	public int getOffset() {
		return offset;
	} 
}
