import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.tools.jcore.ClassWriter;
import miku.lib.reflection.ReflectionHelper;
import miku.lib.utils.InternalUtils;
import miku.lib.utils.NoPrivateOrProtected;
import miku.lib.utils.memory.NativeMemoryHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;
import sun.misc.Unsafe;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.Arrays;

public class Test1 implements Opcodes {
    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static void main(String[] args) throws Throwable {
        Clazz inst = new Clazz();
        inst.a = 114514;
        System.out.println("Call method from Test1,first time.");
        Clazz.method();
        InstanceKlass klass = (InstanceKlass) Klass.getKlass(Clazz.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ClassWriter(klass,bos).write();
        byte[] data = bos.toByteArray();
        ClassNode cn = new ClassNode();
        new ClassReader(data).accept(cn,0);
        cn.fields.add(new FieldNode(ACC_PUBLIC,"b","I",null,null));
        for(MethodNode mn : cn.methods){
            if(mn.name.equals("method")){
                mn.localVariables.clear();
                mn.instructions.clear();
                mn.instructions.add(new InsnNode(RETURN));
            }
        }
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
        cn.accept(cw);
        klass.redefineClass(cw.toByteArray(),null);
        System.out.println("Call method from Test1,second time.");
        Clazz.method();
        Test.m1();
        System.out.println(inst.a);
        Field f = Clazz.class.getDeclaredField("b");
        f.set(inst,666);
        System.out.println(f.get(inst));
    }

    public static class Clazz {
        int a;

        public static void method(){
            System.out.println("Called!");
        }
    }
}
