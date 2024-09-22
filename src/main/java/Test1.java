import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.tools.jcore.ClassWriter;
import miku.lib.utils.FileUtils;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import sun.misc.Unsafe;

import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;
import java.util.Arrays;

public class Test1 implements Opcodes {
    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static boolean flag = false;

    public static void main(String[] args) throws Throwable {
        Clazz.method();
        unsafe.ensureClassInitialized(JVM.class);
        InstanceKlass klass = (InstanceKlass) Klass.getKlass(Clazz.class);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        new ClassWriter(klass,bos).write();
        byte[] data = bos.toByteArray();
        ClassNode cn = new ClassNode();
        new ClassReader(data).accept(cn,0);
        cn.fields.add(new FieldNode(ACC_PUBLIC,"F1","J",null,null));
        cn.fields.add(new FieldNode(ACC_PUBLIC,"F2","J",null,null));
        cn.fields.add(new FieldNode(ACC_PUBLIC,"F3","J",null,null));
        cn.fields.add(new FieldNode(ACC_PUBLIC,"F4","J",null,null));
        for(MethodNode mn : cn.methods){
            if(mn.name.equals("method")){
                mn.localVariables.clear();
                mn.instructions.clear();
                mn.instructions.add(new FieldInsnNode(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;"));
                mn.instructions.add(new LdcInsnNode("Changed!!!"));
                mn.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/Object;)V",false));
                mn.instructions.add(new FieldInsnNode(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;"));
                mn.instructions.add(new LdcInsnNode("123456789!!!"));
                mn.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/Object;)V",false));
                mn.instructions.add(new FieldInsnNode(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;"));
                mn.instructions.add(new LdcInsnNode("Weeeeeeeeeeeee!!!"));
                mn.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/Object;)V",false));
                mn.instructions.add(new FieldInsnNode(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;"));
                mn.instructions.add(new LdcInsnNode("喵喵喵喵!!!"));
                mn.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/Object;)V",false));
                mn.instructions.add(new FieldInsnNode(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;"));
                mn.instructions.add(new LdcInsnNode("Nyaaaaaaa!!!"));
                mn.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/Object;)V",false));
                mn.instructions.add(new FieldInsnNode(GETSTATIC,"java/lang/System","out","Ljava/io/PrintStream;"));
                mn.instructions.add(new LdcInsnNode("end"));
                mn.instructions.add(new MethodInsnNode(INVOKEVIRTUAL,"java/io/PrintStream","println","(Ljava/lang/Object;)V",false));
                mn.instructions.add(new InsnNode(RETURN));
            }
        }
        org.objectweb.asm.ClassWriter cw = new org.objectweb.asm.ClassWriter(0);
        cn.accept(cw);
        byte[] bytes = cw.toByteArray();
        FileUtils.write(Paths.get("/root/IdeaProjects/Sekai/correct.class"),bytes);
        klass.redefineClass(bytes,null);
        System.out.println("Dump the class....");
        bos = new ByteArrayOutputStream();
        new ClassWriter(klass,bos).write();
        byte[] changed = bos.toByteArray();
        FileUtils.write(Paths.get("/root/IdeaProjects/Sekai/114514.class"),changed);
        System.out.println("Data:");
        System.out.println(Arrays.toString(data));
        System.out.println("Changed:");
        System.out.println(Arrays.toString(changed));
        System.out.println("---");
        System.out.println(data.length);
        System.out.println(changed.length);
        try {
            cn = new ClassNode();
            new ClassReader(changed).accept(cn,0);
            System.out.println(cn.name);
            for(MethodNode mn : cn.methods){
                System.out.println(mn.name + ":" + mn.desc);
            }
            for(FieldNode fn : cn.fields){
                System.out.println(fn.name + ":" + fn.desc);
            }
        } catch (Throwable t){
            System.out.println("FUCK!");
            System.out.println(t.getClass());
            t.printStackTrace();
        }
        System.out.println(klass.getSourceFileName());
        System.out.println(klass.getName());
        System.out.println(klass.getGenericSignature());
        for(int i = 0; i < 20000; i++){
            Test.m1();
            Clazz.method();
        }
        System.out.println(Arrays.toString(Clazz.class.getDeclaredFields()));
        System.out.println("喵喵喵喵喵喵");
    }

    public static class Clazz {
        int a;

        public static void method(){
            System.out.println("Called!");
        }
    }
}
