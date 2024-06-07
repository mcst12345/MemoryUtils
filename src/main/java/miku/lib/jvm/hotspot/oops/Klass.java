package miku.lib.jvm.hotspot.oops;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import miku.lib.InternalUtils;
import one.helfy.JVM;
import one.helfy.Type;
import sun.misc.Unsafe;

public class Klass extends Metadata {


    private static final Object2ObjectOpenHashMap<Class<?>, Klass> cache = new Object2ObjectOpenHashMap<>();
    private static final Long2ObjectOpenHashMap<Klass> cache1 = new Long2ObjectOpenHashMap<>();
    private int _layout_helper;
    private Symbol name;
    private AccessFlags _access_flags;
    private int _modifier_flags;
    private Klass _super;
    private Oop _java_mirror;

    Klass(long address) {
        super(address);
        Type type = jvm.type("Klass");
        long _layout_helper_offset = type.offset("_layout_helper");
        _layout_helper = unsafe.getInt(address + _layout_helper_offset);
        long name_offset = type.offset("_name");
        name = new Symbol(unsafe.getAddress(address + name_offset));
        long _access_flags_offset = type.offset("_access_flags");
        _access_flags = new AccessFlags(address + _access_flags_offset);
        long _modifier_flags_offset = type.offset("_modifier_flags");
        _modifier_flags = unsafe.getInt(address + _modifier_flags_offset);
        long _java_mirror_offset = type.offset("_java_mirror");
        _java_mirror = new Oop(unsafe.getAddress(address + _java_mirror_offset));
    }

    Klass(Class<?> clazz) {
        this(jvm.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL);
    }

    public static Klass getKlass(long klass) {
        if (cache1.containsKey(klass)) {
            return cache1.get(klass);
        }
        Klass k = new InstanceKlass(klass);
        cache1.put(klass, k);
        return k;
    }

    public static Klass getKlass(Class<?> clazz) {
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }
        long addr = jvm.intConstant("oopSize") == 8 ? unsafe.getLong(clazz, (long) jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) : unsafe.getInt(clazz, jvm.getInt(jvm.type("java_lang_Class").global("_klass_offset"))) & 0xffffffffL;
        Type type = jvm.findDynamicTypeForAddress(addr, "Metadata");
        Klass klass = new InstanceKlass(clazz);
        cache1.put(klass.getAddress(), klass);
        cache.put(clazz, klass);
        return klass;
    }

    public Oop getMirror() {
        return _java_mirror;
    }

    public Klass getSuper() {
        if (_super == null) {
            _super = new Klass(unsafe.getAddress(getAddress() + jvm.type("Klass").offset("_super")));
        }
        return _super;
    }

    public void setSuper(Klass klass) {
        this._super = klass;
        unsafe.putAddress(getAddress() + jvm.type("Klass").offset("_super"), klass.getAddress());
    }

    public void setSuper(Class<?> clazz) {
        setSuper(getKlass(clazz));
    }

    public int getAccessFlags() {
        return _access_flags.getFlags();
    }

    public void setAccessFlags(int flags) {
        long _modifier_flags_offset = jvm.type("Klass").offset("_modifier_flags");
        unsafe.putInt(getAddress() + _modifier_flags_offset, flags);
        _access_flags.setFlags(flags);
    }

    public String getName() {
        return name.toString();
    }

    //private static final LongSet AllocatedSymbols = new LongOpenHashSet();

    public void setName(String str) {
        JVM jvm = JVM.getInstance();
        Type type = jvm.type("Klass");
        Unsafe unsafe = InternalUtils.getUnsafe();
        long name_offset = type.offset("_name");
        /*long oldSym = unsafe.getAddress(address + name_offset);
        short old_length = unsafe.getShort(oldSym);
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        short neo_length = (short) bytes.length;
        if (old_length > neo_length) {
            System.out.println("Over limit.Allocating memory.");
            long neoSym = unsafe.allocateMemory(neo_length + 8);
            AllocatedSymbols.add(neoSym);
            unsafe.putShort(neoSym, neo_length);
            for (int i = 0; i < neo_length; i++) {
                unsafe.putByte(neoSym + 8 + i, bytes[i]);
            }
            unsafe.putAddress(address + 16L, neoSym);
            if (AllocatedSymbols.contains(oldSym)) {
                System.out.println("We allocated memories before. Freeing them.");
                unsafe.freeMemory(oldSym);
                AllocatedSymbols.remove(oldSym);
            }
        } else {
            unsafe.putShort(oldSym, neo_length);
            for (int i = 0; i < neo_length; i++) {
                unsafe.putByte(oldSym + 8 + i, bytes[i]);
            }
        }
         */
        Symbol symbol = new Symbol(str);
        unsafe.putAddress(getAddress() + name_offset, symbol.getAddress());
        this.name = symbol;
    }

}
