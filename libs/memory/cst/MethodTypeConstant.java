package miku.annihilation.memory.cst;

/** Support for invokedynamic features. */
public class MethodTypeConstant extends Constant {
	public final int refIndex;
	public final Utf8Constant name;
	public final Utf8Constant desc;
	public final int nameAndTypeWhich;
	public final ClassConstant klass;
	
	protected MethodTypeConstant(ConstantPool cp, int which) {
		super(cp, which);
		this.refIndex = unsafe.getInt(cp.addressOf(which) + 4);;
		this.nameAndTypeWhich = ConstantItem.getHigh16(refIndex);
		int nameAndType = cp.get(nameAndTypeWhich).asNameAndTypeInternal();
		this.name = cp.get(ConstantItem.getLow16(nameAndType)).asUtf8();
		this.desc = cp.get(ConstantItem.getHigh16(nameAndType)).asUtf8();
		this.klass = cp.get(ConstantItem.getLow16(refIndex)).asClass();
	}
}
