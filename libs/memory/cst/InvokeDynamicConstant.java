package miku.annihilation.memory.cst;

public class InvokeDynamicConstant extends Constant {
	public final Utf8Constant name;
	public final Utf8Constant desc;
	public final int nameAndTypeWhich;
	/** It is the bootstrap_specifier_index in {@link memory.cst.ConstantPool#putInvokeDynamic(int, int, int)} */
	public final int bootstrapMethodIndex;
	
	protected InvokeDynamicConstant(ConstantPool cp, int which) {
		super(cp, which);
		int slotValue = unsafe.getInt(cp.addressOf(which) + 4);
		this.nameAndTypeWhich = ConstantItem.getHigh16(slotValue);
		int nameAndType = cp.get(nameAndTypeWhich).asNameAndTypeInternal();
		this.name = cp.get(ConstantItem.getLow16(nameAndType)).asUtf8();
		this.desc = cp.get(ConstantItem.getHigh16(nameAndType)).asUtf8();
		this.bootstrapMethodIndex = ConstantItem.getLow16(slotValue);
	}
}
