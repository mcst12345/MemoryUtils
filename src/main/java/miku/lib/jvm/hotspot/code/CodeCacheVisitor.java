package miku.lib.jvm.hotspot.code;

public interface CodeCacheVisitor {
    void prologue(long var1, long var2);

    void visit(CodeBlob var1);

    void epilogue();
}
