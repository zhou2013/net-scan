package zzhao.network.util;

import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author zzhao
 * @version 2016年2月15日
 */
public class ByteUtils {

    public static char toHexChar(byte x) {
        char c = (char) (x & 0xf); // mask low nibble
        return (c > 9 ? (char) (c - 10 + 'a') : (char) (c + '0')); // int to hex char
    }

    public static String toHexString(byte b) {
        StringBuffer sb = new StringBuffer();
        sb.append(toHexChar((byte) (b >> 4)));
        sb.append(toHexChar(b));
        return sb.toString();
    }

    public static String toHexString(byte[] bytes) {
        StringWriter sw = new StringWriter();

        int length = bytes.length;
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                sw.write(toHexString(bytes[i]));
                if (i != length - 1)
                    sw.write(" ");
            }
        }
        return (sw.toString());
    }

    public static String toMac(byte[] bytes) {
        StringWriter sw = new StringWriter();

        int length = bytes.length;
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                sw.write(toHexString(bytes[i]).toUpperCase());
                if (i != length - 1)
                    sw.write(":");
            }
        }
        return (sw.toString());
    }

    public static byte[] macToByte(String mac) {
        if (StringUtils.isBlank(mac) || mac.length() != 17) {
            return null;
        }
        String[] tmp = mac.split(":");
        if (tmp.length != 6) {
            return null;
        }

        byte[] result = new byte[6];
        for (int i = 0; i < 6; i++) {
            String t = tmp[i].toUpperCase();
            result[i] = (byte) (toByte(t.charAt(0)) << 4 | toByte(t.charAt(1)));
        }
        return result;
    }

    public static byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static int toInt(byte[] datas, int start, int length) {
        int value = 0;
        if (start < 0 || length < 0 || start + length > datas.length) {
            return value;
        }

        for (int i = 0; i < length; i++) {
            int curr = (int) (datas[start + i] & 0xFF);
            value = (value << 8) + curr;
        }

        return value;
    }

    public static boolean bytesEqual(byte[] b1, byte[] b2) {
        if (b1 == null || b2 == null || b1.length != b2.length) {
            return false;
        }

        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean bytesEqual(byte[] b1, byte[] b2, int length) {
        if (b1 == null || b2 == null || b1.length < length || b2.length < length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] ipToByte(String ip) {
        byte[] mac = new byte[] {0, 0, 0, 0};

        if (StringUtils.isBlank(ip)) {
            return mac;
        }

        String[] tmps = ip.split("\\.");
        if (tmps.length != 4) {
            return mac;
        }

        for (int i = 0; i < 4; i++) {
            int value = Integer.valueOf(tmps[i]);
            mac[i] = (byte) (0xFF & value);
        }

        return mac;
    }

    public static String byteToIp(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return null;
        }

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            int value = 0xFF & bytes[i];
            buffer.append(value);
            if (i < 3) {
                buffer.append('.');
            }
        }

        return buffer.toString();
    }
}
