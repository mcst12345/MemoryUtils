package miku.annihilation.memory.cst;

public class FieldReferenceConstant extends Constant {
	public final ClassConstant klass;
	public final Utf8Constant name;
	public final Utf8Constant desc;
	public final int nameAndTypeWhich;
	
	protected FieldReferenceConstant(ConstantPool cp, int which) {
		super(cp, which);
		int slotValue = unsafe.getInt(cp.addressOf(which) + 4);
		this.nameAndTypeWhich = ConstantItem.getHigh16(slotValue);
		int nameAndType = cp.get(nameAndTypeWhich).asNameAndTypeInternal();
		this.name = cp.get(ConstantItem.getLow16(nameAndType)).asUtf8();
		this.desc = cp.get(ConstantItem.getHigh16(nameAndType)).asUtf8();
		this.klass = cp.get(ConstantItem.getLow16(slotValue)).asClass();
	}
}
