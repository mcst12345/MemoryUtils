package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface DebugVC50SymbolEnums {
    byte MACHTYPE_INTEL_8080 = 0;
    byte MACHTYPE_INTEL_8086 = 1;
    byte MACHTYPE_INTEL_80286 = 2;
    byte MACHTYPE_INTEL_80386 = 3;
    byte MACHTYPE_INTEL_80486 = 4;
    byte MACHTYPE_INTEL_PENTIUM = 5;
    byte MACHTYPE_INTEL_PENTIUM_PRO = 6;
    byte MACHTYPE_MIPS_R4000 = 16;
    byte MACHTYPE_MIPS_RESERVED = 17;
    byte MACHTYPE_MIPS_RESERVED2 = 18;
    byte MACHTYPE_MC68000 = 32;
    byte MACHTYPE_MC68010 = 33;
    byte MACHTYPE_MC68020 = 34;
    byte MACHTYPE_MC68030 = 35;
    byte MACHTYPE_MC68040 = 36;
    byte MACHTYPE_ALPHA = 48;
    byte MACHTYPE_PPC601 = 64;
    byte MACHTYPE_PPC603 = 65;
    byte MACHTYPE_PPC604 = 66;
    byte MACHTYPE_PPC620 = 67;
    int COMPFLAG_LANGUAGE_MASK = 16711680;
    int COMPFLAG_LANGUAGE_SHIFT = 16;
    int COMPFLAG_LANGUAGE_C = 0;
    int COMPFLAG_LANGUAGE_CPP = 1;
    int COMPFLAG_LANGUAGE_FORTRAN = 2;
    int COMPFLAG_LANGUAGE_MASM = 3;
    int COMPFLAG_LANGUAGE_PASCAL = 4;
    int COMPFLAG_LANGUAGE_BASIC = 5;
    int COMPFLAG_LANGUAGE_COBOL = 6;
    int COMPFLAG_PCODE_PRESENT_MASK = 32768;
    int COMPFLAG_FLOAT_PRECISION_MASK = 24576;
    int COMPFLAG_FLOAT_PRECISION_SHIFT = 13;
    int COMPFLAG_FLOAT_PRECISION_ANSI_C = 1;
    int COMPFLAG_FLOAT_PACKAGE_MASK = 6144;
    int COMPFLAG_FLOAT_PACKAGE_SHIFT = 11;
    int COMPFLAG_FLOAT_PACKAGE_HARDWARE = 0;
    int COMPFLAG_FLOAT_PACKAGE_EMULATOR = 1;
    int COMPFLAG_FLOAT_PACKAGE_ALTMATH = 2;
    int COMPFLAG_AMBIENT_DATA_MASK = 1792;
    int COMPFLAG_AMBIENT_DATA_SHIFT = 12;
    int COMPFLAG_AMBIENT_CODE_MASK = 224;
    int COMPFLAG_AMBIENT_CODE_SHIFT = 8;
    int COMPFLAG_AMBIENT_MODEL_NEAR = 0;
    int COMPFLAG_AMBIENT_MODEL_FAR = 1;
    int COMPFLAG_AMBIENT_MODEL_HUGE = 2;
    int COMPFLAG_MODE32_MASK = 16;
    short FUNCRET_VARARGS_LEFT_TO_RIGHT_MASK = 1;
    short FUNCRET_RETURNEE_STACK_CLEANUP_MASK = 2;
    byte FUNCRET_VOID = 0;
    byte FUNCRET_IN_REGISTERS = 1;
    byte FUNCRET_INDIRECT_CALLER_NEAR = 2;
    byte FUNCRET_INDIRECT_CALLER_FAR = 3;
    byte FUNCRET_INDIRECT_RETURNEE_NEAR = 4;
    byte FUNCRET_INDIRECT_RETURNEE_FAR = 5;
    byte PROCFLAGS_FRAME_POINTER_OMITTED = 1;
    byte PROCFLAGS_INTERRUPT_ROUTINE = 2;
    byte PROCFLAGS_FAR_RETURN = 4;
    byte PROCFLAGS_NEVER_RETURN = 8;
    byte THUNK_NO_TYPE = 0;
    byte THUNK_ADJUSTOR = 1;
    byte THUNK_VCALL = 2;
    byte THUNK_PCODE = 3;
    short EXMODEL_NOT_CODE = 0;
    short EXMODEL_JUMP_TABLE = 1;
    short EXMODEL_PADDING = 2;
    short EXMODEL_NATIVE = 32;
    short EXMODEL_MICROFOCUS_COBOL = 33;
    short EXMODEL_PADDING_FOR_ALIGNMENT = 34;
    short EXMODEL_CODE = 35;
    short EXMODEL_PCODE = 64;
}
