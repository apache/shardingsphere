package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import lombok.var;

/**
 * @author avalon566
 */
public final class DataTypesCodec {

    public static byte readByte(ByteBuf in) {
        return in.readByte();
    }

    public static void writeByte(byte data, ByteBuf out) {
        out.writeByte(data);
    }

    public static short readShort(ByteBuf in) {
        return in.readShortLE();
    }

    public static void writeShort(short data, ByteBuf out) {
        out.writeShortLE(data);
    }

    public static int readInt(ByteBuf in) {
        return in.readIntLE();
    }

    public static void writeInt(int data, ByteBuf out) {
        out.writeIntLE(data);
    }

    public static byte[] readBytes(int length, ByteBuf in) {
        var buffer = new byte[length];
        in.readBytes(buffer, 0 , length);
        return buffer;
    }

    public static void writeBytes(byte[] data, ByteBuf out) {
        out.writeBytes(data);
    }

    public static String readNullTerminatedString(ByteBuf in) {
        var length = in.bytesBefore((byte) 0x00);
        var data = new byte[length];
        in.readBytes(data, 0, length);
        // null terminated
        in.readByte();
        return new String(data);
    }

    public static void writeNullTerminatedString(String data, ByteBuf out) {
        out.writeBytes(data.getBytes());
        out.writeByte(0x00);
    }

    public static long readLengthCoded(ByteBuf data) {
        int firstByte = data.readByte() & 0xFF;
        switch (firstByte) {
            case 251:
                return -1;
            case 252:
                return data.readShortLE();
            case 253:
                return data.readUnsignedMediumLE();
            case 254:
                return data.readLongLE();
            default:
                return firstByte;
        }
    }

    public static void writeLengthCoded(int l, ByteBuf out) {
        if (l < 252) {
            out.writeByte((byte) l);
        } else if (l < (1 << 16L)) {
            out.writeByte((byte) 252);
            out.writeShortLE(l);
        } else if (l < (1 << 24L)) {
            out.writeByte((byte) 253);
            out.writeMediumLE(l);
        } else {
            out.writeByte((byte) 254);
            out.writeIntLE(l);
        }
    }

    public static String readLengthCodedString(ByteBuf data) {
        var length = (int) readLengthCoded(data);
        var buffer = new byte[length];
        data.readBytes(buffer, 0, length);
        return new String(buffer);
    }

    public static void writeLengthCodedBinary(byte[] data, ByteBuf out) {
        writeLengthCoded(data.length, out);
        out.writeBytes(data);
    }
}
