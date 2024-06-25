package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VMObject;

import java.io.PrintStream;

public class AccessFlags extends VMObject {
    private long flags;

    public AccessFlags(long address) {
        super(address);
        long _flags_offset = jvm.type("AccessFlags").offset("_flags");
        flags = unsafe.getInt(address + _flags_offset);
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
        long _flags_offset = jvm.type("AccessFlags").offset("_flags");
        unsafe.putInt(getAddress() + _flags_offset, flags);
    }

    public boolean isPublic() {
        return (this.flags & 1L) != 0L;
    }

    public boolean isPrivate() {
        return (this.flags & 2L) != 0L;
    }

    public boolean isProtected() {
        return (this.flags & 4L) != 0L;
    }

    public boolean isStatic() {
        return (this.flags & 8L) != 0L;
    }

    public boolean isFinal() {
        return (this.flags & 16L) != 0L;
    }

    public boolean isSynchronized() {
        return (this.flags & 32L) != 0L;
    }

    public boolean isSuper() {
        return (this.flags & 32L) != 0L;
    }

    public boolean isVolatile() {
        return (this.flags & 64L) != 0L;
    }

    public boolean isBridge() {
        return (this.flags & 64L) != 0L;
    }

    public boolean isTransient() {
        return (this.flags & 128L) != 0L;
    }

    public boolean isVarArgs() {
        return (this.flags & 128L) != 0L;
    }

    public boolean isNative() {
        return (this.flags & 256L) != 0L;
    }

    public boolean isEnum() {
        return (this.flags & 16384L) != 0L;
    }

    public boolean isAnnotation() {
        return (this.flags & 8192L) != 0L;
    }

    public boolean isInterface() {
        return (this.flags & 512L) != 0L;
    }

    public boolean isAbstract() {
        return (this.flags & 1024L) != 0L;
    }

    public boolean isStrict() {
        return (this.flags & 2048L) != 0L;
    }

    public boolean isSynthetic() {
        return (this.flags & 4096L) != 0L;
    }

    public long getValue() {
        return this.flags;
    }

    public boolean isMonitorMatching() {
        return (this.flags & 268435456L) != 0L;
    }

    public boolean hasMonitorBytecodes() {
        return (this.flags & 536870912L) != 0L;
    }

    public boolean hasLoops() {
        return (this.flags & 1073741824L) != 0L;
    }

    public boolean loopsFlagInit() {
        return (this.flags & -2147483648L) != 0L;
    }

    public boolean queuedForCompilation() {
        return (this.flags & 16777216L) != 0L;
    }

    public boolean isNotOsrCompilable() {
        return (this.flags & 134217728L) != 0L;
    }

    public boolean hasLineNumberTable() {
        return (this.flags & 1048576L) != 0L;
    }

    public boolean hasCheckedExceptions() {
        return (this.flags & 4194304L) != 0L;
    }

    public boolean hasJsrs() {
        return (this.flags & 8388608L) != 0L;
    }

    public boolean isObsolete() {
        return (this.flags & 65536L) != 0L;
    }

    public boolean hasMirandaMethods() {
        return (this.flags & 268435456L) != 0L;
    }

    public boolean hasVanillaConstructor() {
        return (this.flags & 536870912L) != 0L;
    }

    public boolean hasFinalizer() {
        return (this.flags & 1073741824L) != 0L;
    }

    public boolean isCloneable() {
        return (this.flags & -2147483648L) != 0L;
    }

    public boolean hasLocalVariableTable() {
        return (this.flags & 2097152L) != 0L;
    }

    public boolean fieldAccessWatched() {
        return (this.flags & 8192L) != 0L;
    }

    public boolean fieldModificationWatched() {
        return (this.flags & 32768L) != 0L;
    }

    public boolean fieldHasGenericSignature() {
        return (this.flags & 2048L) != 0L;
    }

    public void printOn(PrintStream tty) {
        if (this.isPublic()) {
            tty.print("public ");
        }

        if (this.isPrivate()) {
            tty.print("private ");
        }

        if (this.isProtected()) {
            tty.print("protected ");
        }

        if (this.isStatic()) {
            tty.print("static ");
        }

        if (this.isFinal()) {
            tty.print("final ");
        }

        if (this.isSynchronized()) {
            tty.print("synchronized ");
        }

        if (this.isVolatile()) {
            tty.print("volatile ");
        }

        if (this.isBridge()) {
            tty.print("bridge ");
        }

        if (this.isTransient()) {
            tty.print("transient ");
        }

        if (this.isVarArgs()) {
            tty.print("varargs ");
        }

        if (this.isNative()) {
            tty.print("native ");
        }

        if (this.isEnum()) {
            tty.print("enum ");
        }

        if (this.isInterface()) {
            tty.print("interface ");
        }

        if (this.isAbstract()) {
            tty.print("abstract ");
        }

        if (this.isStrict()) {
            tty.print("strict ");
        }

        if (this.isSynthetic()) {
            tty.print("synthetic ");
        }

    }

    public int getStandardFlags() {
        return (int)(this.flags & 32767L);
    }
}
