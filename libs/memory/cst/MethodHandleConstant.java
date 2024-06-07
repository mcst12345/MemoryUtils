package miku.annihilation.memory.cst;

import java.lang.invoke.MethodHandle;

/** Support for invokedynamic features. */
public class MethodHandleConstant extends Constant {
	/**
     * Constant pool reference-kind codes, as used by ref_kind of CONSTANT_MethodHandle CP entries.
     */
    public static final byte
        //REF_NONE                  = 0,  // null value
        REF_getfield                = 1,
        REF_getstatic               = 2,
        REF_putfield                = 3,
        REF_putstatic               = 4,
        REF_invokevirtual           = 5,
        REF_invokestatic            = 6,
        REF_invokespecial           = 7,
        REF_newinvokespecial        = 8,
        REF_invokeinterface         = 9;
        //REF_LIMIT                 = 10
	
	public final int refKind;
	public final int refIndex;
	public final Utf8Constant name;
	public final Utf8Constant desc;
	public final int nameAndTypeWhich;
	public final ClassConstant klass;
	/** May be null if it hasn't resolved yet. */
	public final MethodHandle resolved;
	
	MethodHandleConstant(ConstantPool cp, int which) {
		super(cp, which);
		int slotValue = unsafe.getInt(cp.addressOf(which) + 4);
		this.refKind = ConstantItem.getLow16(slotValue);
		this.refIndex = ConstantItem.getHigh16(slotValue);
		this.nameAndTypeWhich = ConstantItem.getHigh16(refIndex);
		int nameAndType = cp.get(nameAndTypeWhich).asNameAndTypeInternal();
		this.name = cp.get(ConstantItem.getLow16(nameAndType)).asUtf8();
		this.desc = cp.get(ConstantItem.getHigh16(nameAndType)).asUtf8();
		this.klass = cp.get(ConstantItem.getLow16(refIndex)).asClass();
		this.resolved = (MethodHandle) cp.resolvedObjects()[cp.cpToObjectIndex(which)];
	}
	
	public static int computeRefIndex(int nameAndTypeWhich, int klassWhich) {
		return (nameAndTypeWhich << 16) | klassWhich;
	}
}
