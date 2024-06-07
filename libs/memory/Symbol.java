package miku.annihilation.memory;

import static sun.misc.Unsafe.ADDRESS_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import miku.annihilation.memory.cst.ConstantPool;

public class Symbol extends TypePointer {
	private static final Int2ObjectMap<Symbol> cache = new Int2ObjectOpenHashMap<>();
	private static final long nameOffset = ADDRESS_SIZE + 4 + 4;
	private static final long constantsOffset = Klass.KLASS_SIZE
			+ ADDRESS_SIZE
			+ ADDRESS_SIZE;
	/** The Utf8 constant count (except class name) in a buffer class when you are using {@link memory.Symbol#newSymbols(String[])}.
	  * You can modify it to a suitable value for you if you need to improve the performance.
	  */
	public static int batchBufferSize = 64;
	private final String unicode;
	
	public Symbol(long ptrSymbol) {
		super(ptrSymbol);
		unicode = asUnicode(ptrSymbol);
	}
	
	/*public Symbol(String content) {
		super(unsafe.allocateMemory(2 + 2 + 4 + utf8Length(content.toCharArray())));
		unicode = content;
		//Start to layout memory structure.
		char[] arr = content.toCharArray();
		char len = (char) (utf8Length(arr));
		unsafe.putChar(address, len); // _length
		unsafe.putChar(address + 2, (char) 1); // _refcount
		unsafe.putInt(address + 2 + 2, Math.abs(new Random().nextInt())); // _identity_hash
		long cStr = asUtf8(content.toCharArray());
		for (int i = 0; i < len; i++)
			unsafe.putByte(address + 2 + 2 + 4 + i, unsafe.getByte(cStr + i)); // _body
	}*/
	
	/** Performance is VERY BAD so you'd better not use it when you need to create lots of symbols. */
	public static Symbol newSingleSymbol(String content) {
		int hash = content.hashCode();
		if (cache.containsKey(hash))
			return cache.get(hash);
		ClassWriter cw = new ClassWriter(0);
		cw.visit(Opcodes.V1_8, Opcodes.ACC_SUPER, content, null, "java/lang/Object", null);
		byte[] bc = cw.toByteArray();
		Class<?> c = unsafe.defineAnonymousClass(Symbol.class, bc, null);
		Symbol s = new Symbol(getAddress(MemoryTest.getAddress(c, 72L) + nameOffset));
		unsafe.putChar(s.address + 2, (char) (unsafe.getChar(s.address + 2) + 1)); // _refcount++
		cache.put(content.hashCode(), s);
		return s;
	}
	
	/** Performance is also bad but more suitable for creating lots of symbols. */
	public static void newSymbols(String[] contents) {
		contents = new ArrayList<>(new HashSet<>(Arrays.asList(contents))).toArray(new String[0]);
		int existCount = 0;
		for (int i = 0; i < contents.length; i++) {
			int hash = contents[i].hashCode();
			if (cache.containsKey(hash)) {
				contents[i] = null;
			}
		}
		String[] newContent = new String[contents.length - existCount];
		for (int i = 0, j = 0; i < contents.length; i++) {
			String s = contents[i];
			if (s != null)
				newContent[j++] = s;
		}
		contents = newContent;
		int length = contents.length;
		int bufSize = batchBufferSize;
		int classCount = length / (bufSize + 1);
		if (length % (bufSize + 1) != 0)
			++classCount;
		int contentIndex = 0;
		int[] utf8Whiches = new int[bufSize];
		for (int i = 0; i < classCount; i++) {
			ClassWriter cw = new ClassWriter(0);
			cw.visit(Opcodes.V1_8, Opcodes.ACC_SUPER, contents[contentIndex++], null, "java/lang/Object", null);
			int endIndex = contentIndex + bufSize;
			if (endIndex > length)
				endIndex = length;
			int utf8Index = 0;
			for (int j = contentIndex; j < endIndex; j++) {
				utf8Whiches[utf8Index++] = cw.newUTF8(contents[contentIndex++]);
			}
			byte[] bc = cw.toByteArray();
			Class<?> c = unsafe.defineAnonymousClass(Symbol.class, bc, null);
			long klass_ptr = MemoryTest.getAddress(c, 72L);
			Symbol s = new Symbol(getAddress(klass_ptr + nameOffset));
			unsafe.putChar(s.address + 2, (char) (unsafe.getChar(s.address + 2) + 1)); // _refcount++
			cache.put(s.toString().hashCode(), s);
			long ptrConstants = getAddress(klass_ptr + constantsOffset);
			ConstantPool cp = new ConstantPool(ptrConstants);
			for (int j = 0; j < utf8Index; j++) {
				s = new Symbol(TypePointer.getAddress(cp.addressOf(utf8Whiches[j]) + 4));
				unsafe.putChar(s.address + 2, (char) (unsafe.getChar(s.address + 2) + 1)); // _refcount++
				cache.put(s.toString().hashCode(), s);
			}
		}
	}
	
	/** Find the exist symbol which has the content and created in newSingleSymbol()/newSymbols() method. */
	public static Symbol lookup(String content) {
		return cache.get(content.hashCode());
	}
	
	@Override
	public String toString() {
		return unicode;
	}
	
	private static String asUnicode(long ptrSymbol) {
		int length = unicodeLength(ptrSymbol + 2 + 2 + 4, unsafe.getChar(ptrSymbol));
		char[] result = new char[length];
		if (length > 0) {
			convertToUnicode(ptrSymbol + 2 + 2 + 4, result, length);
		}
		return String.valueOf(result);
	}
	
	/** Count bytes of the form 10xxxxxx and deduct this count
	 *  from the total byte count.  The utf8 string must be in
	 *  legal form which has been verified in the format checker. */
	private static int unicodeLength(long cStr, int len) {
		int num_chars = len;
		for (int i = 0; i < len; i++) {
		    if ((unsafe.getByte(cStr + i) & 0xC0) == 0x80) {
		      --num_chars;
		    }
		}
		return num_chars;
	}
	
	private static void convertToUnicode(long cStr, char[] unicodeStr, int unicodeLen) {
		long ptr = cStr;
		byte ch;
		int index = 0;
		/* ASCII case loop optimization */
		for (; index < unicodeLen; index++) {
			if ((ch = unsafe.getByte(ptr)) < 0)
				break;
			unicodeStr[index] = (char) ch;
			ptr = ptr + 1;
		}
		for (; index < unicodeLen; index++) {
			ptr = next(ptr, unicodeStr, index);
		}
	}
	
	/** Assume the utf8 string is in legal form and has been 
	 *  checked in the class file parser/format checker. */
	private static long next(long ptr, char[] value, int index) {
		byte ch, ch2, ch3;
		int length = -1;   /* bad length */
		char result = 0;
		switch ((ch = unsafe.getByte(ptr)) >> 4) {
		case 0xfffffff8: case 0xfffffff9: case 0xfffffffA: case 0xfffffffB: case 0xfffffffF:
		    /* Shouldn't happen. */
		    break;
		
		case 0xfffffffC: case 0xfffffffD:
		    /* 110xxxxx  10xxxxxx */
		    if (((ch2 = unsafe.getByte(ptr + 1)) & 0xC0) == 0x80) {
		      byte high_five = (byte) (ch & 0x1F);
		      byte low_six = (byte) (ch2 & 0x3F);
		      result = (char) ((high_five << 6) + low_six);
		      length = 2;
		      break;
		    }
		    break;
		
		case 0xfffffffE:
		    /* 1110xxxx 10xxxxxx 10xxxxxx */
		    if (((ch2 = unsafe.getByte(ptr + 1)) & 0xC0) == 0x80) {
		      if (((ch3 = unsafe.getByte(ptr + 2)) & 0xC0) == 0x80) {
		        byte high_four = (byte) (ch & 0x0f);
		        byte mid_six = (byte) (ch2 & 0x3f);
		        byte low_six = (byte) (ch3 & 0x3f);
		        result = (char) ((((high_four << 6) + mid_six) << 6) + low_six);
		        length = 3;
		      }
		    }
		    break;
		default:
			result = (char) ch;
			length = 1;
			break;
		} /* end of switch */
		
		if (length <= 0) {
		    value[index] = (char) unsafe.getByte(ptr);    /* default bad result; */
		    return ptr + 1; // make progress somehow
		}
		
		value[index] = result;
		
		// The assert is correct but the .class file is wrong
		// assert(UNICODE::utf8_size(result) == length, "checking reverse computation");
		return ptr + length;
	}
	
	private static long asUtf8(char[] base) {
		int utf8_len = utf8Length(base);
		long result = unsafe.allocateMemory(utf8_len);
		long p = result;
		for (int index = 0; index < base.length; index++) {
		    p = utf8Write(p, base[index]);
		}
		//unsafe.putByte(p, (byte) '\0');
		return result;
	}
	
	/** Writes a jchar a utf8 and returns the end */
	private static long utf8Write(long base, char ch) {
		if ((ch != 0) && (ch <= 0x7f)) {
		    unsafe.putByte(base, (byte) ch);
		    return base + 1;
		}

		if (ch <= 0x7FF) {
		    /* 11 bits or less. */
		    byte high_five = (byte) (ch >> 6);
		    byte low_six = (byte) (ch & 0x3F);
		    unsafe.putByte(base, (byte) (high_five | 0xC0)); /* 110xxxxx */
		    unsafe.putByte(base, (byte) (low_six | 0x80));   /* 10xxxxxx */
		    return base + 2;
	    }
		/* possibly full 16 bits. */
		byte high_four = (byte) (ch >> 12);
		byte mid_six = (byte) ((ch >> 6) & 0x3F);
	    byte low_six = (byte) (ch & 0x3f);
	    unsafe.putByte(base, (byte) (high_four | 0xE0)); /* 1110xxxx */
	    unsafe.putByte(base, (byte) (mid_six | 0x80));   /* 10xxxxxx */
	    unsafe.putByte(base, (byte) (low_six | 0x80));   /* 10xxxxxx */
		return base + 3;
	}
	
	private static int utf8Length(char[] base) {
		int result = 0;
		for (int index = 0; index < base.length; index++) {
		    char c = base[index];
		    if ((0x0001 <= c) && (c <= 0x007F)) result += 1;
		    else if (c <= 0x07FF) result += 2;
		    else result += 3;
		}
		return result;
	}
}
