package net.fornwall.jelf;

/**
 * String table sections hold null-terminated character sequences, commonly called strings.
 * <p>
 * The object file uses these strings to represent symbol and section names.
 * <p>
 * You reference a string as an index into the string table section.
 */
final public class ElfStringTable extends ElfSection {

    public final int numStrings;
    /**
     * The string table data.
     */
    private final byte[] data;

    /**
     * Reads all the strings from [offset, length].
     */
    ElfStringTable(ElfParser parser, long offset, int length, ElfSectionHeader header) throws ElfException {
        super(parser, header);

        parser.seek(offset);
        data = new byte[length];
        int bytesRead = parser.read(data);
        if (bytesRead != length)
            throw new ElfException("Error reading string table (read " + bytesRead + "bytes - expected to " + "read " + data.length + "bytes)");

        int stringsCount = 0;
        for (byte datum : data) if (datum == '\0') stringsCount++;
        numStrings = stringsCount;
    }

    public String get(int index) {
        int endPtr = index;
        while (data[endPtr] != '\0')
            endPtr++;
        return new String(data, index, endPtr - index);
    }
}
