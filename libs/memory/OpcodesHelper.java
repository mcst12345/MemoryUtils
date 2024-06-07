package miku.annihilation.memory;

import java.lang.reflect.Field;

import org.objectweb.asm.Opcodes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class OpcodesHelper {
	private static final Int2ObjectMap<String> tableopc = new Int2ObjectOpenHashMap<>();
	
	// The JVM opcode values which are not part of the ASM public API.
	// See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html.
	public static final int LDC_W = 19;
	public static final int LDC2_W = 20;
	public static final int ILOAD_0 = 26;
	public static final int ILOAD_1 = 27;
	public static final int ILOAD_2 = 28;
	public static final int ILOAD_3 = 29;
	public static final int LLOAD_0 = 30;
	public static final int LLOAD_1 = 31;
	public static final int LLOAD_2 = 32;
	public static final int LLOAD_3 = 33;
	public static final int FLOAD_0 = 34;
	public static final int FLOAD_1 = 35;
	public static final int FLOAD_2 = 36;
	public static final int FLOAD_3 = 37;
	public static final int DLOAD_0 = 38;
	public static final int DLOAD_1 = 39;
	public static final int DLOAD_2 = 40;
	public static final int DLOAD_3 = 41;
	public static final int ALOAD_0 = 42;
	public static final int ALOAD_1 = 43;
	public static final int ALOAD_2 = 44;
	public static final int ALOAD_3 = 45;
	public static final int ISTORE_0 = 59;
	public static final int ISTORE_1 = 60;
	public static final int ISTORE_2 = 61;
	public static final int ISTORE_3 = 62;
	public static final int LSTORE_0 = 63;
	public static final int LSTORE_1 = 64;
	public static final int LSTORE_2 = 65;
	public static final int LSTORE_3 = 66;
	public static final int FSTORE_0 = 67;
	public static final int FSTORE_1 = 68;
	public static final int FSTORE_2 = 69;
	public static final int FSTORE_3 = 70;
	public static final int DSTORE_0 = 71;
	public static final int DSTORE_1 = 72;
	public static final int DSTORE_2 = 73;
	public static final int DSTORE_3 = 74;
	public static final int ASTORE_0 = 75;
	public static final int ASTORE_1 = 76;
	public static final int ASTORE_2 = 77;
	public static final int ASTORE_3 = 78;
	public static final int WIDE = 196;
	public static final int GOTO_W = 200;
	public static final int JSR_W = 201;
	
	// Constants to convert between normal and wide jump instructions.

	// The delta between the GOTO_W and JSR_W opcodes and GOTO and JUMP.
	public static final int WIDE_JUMP_OPCODE_DELTA = GOTO_W - Opcodes.GOTO;

	// Constants to convert JVM opcodes to the equivalent ASM specific opcodes, and vice versa.

	// The delta between the ASM_IFEQ, ..., ASM_IF_ACMPNE, ASM_GOTO and ASM_JSR opcodes
    // and IFEQ, ..., IF_ACMPNE, GOTO and JSR.
	public static final int ASM_OPCODE_DELTA = 49;

	// The delta between the ASM_IFNULL and ASM_IFNONNULL opcodes and IFNULL and IFNONNULL.
	public static final int ASM_IFNULL_OPCODE_DELTA = 20;

	// ASM specific opcodes, used for long forward jump instructions.

	public static final int ASM_IFEQ = Opcodes.IFEQ + ASM_OPCODE_DELTA;
	public static final int ASM_IFNE = Opcodes.IFNE + ASM_OPCODE_DELTA;
	public static final int ASM_IFLT = Opcodes.IFLT + ASM_OPCODE_DELTA;
	public static final int ASM_IFGE = Opcodes.IFGE + ASM_OPCODE_DELTA;
	public static final int ASM_IFGT = Opcodes.IFGT + ASM_OPCODE_DELTA;
	public static final int ASM_IFLE = Opcodes.IFLE + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ICMPEQ = Opcodes.IF_ICMPEQ + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ICMPNE = Opcodes.IF_ICMPNE + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ICMPLT = Opcodes.IF_ICMPLT + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ICMPGE = Opcodes.IF_ICMPGE + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ICMPGT = Opcodes.IF_ICMPGT + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ICMPLE = Opcodes.IF_ICMPLE + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ACMPEQ = Opcodes.IF_ACMPEQ + ASM_OPCODE_DELTA;
	public static final int ASM_IF_ACMPNE = Opcodes.IF_ACMPNE + ASM_OPCODE_DELTA;
	public static final int ASM_GOTO = Opcodes.GOTO + ASM_OPCODE_DELTA;
	public static final int ASM_JSR = Opcodes.JSR + ASM_OPCODE_DELTA;
	public static final int ASM_IFNULL = Opcodes.IFNULL + ASM_IFNULL_OPCODE_DELTA;
	public static final int ASM_IFNONNULL = Opcodes.IFNONNULL + ASM_IFNULL_OPCODE_DELTA;
	public static final int ASM_GOTO_W = 220;
	
	// Hotspot JVM internal bytecode instructions.
	public static final int 
	        _fast_agetfield       = 204, 
		    _fast_bgetfield       = 205, 
		    _fast_cgetfield       = 206, 
		    _fast_dgetfield       = 207, 
		    _fast_fgetfield       = 208, 
		    _fast_igetfield       = 209, 
		    _fast_lgetfield       = 210, 
		    _fast_sgetfield       = 211, 

		    _fast_aputfield       = 212, 
		    _fast_bputfield       = 213, 
		    _fast_cputfield       = 214, 
		    _fast_dputfield       = 215, 
		    _fast_fputfield       = 216, 
		    _fast_iputfield       = 217, 
		    _fast_lputfield       = 218, 
		    _fast_sputfield       = 219, 

		    _fast_aload_0         = 220, 
		    _fast_iaccess_0       = 221, 
		    _fast_aaccess_0       = 222, 
		    _fast_faccess_0       = 223, 

		    _fast_iload           = 224, 
		    _fast_iload2          = 225, 
		    _fast_icaload         = 226, 

		    _fast_invokevfinal    = 227, 
		    _fast_linearswitch    = 228, 
		    _fast_binaryswitch    = 229, 

		    // special handling of oop constants:
		    _fast_aldc            = 230, 
		    _fast_aldc_w          = 231, 

		    _return_register_finalizer    = 232, 

		    // special handling of signature-polymorphic methods:
		    _invokehandle         = 233;
	
	static {
		try {
			Field[] fields = Opcodes.class.getDeclaredFields();
			boolean startCollect = false;
			for (Field f : fields) {
				if ("NOP".equals(f.getName()))
					startCollect = true;
				if (startCollect)
					tableopc.put(f.getInt(null), f.getName().toLowerCase());
			}
			fields = OpcodesHelper.class.getDeclaredFields();
			startCollect = false;
			for (Field f : fields) {
				if ("LDC_W".equals(f.getName()))
					startCollect = true;
				if (startCollect && !f.getName().startsWith("ASM_") && !f.getName().endsWith("_DELTA"))
					tableopc.put(f.getInt(null), f.getName().toLowerCase());
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static String toString(byte opcode) {
		if (opcode == 0)
			return "0";
		String s = tableopc.get(opcode & 0xFF);
		return s == null ? Integer.toString(opcode & 0xFF) : s;
	}
	
	public static String toString(int opcode) {
		if (opcode == 0)
			return "0";
		String s = tableopc.get(opcode);
		return s == null ? Integer.toString(opcode) : s;
	}
}
