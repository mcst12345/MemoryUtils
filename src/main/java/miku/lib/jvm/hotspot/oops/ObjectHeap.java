package miku.lib.jvm.hotspot.oops;


import one.helfy.JVM;

public class ObjectHeap {
    private long oopSize = JVM.getInstance().intConstant("oopSize");
    private long byteSize = JVM.getInstance().type("jbyte").size;
    private long charSize = JVM.getInstance().type("jchar").size;
    private long booleanSize = JVM.getInstance().type("jboolean").size;
    private long intSize = JVM.getInstance().type("jint").size;
    private long shortSize = JVM.getInstance().type("jshort").size;
    private long longSize = JVM.getInstance().type("jlong").size;
    private long floatSize = JVM.getInstance().type("jfloat").size;
    private long doubleSize = JVM.getInstance().type("jdouble").size;
}
