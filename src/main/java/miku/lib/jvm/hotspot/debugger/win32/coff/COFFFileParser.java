package miku.lib.jvm.hotspot.debugger.win32.coff;

public class COFFFileParser {
    private static COFFFileParser soleInstance;
    private static final int COFF_HEADER_SIZE = 20;
    private static final int SECTION_HEADER_SIZE = 40;
    private static final int SYMBOL_SIZE = 18;
    private static final int RELOCATION_SIZE = 10;
    private static final int LINE_NUMBER_SIZE = 6;
    private static final String US_ASCII = "US-ASCII";

    private COFFFileParser() {
    }

    public static COFFFileParser getParser(){
        if(soleInstance == null){
            soleInstance = new COFFFileParser();
        }
        return soleInstance;
    }
}
