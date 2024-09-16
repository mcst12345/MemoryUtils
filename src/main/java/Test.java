import com.sun.jna.Function;
import com.sun.jna.Pointer;
import me.xdark.shell.JVMUtil;
import miku.lib.jvm.hotspot.oops.InstanceKlass;
import miku.lib.jvm.hotspot.oops.Klass;
import miku.lib.jvm.hotspot.oops.Method;
import miku.lib.jvm.hotspot.utilities.MethodArray;
import miku.lib.utils.InternalUtils;
import one.helfy.JVM;
import sun.misc.Unsafe;

public class Test {

    private static final Unsafe unsafe = InternalUtils.getUnsafe();

    public static void main(String[] args) throws Throwable{
        System.out.println(JVMUtil.findJvm().findEntry("Unsafe_GetNativeInt"));
    }

    public static void m1(){
        System.out.println("Call method from Test");
        Test1.Clazz.method();
    }
}
