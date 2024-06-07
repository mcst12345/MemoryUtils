package miku.annihilation.memory.cst;

import miku.annihilation.memory.Klass;
import miku.annihilation.memory.Symbol;
import miku.annihilation.memory.TypePointer;

public class ClassConstant extends Constant {
	public final Symbol name;
	/** May be null if the constant is unresolved. */
	public final Klass resolved;
	
	ClassConstant(ConstantPool cp, int which) {
		super(cp, which);
		long slotValue = TypePointer.getAddress(cp.addressOf(which) + 4);
		if ((slotValue & 1) == 0) {
			resolved = Klass.getKlass(slotValue);
			name = resolved.getName();
		} else {
			resolved = null;
			name = new Symbol(slotValue);
		}
	}
	
	public boolean isResolved() {
		return resolved != null;
	}
}
