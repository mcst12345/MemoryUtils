package miku.annihilation.memory;
import static sun.misc.Unsafe.ADDRESS_SIZE;

import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.function.Supplier;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.win32.W32APIOptions;

import miku.annihilation.memory.cst.ConstantItem;
import miku.annihilation.memory.cst.ConstantPool;
import miku.annihilation.memory.cst.FieldReferenceConstant;
import miku.annihilation.memory.cst.MethodReferenceConstant;
import miku.annihilation.memory.cst.Utf8Constant;
import sun.misc.Unsafe;

public class MemoryTest {
	public static Unsafe unsafe;
	private static long narrow_klass_base;
	private static long narrow_oop_base;
	private static long narrow_oop_shift;
	private static TransformHelper helper = new TransformHelper();
	
	static int log2(long n) {
		return (int) (Math.log(n) / Math.log(2));
	}
	
	/** Calculates pointer compressing bases/shifts if we are in a 64-bit JVM. */
	static void calculateCompressingField() {
		if (TypePointer.IS_64BIT_JVM) {
			int encoded = unsafe.getInt(new MemoryTest(), 8L);
			long decoded = unsafe.getLong(MemoryTest.class, 72L);
			narrow_klass_base = decoded - (encoded << 3);
			
			int encoded1 = addressNarrow(MemoryTest.class);
			long decoded1 = unsafe.getLong(
				unsafe.getLong(MemoryTest.class, 72L) // C++ vtbl ptr
				+ Unsafe.ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ Unsafe.ADDRESS_SIZE // _secondary_super_cache
				+ Unsafe.ADDRESS_SIZE // _secondary_supers
				+ Unsafe.ADDRESS_SIZE // primary_supers
				+ Unsafe.ADDRESS_SIZE * 8 // _java_mirror
			);
			int encoded2 = addressNarrow(TransformHelper.class);
			long decoded2 = unsafe.getLong(
				unsafe.getLong(TransformHelper.class, 72L) // C++ vtbl ptr
				+ Unsafe.ADDRESS_SIZE // _layout_helper
				+ 4 // _super_check_offset
				+ 4 // _name
				+ Unsafe.ADDRESS_SIZE // _secondary_super_cache
				+ Unsafe.ADDRESS_SIZE // _secondary_supers
				+ Unsafe.ADDRESS_SIZE // primary_supers
				+ Unsafe.ADDRESS_SIZE * 8 // _java_mirror
			);
			narrow_oop_shift = log2((decoded2 - decoded1) / (encoded2 - encoded1));
			narrow_oop_base = decoded1 - (encoded1 << narrow_oop_shift);
		}
	}
	
	static {
		try {
			Field f = Unsafe.class.getDeclaredField("theUnsafe");
			f.setAccessible(true);
			unsafe = (Unsafe) f.get(null);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
		calculateCompressingField();
	}
	
	static class TransformHelper {
		volatile Object content;
		
		TransformHelper() {}
		
		TransformHelper(Object c) {
			content = c;
		}
		
		synchronized void changeContent(Object c) {
			unsafe.putObjectVolatile(this, 12L, c);
		}
		
		synchronized void changeContent(int c) {
			unsafe.putIntVolatile(this, 12L, c);
		}
	}
	
	public interface Kernel32 extends Library {
		@SuppressWarnings("deprecation")
		Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
		
		int PAGE_NOACCESS           = 0x01;   
		int PAGE_READONLY           = 0x02;   
		int PAGE_READWRITE          = 0x04;   
		int PAGE_WRITECOPY          = 0x08;   
		int PAGE_EXECUTE            = 0x10;   
		int PAGE_EXECUTE_READ       = 0x20;   
		int PAGE_EXECUTE_READWRITE  = 0x40;   
		int PAGE_EXECUTE_WRITECOPY  = 0x80;
		
		boolean VirtualProtect(long lpAddress, int dwSize, int flNewProtect, Pointer lpflOldProtect);
	}
	
	/** Please call it in the main class of your Mod before you use any function of this API, if you are using this API in a Minecraft Forge Mod. */
	public MemoryTest() {
		String s = "narrow_ptr(1): " + addressNarrow(this);
		String s2 = "narrow_ptr(2): " + Oop.asOop(this).getNarrowAddress();
	}
	
	public static boolean doesMethodReturn = false;

	public static void main(String[] args) throws Throwable {
		//A.a();
		
		ConstantPool mt = Klass.asKlass(MemoryTest.class).getConstantPool();
		Klass k = Klass.asKlass(A.class);
		unsafe.ensureClassInitialized(A.class);
		MethodPtr m = k.getMethod("a", "()V");
		ConstMethod cm = m.getConstMethod();
		
		ConstantPool cp = k.getConstantPool();
		cp.expand(5);
		int klassWhich = cp.putResolvedKlass(cp.getFirstEmptyWhich(), Klass.asKlass(MemoryTest.class));
		int nameWhich = cp.putUtf8(cp.getFirstEmptyWhich(), mt.searchSymbol("doesMethodReturn"));
		int descWhich = cp.putUtf8(cp.getFirstEmptyWhich(), mt.searchSymbol("Z"));
		int nameAndTypeWhich = cp.putNameAndType(cp.getFirstEmptyWhich(), nameWhich, descWhich);
		int fieldRefWhich = cp.putFieldReference(cp.getFirstEmptyWhich(), klassWhich, nameAndTypeWhich);
		cp.addCacheRecord(fieldRefWhich);
		
		byte[] origCode = cm.getCode();
		m.expandCode(7);
		cm.putInCode(0, 0);
		cm.putInCode(1, 0);
		cm.putInCode(2, 0);
		//cm.putInCode(1, fieldRefWhich & 0xFFFF0000);
		//cm.putInCode(2, fieldRefWhich & 0x0000FFFF);
		cm.putInCode(3, 0);
		cm.putInCode(4, 0);
		cm.putInCode(5, 0);
		cm.putInCode(6, Opcodes.ACONST_NULL);
		for (int i = 0; i < origCode.length; i++)
			cm.putInCode(7 + i, origCode[i]);
		cm.putInCode(7, Opcodes.PUTSTATIC);
		cp.resetCache(cm.getCode()[9]);
		println(OpcodesHelper.toString(cm.getCode()[12]));
		
		A.a();
		System.out.println("Hello?");
	}
	
	public static void main(String[] args) {
		/*long offset =
			8 // _constants
			+ ADDRESS_SIZE // _stackmap_data
			+ ADDRESS_SIZE // _constMethod_size
			+ 4 // _interp_kind (memory-intrinsic field?)
			+ 2 // _flags
			+ 2 // _code_size
			+ 2 // _name_index
			+ 2 // _signature_index
			+ 2 // _method_idnum
			+ 2 // _max_stack
			+ 2 // _max_locals
			+ 2 // _size_of_parameters
			+ 4 // _code[0]
		;
		println(offset);
		println((byte) Opcodes.ICONST_0);
		println((byte) Opcodes.RETURN);*/
		
	}
	
	public static class A {
		private static void a() {
			System.out.println("I am A.a()");
		}
		
		private static void c() {
			System.out.println("Replaced");
		}
		
		public A a;
		public static int b;
		
		private static long c;
		
		volatile Object d;
		
		public int test() {
			return 0;
		}
		protected static void b() {}
	}
	
	static class B {
		public static void method1() {
			System.out.println("I am method1()");
		}
		
		public static void method2() {
			System.out.println("I am method2()");
		}
	}
	
	static class C extends B implements J {
		
	}
	static class D extends C implements I, F {
		public void i() {
			println("I am i()");
		}
		
		public void g() {
			println("I am g()");
		}
	}
	static interface G {
		void g();
	}
	static interface H {}
	static interface I extends H {
		void i();
	}
	static interface J {}
	
	public static enum E {}
	protected static interface F {}
	static interface K {}
	
	static String fillBlank(String hex, int alignedLength) {
		int len = hex.length();
		if (len != alignedLength)
			for (int i = 0; i < (alignedLength - len); i++)
				hex = "0" + hex;
		if (hex.length() != alignedLength)
			hex = hex.substring(hex.length() - alignedLength);
		return hex;
	}
	
	static String bits(int num) {
		return Integer.toBinaryString(num);
	}
	
	static String bits(long num) {
		return Long.toBinaryString(num);
	}
	
	static String hex(int num) {
		return Integer.toHexString(num);
	}
	
	static String hex(long num) {
		return Long.toHexString(num);
	}
	
	static long getAddress(Object object, long offset) {
		if (TypePointer.IS_64BIT_JVM)
			return unsafe.getLong(object, offset);
		else
			return unsafe.getInt(object, offset);
	}
	
	static void putAddressVolatile(Object object, long offset, long value) {
		if (TypePointer.IS_64BIT_JVM)
			unsafe.putLongVolatile(object, offset, value);
		else
			unsafe.putIntVolatile(object, offset, (int) value);
	}
	
	/***********            Pointer compressing of 64-bit JVM supports            *********/
	// oop ptr -> narrowOop ptr
	static int encodeOop(long oop) {
		if (!TypePointer.IS_64BIT_JVM)
			return (int) oop;
		return (int) (oop - (int) narrow_oop_base >> narrow_oop_shift);
	}
		
	// narrowOop ptr -> oop ptr
	static long decodeOop(int narrowOop) {
		if (!TypePointer.IS_64BIT_JVM)
			return narrowOop;
		return narrow_oop_base + (narrowOop << narrow_oop_shift);
	}
	
	// klass ptr -> narrowKlass ptr
	static int encodeKlass(long klass) {
		if (!TypePointer.IS_64BIT_JVM)
			return (int) klass;
		return (int) (klass - narrow_klass_base >> 3);
	}
	
	// narrowKlass ptr -> klass ptr
	static long decodeKlass(int narrowKlass) {
		if (!TypePointer.IS_64BIT_JVM)
			return narrowKlass;
		return narrow_klass_base + (narrowKlass << 3);
	}
	/***********            Supports End            *********/
	
	static void println(Object msg) {
		System.out.println(msg);
	}
	
	static void println(boolean msg) {
		System.out.println(msg);
	}
	
	static void println(String msg) {
		System.out.println(msg);
	}
	
	static void println(long msg) {
		System.out.println(msg);
	}
	
	static void printHex(long msg) {
		System.out.println(Long.toHexString(msg));
	}
	
	static void printHexAligned(long msg, int len) {
		System.out.print(fillBlank(Long.toHexString(msg), len) + " ");
	}
	
	static void printBin(long msg) {
		System.out.println(Long.toBinaryString(msg));
	}
	
	static void println() {
		System.out.println();
	}
	
	static Object object(int narrow_oop) {
		helper.changeContent(narrow_oop);
		return helper.content;
	}
	
	static int addressNarrow(Object obj) {
		helper.changeContent(obj);
		return unsafe.getIntVolatile(helper, 12L);
	}
}
