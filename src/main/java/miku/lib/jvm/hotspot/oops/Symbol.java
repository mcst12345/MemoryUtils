package miku.lib.jvm.hotspot.oops;

import miku.lib.jvm.hotspot.runtime.VMObject;
import one.helfy.Type;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Symbol extends VMObject {
    private final int _length;
    private int _identity_hash;
    private long _body;

    public Symbol(long address) {
        super(address);
        Type type = jvm.type("Symbol");
        _identity_hash = unsafe.getInt(address + type.offset("_identity_hash"));
        _length = unsafe.getShort(address + type.offset("_length")) & 0xffff;
        _body = address + type.offset("_body");
    }

    public Symbol(String jstring) {
        super(unsafe.allocateMemory(jstring.getBytes(StandardCharsets.UTF_8).length + jvm.type("Symbol").offset("_body")));
        Type type = jvm.type("Symbol");
        byte[] data = jstring.getBytes(StandardCharsets.UTF_8);
        _length = (short) data.length;
        _body = unsafe.getInt(getAddress() + type.offset("_body"));
        unsafe.putShort(getAddress(), (short) _length);
        for (int i = 0; i < _length; i++) {
            unsafe.putByte(_body + i, data[i]);
        }
    }

    private static String readModifiedUTF8(byte[] buf) throws IOException {
        int len = buf.length;
        byte[] tmp = new byte[len + 2];
        tmp[0] = (byte) (len >>> 8 & 255);
        tmp[1] = (byte) (len & 255);
        System.arraycopy(buf, 0, tmp, 2, len);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(tmp));
        return dis.readUTF();
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(jvm.type("oopDesc").fields));
        System.out.println(Arrays.toString(jvm.type("instanceOopDesc").fields));
        System.out.println(Arrays.toString(jvm.type("oop").fields));
    }

    public byte getByteAt(long index) {
        return unsafe.getByte(_body + index);
    }

    public int identityHash() {
        return _identity_hash;
    }

    public byte[] asByteArray() {
        int length = this._length;
        byte[] result = new byte[length];

        for (int index = 0; index < length; ++index) {
            result[index] = this.getByteAt((long) index);
        }

        return result;
    }

    @Override
    public String toString() {
        byte[] data = new byte[_length];
        for (int i = 0; i < _length; i++) {
            data[i] = unsafe.getByte(_body + i);
        }
        try {
            return readModifiedUTF8(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int length() {
        return _length;
    }
}
