package miku.annihilation.memory.cst;

import miku.annihilation.memory.MemoryTest;
import sun.misc.Unsafe;

public abstract class Constant {
	protected static final Unsafe unsafe = MemoryTest.unsafe;
	public final ConstantPool holder;
	public final int which;
	
	protected Constant(ConstantPool cp, int which) {
		holder = cp;
		this.which = which;
	}
}
