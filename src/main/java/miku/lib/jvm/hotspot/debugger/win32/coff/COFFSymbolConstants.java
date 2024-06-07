package miku.lib.jvm.hotspot.debugger.win32.coff;

public interface COFFSymbolConstants {
    short IMAGE_SYM_UNDEFINED = 0;
    short IMAGE_SYM_ABSOLUTE = -1;
    short IMAGE_SYM_DEBUG = -2;
    short IMAGE_SYM_TYPE_NULL = 0;
    short IMAGE_SYM_TYPE_VOID = 1;
    short IMAGE_SYM_TYPE_CHAR = 2;
    short IMAGE_SYM_TYPE_SHORT = 3;
    short IMAGE_SYM_TYPE_INT = 4;
    short IMAGE_SYM_TYPE_LONG = 5;
    short IMAGE_SYM_TYPE_FLOAT = 6;
    short IMAGE_SYM_TYPE_DOUBLE = 7;
    short IMAGE_SYM_TYPE_STRUCT = 8;
    short IMAGE_SYM_TYPE_UNION = 9;
    short IMAGE_SYM_TYPE_ENUM = 10;
    short IMAGE_SYM_TYPE_MOE = 11;
    short IMAGE_SYM_TYPE_BYTE = 12;
    short IMAGE_SYM_TYPE_WORD = 13;
    short IMAGE_SYM_TYPE_UINT = 14;
    short IMAGE_SYM_TYPE_DWORD = 15;
    short IMAGE_SYM_DTYPE_NULL = 0;
    short IMAGE_SYM_DTYPE_POINTER = 1;
    short IMAGE_SYM_DTYPE_FUNCTION = 2;
    short IMAGE_SYM_DTYPE_ARRAY = 3;
    byte IMAGE_SYM_CLASS_END_OF_FUNCTION = -1;
    byte IMAGE_SYM_CLASS_NULL = 0;
    byte IMAGE_SYM_CLASS_AUTOMATIC = 1;
    byte IMAGE_SYM_CLASS_EXTERNAL = 2;
    byte IMAGE_SYM_CLASS_STATIC = 3;
    byte IMAGE_SYM_CLASS_REGISTER = 4;
    byte IMAGE_SYM_CLASS_EXTERNAL_DEF = 5;
    byte IMAGE_SYM_CLASS_LABEL = 6;
    byte IMAGE_SYM_CLASS_UNDEFINED_LABEL = 7;
    byte IMAGE_SYM_CLASS_MEMBER_OF_STRUCT = 8;
    byte IMAGE_SYM_CLASS_ARGUMENT = 9;
    byte IMAGE_SYM_CLASS_STRUCT_TAG = 10;
    byte IMAGE_SYM_CLASS_MEMBER_OF_UNION = 11;
    byte IMAGE_SYM_CLASS_UNION_TAG = 12;
    byte IMAGE_SYM_CLASS_TYPE_DEFINITION = 13;
    byte IMAGE_SYM_CLASS_UNDEFINED_STATIC = 14;
    byte IMAGE_SYM_CLASS_ENUM_TAG = 15;
    byte IMAGE_SYM_CLASS_MEMBER_OF_ENUM = 16;
    byte IMAGE_SYM_CLASS_REGISTER_PARAM = 17;
    byte IMAGE_SYM_CLASS_BIT_FIELD = 18;
    byte IMAGE_SYM_CLASS_BLOCK = 100;
    byte IMAGE_SYM_CLASS_FUNCTION = 101;
    byte IMAGE_SYM_CLASS_END_OF_STRUCT = 102;
    byte IMAGE_SYM_CLASS_FILE = 103;
    byte IMAGE_SYM_CLASS_SECTION = 104;
    byte IMAGE_SYM_CLASS_WEAK_EXTERNAL = 105;
}
