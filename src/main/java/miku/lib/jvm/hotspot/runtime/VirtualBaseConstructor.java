package miku.lib.jvm.hotspot.runtime;

import miku.lib.jvm.hotspot.code.*;
import miku.lib.jvm.hotspot.oops.*;
import one.helfy.JVM;
import one.helfy.Type;

import java.util.HashMap;

public class VirtualBaseConstructor<T> {
    public static final VirtualBaseConstructor<?> INSTANCE = new VirtualBaseConstructor<>();
    private static final HashMap<String, Class<? extends VMObject>> map = new HashMap<>();
    private static final Type baseType = JVM.type("Metadata");

    static {
        INSTANCE.addMapping("Metadata", Metadata.class);
        INSTANCE.addMapping("Klass", Klass.class);
        INSTANCE.addMapping("InstanceKlass", InstanceKlass.class);
        INSTANCE.addMapping("InstanceMirrorKlass", InstanceMirrorKlass.class);
        INSTANCE.addMapping("InstanceRefKlass", InstanceRefKlass.class);
        INSTANCE.addMapping("InstanceClassLoaderKlass",InstanceClassLoaderKlass.class);
        INSTANCE.addMapping("TypeArrayKlass",TypeArrayKlass.class);
        INSTANCE.addMapping("ObjArrayKlass",ObjArrayKlass.class);
        INSTANCE.addMapping("Method",Method.class);
        INSTANCE.addMapping("MethodData", MethodData.class);
        INSTANCE.addMapping("ConstMethod", ConstMethod.class);
        INSTANCE.addMapping("ConstantPool", ConstantPool.class);
        INSTANCE.addMapping("ConstantPoolCache", ConstantPoolCache.class);
        INSTANCE.addMapping("BufferBlob", BufferBlob.class);
        INSTANCE.addMapping("nmethod", NMethod.class);
        INSTANCE.addMapping("RuntimeStub", RuntimeStub.class);
        INSTANCE.addMapping("AdapterBlob", AdapterBlob.class);
        INSTANCE.addMapping("MethodHandlesAdapterBlob", MethodHandlesAdapterBlob.class);
        INSTANCE.addMapping("SafepointBlob", SafepointBlob.class);
        INSTANCE.addMapping("DeoptimizationBlob", DeoptimizationBlob.class);
        if(VM.usingServerCompiler){
            INSTANCE.addMapping("ExceptionBlob",ExceptionBlob.class);
            INSTANCE.addMapping("UncommonTrapBlob",UncommonTrapBlob.class);
        }
    }

    public T instantiateWrapperFor(long var1) throws IllegalArgumentException {
        Type type = JVM.getInstance().findDynamicTypeForAddress(var1, baseType);
        if (type != null) {
            if (map.containsKey(type.name)) {
                return (T) VMObjectFactory.newObject(map.get(type.name), var1);
            }
        }
        return null;
    }

    public boolean addMapping(String cTypeName, Class<? extends VMObject> clazz) {
        if (map.get(cTypeName) != null) {
            return false;
        } else {
            map.put(cTypeName, clazz);
            return true;
        }
    }
}
