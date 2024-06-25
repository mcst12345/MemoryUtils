package one.helfy;

public class DisableStructs {
    static final JVM jvm = JVM.getInstance();

    public static void main(String[] args) throws Exception {
        long structs = JVM.getSymbol("gHotSpotVMStructs");
        JVM.putAddress(structs, 0);

        System.out.println("VM Structs disabled. Try jstack -F or jmap -F");
        Thread.currentThread().join();
    }
}
