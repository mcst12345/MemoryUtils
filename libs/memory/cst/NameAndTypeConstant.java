package miku.annihilation.memory.cst;

/** In our other constant classes, it is almost all inlined because of convenience. */
public class NameAndTypeConstant extends Constant {
	public final Utf8Constant name;
	public final Utf8Constant desc;
	
	protected NameAndTypeConstant(ConstantPool cp, int which) {
		super(cp, which);
		int nameAndType = unsafe.getInt(cp.addressOf(which) + 4);
		this.name = cp.get(ConstantItem.getLow16(nameAndType)).asUtf8();
		this.desc = cp.get(ConstantItem.getHigh16(nameAndType)).asUtf8();
	}
}
