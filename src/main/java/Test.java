import me.xdark.shell.JVMUtil;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import sun.misc.Unsafe;

public class Test {

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static void main(String[] args) throws Throwable{
        System.out.println(JVM.type("CodeHeap"));
    }

    public static void m1(){
        System.out.println("Call method from Test");
        Test1.Clazz.method();
    }
}
