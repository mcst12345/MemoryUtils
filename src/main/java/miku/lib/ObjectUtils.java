package miku.lib;

import miku.lib.reflection.ReflectionHelper;
import one.helfy.JVM;
import sun.misc.Unsafe;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ObjectUtils {
    static final Map<Field, Long> offsetCache = new ConcurrentHashMap<>();
    static final Map<Field, Object> baseCache = new ConcurrentHashMap<>();
    private static final Unsafe unsafe = InternalUtils.getUnsafe();
    /*public static boolean FromModClass(Object obj) {
        String name = ReflectionHelper.getName(obj.getClass());
        return ModClass(name);
    }

    static boolean win = System.getProperty("os.name").startsWith("Windows");

    public static boolean ModClass(String name) {

        if(name.startsWith("openeye.") || name.startsWith("miku.sekai") || name.startsWith("one.helfy") || name.startsWith("me.xdark")){
            return false;
        }
        String original_name = FMLDeobfuscatingRemapper.INSTANCE.unmap(name.replace('.', '/')).replace('/','.');
        if(!name.equals(original_name)){
            return false;
        }
        final URL res = Launch.classLoader.findResource(original_name.replace('.', '/').concat(".class"));
        if (res != null) {
            String path = res.getPath();
            try {
                path = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            } catch (UnsupportedEncodingException ignored) {
            }

            if (path.contains("!")) {
                path = path.substring(0, path.lastIndexOf("!"));
            }
            if (path.contains("file:/")) {
                path = path.replace("file:/", "");
            }
            if (win) {
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
            }
            return path.contains("/mods/") || path.contains("\\mods\\");
        }
        return false;
    }

     */
    private static final TransformHelper helper = new TransformHelper();
    private static long narrow_klass_base;
    private static long narrow_oop_base;
    private static long narrow_oop_shift;

    static {
        calculateCompressingField();
    }

    public static Object clone(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Class) {
            return o;
        }
        Object copy;
        long offset;
        if (o.getClass().getName().startsWith("[")) {
            Class<?> cls = o.getClass();
            int abo = unsafe.arrayBaseOffset(cls);
            int ais = unsafe.arrayIndexScale(cls);
            switch (cls.getName()) {
                case "[I": {
                    int[] array = new int[((int[]) o).length];
                    int[] old = (int[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[J": {
                    long[] array = new long[((long[]) o).length];
                    long[] old = (long[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[S": {
                    short[] array = new short[((short[]) o).length];
                    short[] old = (short[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[Z": {
                    boolean[] array = new boolean[((boolean[]) o).length];
                    boolean[] old = (boolean[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[F": {
                    float[] array = new float[((float[]) o).length];
                    float[] old = (float[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[D": {
                    double[] array = new double[((double[]) o).length];
                    double[] old = (double[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[C": {
                    char[] array = new char[((char[]) o).length];
                    char[] old = (char[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                case "[B": {
                    byte[] array = new byte[((byte[]) o).length];
                    byte[] old = (byte[]) o;
                    System.arraycopy(old, 0, array, 0, old.length);
                    return array;
                }
                default: {
                    Object array = Array.newInstance(o.getClass().getComponentType(), Array.getLength(o));
                    int length = Array.getLength(o);
                    for (int i = 0; i < length; i++) {
                        long address = ((long) i * ais) + abo;
                        unsafe.putObjectVolatile(array, address, unsafe.getObjectVolatile(o, address));
                    }
                    return array;
                }
            }
        }
        {
            try {
                copy = unsafe.allocateInstance(o.getClass());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        for (Field field : ReflectionHelper.getAllFields(o.getClass())) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (offsetCache.containsKey(field)) {
                offset = offsetCache.get(field);
            } else {
                offset = unsafe.objectFieldOffset(field);
                offsetCache.put(field, offset);
            }
            switch (field.getType().getName()) {
                case "int": {
                    unsafe.putIntVolatile(copy, offset, unsafe.getIntVolatile(o, offset));
                    break;
                }
                case "float": {
                    unsafe.putFloatVolatile(copy, offset, unsafe.getFloatVolatile(o, offset));
                    break;
                }
                case "double": {
                    unsafe.putDoubleVolatile(copy, offset, unsafe.getDoubleVolatile(o, offset));
                    break;
                }
                case "long": {
                    unsafe.putLongVolatile(copy, offset, unsafe.getLongVolatile(o, offset));
                    break;
                }
                case "short": {
                    unsafe.putShortVolatile(copy, offset, unsafe.getShortVolatile(o, offset));
                    break;
                }
                case "boolean": {
                    unsafe.putBooleanVolatile(copy, offset, unsafe.getBooleanVolatile(o, offset));
                    break;
                }
                case "char": {
                    unsafe.putCharVolatile(copy, offset, unsafe.getCharVolatile(o, offset));
                    break;
                }
                case "byte": {
                    unsafe.putByteVolatile(copy, offset, unsafe.getByteVolatile(o, offset));
                    break;
                }
                default: {
                    Object obj = unsafe.getObjectVolatile(o, offset);
                    unsafe.putObjectVolatile(copy, offset, clone(obj));
                }
            }
        }
        return copy;
    }

    public static Object getField(Field field, Object base) {
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                return unsafe.getIntVolatile(base, offset);
            }
            case "float": {
                return unsafe.getFloatVolatile(base, offset);
            }
            case "double": {
                return unsafe.getDoubleVolatile(base, offset);
            }
            case "long": {
                return unsafe.getLongVolatile(base, offset);
            }
            case "short": {
                return unsafe.getShortVolatile(base, offset);
            }
            case "boolean": {
                return unsafe.getBooleanVolatile(base, offset);
            }
            case "char": {
                return unsafe.getCharVolatile(base, offset);
            }
            case "byte": {
                return unsafe.getByteVolatile(base, offset);
            }
            default: {
                return unsafe.getObjectVolatile(base, unsafe.objectFieldOffset(field));
            }
        }
    }

    /*public static void resetStatic(){
        ModMain.LOGGER.info("resetStatic method entered.");
        ThreadUtils.killThreads();
        Vector<Class<?>> targets = new Vector<>(Launch.classLoader.classes);
        targets.removeIf(c -> !ModClass(ReflectionHelper.getName(c)));
        for(Class<?> clazz : targets){
            ModMain.LOGGER.info("Class:{}", ReflectionHelper.getName(clazz));
            for(Field field : ReflectionHelper.getFields(clazz)){
                if(Modifier.isStatic(field.getModifiers())){
                    Object obj = getStatic(field);
                    if(obj instanceof List){
                        ModMain.LOGGER.info("Clear:{}", field.getName());
                        try {
                            ((List<?>) obj).clear();
                        } catch (Throwable ignored){}
                    } else if(obj instanceof Map){
                        ModMain.LOGGER.info("Clear:{}", field.getName());
                        try {
                            ((Map<?, ?>) obj).clear();
                        } catch (Throwable ignored){}
                    } else if(obj instanceof Set){
                        ModMain.LOGGER.info("Clear:{}", field.getName());
                        try {
                            ((Set<?>) obj).clear();
                        } catch (Throwable ignored){}
                    } else if(obj instanceof Entity || obj instanceof NBTTagCompound){
                        ModMain.LOGGER.info("Null:{}", field.getName());
                        putStatic(field,null);
                    } else if(field.getType() == boolean.class){
                        ModMain.LOGGER.info("False:{}", field.getName());
                        ObjectUtils.putStatic(field,false);
                    }
                }
            }
        }
        MinecraftForge.EVENT_BUS.listenerOwners.forEach((k,v) -> {
            if(v.getModId() != null && !v.getModId().equals("forge") && !v.getModId().equals("sekai") && !v.getModId().equals("openeye") && !v.getModId().equals("jei")){
                ModMain.LOGGER.info("{}:{}", v.getModId(), k.getClass().getName());
                resetObjectFields(k);
                resetObjectFields(v);
            }
        });
    }

    private static void resetObjectFields(Object target){
        Class<?> clazz = target.getClass();
        for(Field field : ReflectionHelper.getFields(clazz)){
            if(!Modifier.isStatic(field.getModifiers())){
                if(field.getType() == boolean.class){
                    ModMain.LOGGER.info("False:{}", field.getName());
                    putField(field,target,false);
                } else {
                    Object obj = getField(field,target);
                    if(obj instanceof List){
                        ModMain.LOGGER.info("Clear:{}", field.getName());
                        try {
                            ((List<?>) obj).clear();
                        } catch (Throwable ignored){}
                    } else if(obj instanceof Map){
                        ModMain.LOGGER.info("Clear:{}", field.getName());
                        try {
                            ((Map<?, ?>) obj).clear();
                        } catch (Throwable ignored){}
                    } else if(obj instanceof Set){
                        ModMain.LOGGER.info("Clear:{}", field.getName());
                        try {
                            ((Set<?>) obj).clear();
                        } catch (Throwable ignored){}
                    }
                }
            }
        }
    }

     */

    public static Object getStatic(Field field) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = unsafe.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.staticFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                return unsafe.getIntVolatile(base, offset);
            }
            case "float": {
                return unsafe.getFloatVolatile(base, offset);
            }
            case "double": {
                return unsafe.getDoubleVolatile(base, offset);
            }
            case "long": {
                return unsafe.getLongVolatile(base, offset);
            }
            case "short": {
                return unsafe.getShortVolatile(base, offset);
            }
            case "boolean": {
                return unsafe.getBooleanVolatile(base, offset);
            }
            case "char": {
                return unsafe.getCharVolatile(base, offset);
            }
            case "byte": {
                return unsafe.getByteVolatile(base, offset);
            }
            default: {
                return unsafe.getObjectVolatile(base, unsafe.staticFieldOffset(field));
            }
        }

    }

    public static void putField(Field field, Object base, Object obj) {
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                unsafe.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                unsafe.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                unsafe.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                unsafe.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                unsafe.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                unsafe.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                unsafe.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                unsafe.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                unsafe.putObjectVolatile(base, offset, obj);
                break;
            }
        }
    }

    public static void putStatic(Field field, Object obj) {
        Object base;
        if (baseCache.containsKey(field)) {
            base = baseCache.get(field);
        } else {
            base = unsafe.staticFieldBase(field);
            baseCache.put(field, base);
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = unsafe.staticFieldOffset(field);
            offsetCache.put(field, offset);
        }
        switch (field.getType().getName()) {
            case "int": {
                unsafe.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                unsafe.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                unsafe.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                unsafe.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                unsafe.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                unsafe.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                unsafe.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                unsafe.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                unsafe.putObjectVolatile(base, offset, obj);
                break;
            }
        }
    }

    public static void printAddresses(Object... objects) {
        System.out.print("0x");
        long last;
        int offset = unsafe.arrayBaseOffset(objects.getClass());
        int scale = unsafe.arrayIndexScale(objects.getClass());
        switch (scale) {
            case 4:
                long factor = unsafe.addressSize() == 8 ? 8 : 1;
                final long i1 = (unsafe.getIntVolatile(objects, offset) & 0xFFFFFFFFL) * factor;
                System.out.print(Long.toHexString(i1));
                last = i1;
                for (int i = 1; i < objects.length; i++) {
                    final long i2 = (unsafe.getIntVolatile(objects, offset + i * 4L) & 0xFFFFFFFFL) * factor;
                    if (i2 > last)
                        System.out.print(", +" + Long.toHexString(i2 - last));
                    else
                        System.out.print(", -" + Long.toHexString(last - i2));
                    last = i2;
                }
                break;
            case 8:
                throw new AssertionError("Not supported");
        }
        System.out.println();
    }

    public static void fillValue(Field field, Object o, Object obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        Object base = null;
        if (isStatic) {
            if (baseCache.containsKey(field)) {
                base = baseCache.get(field);
            } else {
                base = unsafe.staticFieldBase(field);
                baseCache.put(field, base);
            }
        }
        long offset;
        if (offsetCache.containsKey(field)) {
            offset = offsetCache.get(field);
        } else {
            offset = isStatic ? unsafe.staticFieldOffset(field) : unsafe.objectFieldOffset(field);
            offsetCache.put(field, offset);
        }
        base = base == null ? o : base;
        if (base == null) {
            throw new IllegalArgumentException("The fuck?");
        }
        if (field.getType().getName().startsWith("[")) {
            int abo = unsafe.arrayBaseOffset(field.getType());
            int ais = unsafe.arrayIndexScale(field.getType());
            switch (field.getType().getName()) {
                case "[I": {
                    int[] array = new int[((int[]) obj).length];
                    int[] old = (int[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[J": {
                    long[] array = new long[((long[]) obj).length];
                    long[] old = (long[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[S": {
                    short[] array = new short[((short[]) obj).length];
                    short[] old = (short[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[Z": {
                    boolean[] array = new boolean[((boolean[]) obj).length];
                    boolean[] old = (boolean[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[F": {
                    float[] array = new float[((float[]) obj).length];
                    float[] old = (float[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[D": {
                    double[] array = new double[((double[]) obj).length];
                    double[] old = (double[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[C": {
                    char[] array = new char[((char[]) obj).length];
                    char[] old = (char[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                case "[B": {
                    byte[] array = new byte[((byte[]) obj).length];
                    byte[] old = (byte[]) obj;
                    System.arraycopy(old, 0, array, 0, old.length);
                    unsafe.putObjectVolatile(base, offset, array);
                    break;
                }
                default: {
                    Object array = Array.newInstance(field.getType().getComponentType(), Array.getLength(obj));
                    int length = Array.getLength(obj);
                    for (int i = 0; i < length; i++) {
                        long address = ((long) i * ais) + abo;
                        unsafe.putObjectVolatile(array, address, unsafe.getObjectVolatile(obj, address));
                    }
                    break;
                }
            }
            return;
        }
        switch (field.getType().getName()) {
            case "int": {
                unsafe.putIntVolatile(base, offset, (int) obj);
                break;
            }
            case "float": {
                unsafe.putFloatVolatile(base, offset, (float) obj);
                break;
            }
            case "double": {
                unsafe.putDoubleVolatile(base, offset, (double) obj);
                break;
            }
            case "long": {
                unsafe.putLongVolatile(base, offset, (long) obj);
                break;
            }
            case "short": {
                unsafe.putShortVolatile(base, offset, (short) obj);
                break;
            }
            case "boolean": {
                unsafe.putBooleanVolatile(base, offset, (boolean) obj);
                break;
            }
            case "char": {
                unsafe.putCharVolatile(base, offset, (char) obj);
                break;
            }
            case "byte": {
                unsafe.putByteVolatile(base, offset, (byte) obj);
                break;
            }
            default: {
                unsafe.putObjectVolatile(base, offset, clone(obj));
                break;
            }
        }
    }

    static int encodeKlass(long klass) {
        if (unsafe.pageSize() == 8)
            return (int) klass;
        return (int) (klass - narrow_klass_base >> 3);
    }

    public static void setObjectClass(Object target, Class<?> clazz) {
        JVM jvm = JVM.getInstance();
        int oopSize = jvm.intConstant("oopSize");
        long klassOffset = jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"));
        long klass = oopSize == 8 ? unsafe.getLong(clazz, klassOffset) : unsafe.getInt(clazz, klassOffset) & 0xffffffffL;
        unsafe.putLongVolatile(target, 8L, encodeKlass(klass));
    }

    static int log2(long n) {
        return (int) (Math.log(n) / Math.log(2));
    }

    /**
     * Calculates pointer compressing bases/shifts if we are in a 64-bit JVM.
     */
    static void calculateCompressingField() {
        if (unsafe.pageSize() == 8) {
            int encoded = unsafe.getInt(new Object(), 8L);
            long decoded = unsafe.getLong(Object.class, 72L);
            narrow_klass_base = decoded - ((long) encoded << 3);

            int encoded1 = addressNarrow(Object.class);
            long decoded1 = unsafe.getLong(
                    unsafe.getLong(Object.class, 72L) // C++ vtbl ptr
                            + Unsafe.ADDRESS_SIZE // _layout_helper
                            + 4 // _super_check_offset
                            + 4 // _name
                            + Unsafe.ADDRESS_SIZE // _secondary_super_cache
                            + Unsafe.ADDRESS_SIZE // _secondary_supers
                            + Unsafe.ADDRESS_SIZE // primary_supers
                            + Unsafe.ADDRESS_SIZE * 8L // _java_mirror
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
                            + Unsafe.ADDRESS_SIZE * 8L // _java_mirror
            );
            narrow_oop_shift = log2((decoded2 - decoded1) / (encoded2 - encoded1));
            narrow_oop_base = decoded1 - ((long) encoded1 << narrow_oop_shift);
        }
    }

    static int addressNarrow(Object obj) {
        helper.changeContent(obj);
        return unsafe.getIntVolatile(helper, 12L);
    }

    static long decodeOop(int narrowOop) {
        if (!(unsafe.pageSize() == 8))
            return narrowOop;
        return narrow_oop_base + ((long) narrowOop << narrow_oop_shift);
    }

    public static long location(Object object) {

        Object[] array = new Object[]{object};


        long baseOffset = unsafe.arrayBaseOffset(Object[].class);

        int addressSize = unsafe.addressSize();

        long location;

        switch (addressSize) {

            case 4:

                location = unsafe.getInt(array, baseOffset);

                break;

            case 8:

                location = unsafe.getLong(array, baseOffset);

                break;

            default:

                throw new Error("unsupported address size: " + addressSize);

        }

        return (location) * 8L;

    }

    private static Object object(int narrow_oop) {
        helper.changeContent(narrow_oop);
        return helper.content;
    }

    public static void main(String[] args) {
        Object o = new Object();
        int addr = addressNarrow(o);
        System.out.println(narrow_oop_base);
        System.out.println(narrow_oop_shift);
        System.out.println(narrow_klass_base);
        System.out.println(object(addr));
    }

    static class TransformHelper {
        volatile Object content;

        TransformHelper() {
        }

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
}
