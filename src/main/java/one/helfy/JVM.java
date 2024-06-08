package one.helfy;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.xdark.shell.JVMUtil;
import me.xdark.shell.NativeLibrary;
import miku.lib.InternalUtils;
import miku.lib.jvm.hotspot.debugger.windbg.DLL;
import net.fornwall.jelf.ElfFile;
import net.fornwall.jelf.ElfSymbol;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public final class JVM {
    private static final Unsafe unsafe = InternalUtils.getUnsafe();
    private static final NativeLibrary JVM;
    private static final boolean linux = System.getProperty("os.name").trim().toLowerCase().contains("linux");
    private static JVM jvm;

    static {
        try {
            JVM = JVMUtil.findJvm();
        } catch (Throwable t) {
            throw new ExceptionInInitializerError(t);
        }
    }

    private final Map<Type, Long> typeToVtblMap = new HashMap<>();
    private final Object2LongOpenHashMap<Type> typeToVtbl = new Object2LongOpenHashMap();
    private final Map<String, Type> types = new LinkedHashMap<>();
    private final Map<String, Number> constants = new LinkedHashMap<>();
    private String vt;

    private JVM() {
        readVmTypes(readVmStructs());
        readVmIntConstants();
        readVmLongConstants();

        if (linux) {
            try {
                getSymbol("__vt_10JavaThread");
                this.vt = "__vt_";
            } catch (NoSuchElementException e) {
                this.vt = "_ZTV";
            }
        } else {

        }
    }

    public static JVM getInstance() {
        if (jvm == null) {
            jvm = new JVM();
        }
        return jvm;
    }

    public static long SymbolOffset(String name) {
        File f = JVMUtil.LIBJVM.toFile();
        if (linux) {
            try {
                ElfFile elf = ElfFile.from(f);
                ElfSymbol s = elf.getELFSymbol(name);
                if (s == null) {
                    throw new NoSuchElementException();
                }
                return s.offset;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return DLL.lookupSymbolOffset(name);
        }

    }

    private String vtblSymbolForType(Type type) {
        if (linux) {
            return vt + type.name.length() + type.name;
        } else {
            return "??_7" + type.name + "@@6B@";
        }
    }

    public long getVtblForType(Type type) {
        if (type == null) {
            return 0;
        } else {
            if (typeToVtblMap.containsKey(type)) {
                return typeToVtblMap.get(type);
            } else {
                String vtblSymbol = this.vtblSymbolForType(type);
                try {
                    long addr = getSymbol(vtblSymbol);
                    this.typeToVtblMap.put(type, addr);
                    return addr;
                } catch (NoSuchElementException e) {
                    e.printStackTrace();
                    this.typeToVtblMap.put(type, 0L);
                    return 0;
                }
            }
        }
    }

    public long vtblForType(Type type) {
        if (!typeToVtbl.containsKey(type)) {
            long vtblAddr = typeToVtbl.getLong(type);
            if (vtblAddr == 0) {
                vtblAddr = getVtblForType(type);
                if (vtblAddr != 0) {
                    typeToVtbl.put(type, vtblAddr);
                }
            }

            return vtblAddr;
        }
        return typeToVtbl.getLong(type);
    }

    public Type findDynamicTypeForAddress(long addr, Type baseType) {
        if (this.vtblForType(baseType) == 0) {
            throw new InternalError(baseType + " does not appear to be polymorphic");
        } else {
            long loc1 = unsafe.getAddress(addr);

            long loc2 = 0;
            long loc3 = 0;
            long offset2 = baseType.size;
            offset2 = offset2 - offset2 % unsafe.pageSize() - unsafe.pageSize();
            if (offset2 > 0L) {
                loc2 = unsafe.getAddress(addr + offset2);
            }
            long offset3 = offset2 - unsafe.pageSize();
            if (offset3 > 0L) {
                loc3 = unsafe.getAddress(addr + offset3);
            }


            Type loc2Match = null;
            Type loc3Match = null;
            Iterator<Type> iter = this.getTypes();

            while (iter.hasNext()) {
                Type type = iter.next();

                Type superClass;
                for (superClass = type; !Objects.equals(superClass, baseType) && superClass != null; superClass = type(superClass.superName)) {
                }

                if (superClass != null) {
                    long vtblAddr = this.vtblForType(type) - (linux ? 941672 : 0);
                    if (vtblAddr != 0) {
                        if (vtblAddr == loc1) {
                            return type;
                        }

                        if (loc2 != 0 && loc2Match == null && vtblAddr == (loc2)) {
                            loc2Match = type;
                        }

                        if (loc3 != 0 && loc3Match == null && vtblAddr == (loc3)) {
                            loc3Match = type;
                        }
                    }
                }
            }

            if (loc2Match != null) {
                return loc2Match;
            } else return loc3Match;
        }
    }

    public Iterator<Type> getTypes() {
        return types.values().iterator();
    }

    public void print(boolean Types, boolean Constants, boolean fields) {
        if (Types) {
            types.forEach((s, v) -> {
                System.out.println(s);
                System.out.println("Super:" + v.superName);
                if (fields) {
                    System.out.println("Fields:" + Arrays.toString(v.fields));
                }
                System.out.println("vtbl" + vtblForType(v));
                System.out.println();
            });
        }
        if (Constants) {
            constants.forEach((s, v) -> {
                System.out.println(s);
                System.out.println();
            });
        }
    }

    public <T> T getObject(T obj, long addr) {
        return (T) unsafe.getObject(obj, addr);
    }

    public long fieldOffset(java.lang.reflect.Field field) {
        return unsafe.objectFieldOffset(field);
    }

    public Type findDynamicTypeForAddress(long addr, String type) {
        return findDynamicTypeForAddress(addr, type(type));
    }

    private Map<String, Set<Field>> readVmStructs() {
        long entry = getSymbol("gHotSpotVMStructs");
        long typeNameOffset = getSymbol("gHotSpotVMStructEntryTypeNameOffset");
        long fieldNameOffset = getSymbol("gHotSpotVMStructEntryFieldNameOffset");
        long typeStringOffset = getSymbol("gHotSpotVMStructEntryTypeStringOffset");
        long isStaticOffset = getSymbol("gHotSpotVMStructEntryIsStaticOffset");
        long offsetOffset = getSymbol("gHotSpotVMStructEntryOffsetOffset");
        long addressOffset = getSymbol("gHotSpotVMStructEntryAddressOffset");
        long arrayStride = getSymbol("gHotSpotVMStructEntryArrayStride");

        Map<String, Set<Field>> structs = new HashMap<>();

        for (; ; entry += arrayStride) {
            String typeName = getStringRef(entry + typeNameOffset);
            String fieldName = getStringRef(entry + fieldNameOffset);
            if (fieldName == null) break;

            String typeString = getStringRef(entry + typeStringOffset);
            boolean isStatic = getInt(entry + isStaticOffset) != 0;
            long offset = getLong(entry + (isStatic ? addressOffset : offsetOffset));

            Set<Field> fields = structs.computeIfAbsent(typeName, k -> new TreeSet<>());
            fields.add(new Field(fieldName, typeString, offset, isStatic));
        }

        return structs;
    }

    private void readVmTypes(Map<String, Set<Field>> structs) {
        long entry = getSymbol("gHotSpotVMTypes");
        long typeNameOffset = getSymbol("gHotSpotVMTypeEntryTypeNameOffset");
        long superclassNameOffset = getSymbol("gHotSpotVMTypeEntrySuperclassNameOffset");
        long isOopTypeOffset = getSymbol("gHotSpotVMTypeEntryIsOopTypeOffset");
        long isIntegerTypeOffset = getSymbol("gHotSpotVMTypeEntryIsIntegerTypeOffset");
        long isUnsignedOffset = getSymbol("gHotSpotVMTypeEntryIsUnsignedOffset");
        long sizeOffset = getSymbol("gHotSpotVMTypeEntrySizeOffset");
        long arrayStride = getSymbol("gHotSpotVMTypeEntryArrayStride");

        for (; ; entry += arrayStride) {
            String typeName = getStringRef(entry + typeNameOffset);
            if (typeName == null) break;

            String superclassName = getStringRef(entry + superclassNameOffset);
            boolean isOop = getInt(entry + isOopTypeOffset) != 0;
            boolean isInt = getInt(entry + isIntegerTypeOffset) != 0;
            boolean isUnsigned = getInt(entry + isUnsignedOffset) != 0;
            int size = getInt(entry + sizeOffset);

            Set<Field> fields = structs.get(typeName);
            types.put(typeName, new Type(typeName, superclassName, size, isOop, isInt, isUnsigned, fields));
        }
    }

    private static long libBase = 0;

    public long getLibBase() {
        if(libBase != 0){
            return libBase;
        }
        if (linux) {
            try {
                FileReader fin = new FileReader("/proc/self/maps");
                BufferedReader reader = new BufferedReader(fin);
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] splits = line.trim().split(" ");
                    if (line.endsWith("libjvm.so")) {
                        String[] addr_range = splits[0].split("-");
                        libBase = Long.parseLong(addr_range[0], 16);
                        return libBase;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            long offset = SymbolOffset("??_7InstanceKlass@@6B@");
            long vtbl = unsafe.getAddress(jvm.intConstant("oopSize") == 8 ? unsafe.getLong(Object.class, (long) jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(Object.class, jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL);
            libBase = vtbl - offset;
            return libBase;
        }
        throw new JVMException("Cannot find libbase!");
    }

    private void readVmIntConstants() {
        long entry = getSymbol("gHotSpotVMIntConstants");
        long nameOffset = getSymbol("gHotSpotVMIntConstantEntryNameOffset");
        long valueOffset = getSymbol("gHotSpotVMIntConstantEntryValueOffset");
        long arrayStride = getSymbol("gHotSpotVMIntConstantEntryArrayStride");

        for (; ; entry += arrayStride) {
            String name = getStringRef(entry + nameOffset);
            if (name == null) break;

            int value = getInt(entry + valueOffset);
            constants.put(name, value);
        }
    }

    private void readVmLongConstants() {
        long entry = getSymbol("gHotSpotVMLongConstants");
        long nameOffset = getSymbol("gHotSpotVMLongConstantEntryNameOffset");
        long valueOffset = getSymbol("gHotSpotVMLongConstantEntryValueOffset");
        long arrayStride = getSymbol("gHotSpotVMLongConstantEntryArrayStride");

        for (; ; entry += arrayStride) {
            String name = getStringRef(entry + nameOffset);
            if (name == null) break;

            long value = getLong(entry + valueOffset);
            constants.put(name, value);
        }
    }

    public byte getByte(long addr) {
        return unsafe.getByte(addr);
    }

    public void putByte(long addr, byte val) {
        unsafe.putByte(addr, val);
    }

    public short getShort(long addr) {
        return unsafe.getShort(addr);
    }

    public void putShort(long addr, short val) {
        unsafe.putShort(addr, val);
    }

    public int getInt(long addr) {
        return unsafe.getInt(addr);
    }

    public void putInt(long addr, int val) {
        unsafe.putInt(addr, val);
    }

    public long getLong(long addr) {
        return unsafe.getLong(addr);
    }

    public void putLong(long addr, long val) {
        unsafe.putLong(addr, val);
    }

    public long getAddress(long addr) {
        return unsafe.getAddress(addr);
    }

    public void putAddress(long addr, long val) {
        unsafe.putAddress(addr, val);
    }

    public String getString(long addr) {
        if (addr == 0) {
            return null;
        }

        char[] chars = new char[40];
        int offset = 0;
        for (byte b; (b = getByte(addr + offset)) != 0; ) {
            if (offset >= chars.length) chars = Arrays.copyOf(chars, offset * 2);
            chars[offset++] = (char) b;
        }
        return new String(chars, 0, offset);
    }

    public String getStringRef(long addr) {
        return getString(getAddress(addr));
    }

    public long getSymbol(String name) {
        long address = JVM.findEntry(name);
        if (address == 0) {
            address = getLibBase() + SymbolOffset(name);
            if (address == 0) {
                throw new NoSuchElementException("No such symbol: " + name);
            }
            return address;
        }
        if(name.startsWith("??_7") && name.endsWith("@@6B@")){
            return address;
        }
        return getLong(address);
    }

    public Type type(String name) {
        Type type = types.get(name);
        if (type == null) {
            return null;
            //throw new NoSuchElementException("No such type: " + name);
        }
        return type;
    }

    public Number constant(String name) {
        Number constant = constants.get(name);
        if (constant == null) {
            throw new NoSuchElementException("No such constant: " + name);
        }
        return constant;
    }

    public int intConstant(String name) {
        return constant(name).intValue();
    }

    public long longConstant(String name) {
        return constant(name).longValue();
    }

    /*
     * This class was tested under permanent System.gc calls and doesn't seem to crash JVM due to object relocations
     */
    public static class Ptr2Obj {
        private static final JVM jvm = one.helfy.JVM.getInstance();
        private static final long _narrow_oop_base = jvm.getAddress(jvm.type("Universe").global("_narrow_oop._base"));
        private static final int _narrow_oop_shift = jvm.getInt(jvm.type("Universe").global("_narrow_oop._shift"));
        private static final long objFieldOffset;

        static {
            try {
                java.lang.reflect.Field objField = Ptr2Obj.class.getDeclaredField("obj");
                objFieldOffset = jvm.fieldOffset(objField);
            } catch (NoSuchFieldException e) {
                throw new JVMException("Couldn't obtain obj field of own class");
            }
        }

        private volatile Object obj;

        public static Object getFromPtr2Ptr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) ((jvm.getAddress(address) - _narrow_oop_base) >> _narrow_oop_shift));
            return ptr2Obj.obj;
        }

        public static Object getFromPtr2NarrowPtr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) jvm.getAddress(address));
            return ptr2Obj.obj;
        }

        public static int narrowKlassAddress(long address) {
            return (int) ((address - _narrow_oop_base) >> _narrow_oop_shift);
        }

        public static Object getFromPtr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) ((address - _narrow_oop_base) >> _narrow_oop_shift));
            return ptr2Obj.obj;
        }

        public static Object getFromNarrowPtr(long address) {
            if (address == 0) {
                return null;
            }
            Ptr2Obj ptr2Obj = new Ptr2Obj();
            unsafe.compareAndSwapInt(ptr2Obj, objFieldOffset, 0, (int) address);
            return ptr2Obj.obj;
        }
    }
}
