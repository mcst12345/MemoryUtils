package miku.annihilation.memory.cst;

import miku.annihilation.memory.Symbol;
import miku.annihilation.memory.TypePointer;

public class Utf8Constant extends Constant {
	public final Symbol utf8;

	Utf8Constant(ConstantPool cp, int which) {
		super(cp, which);
		utf8 = new Symbol(TypePointer.getAddress(cp.addressOf(which) + 4));
	}

	@Override
	public String toString() {
		return utf8.toString();
	}
}
