package zzhao.network;

import jpcap.util.HexHelper;

import org.apache.commons.lang.StringUtils;

public class ByteUtils {
    public static int toInt (byte[] datas, int start, int length){
        int value = 0;
        if( start < 0 || length < 0 || start + length > datas.length){
            return value;
        }
        
        for(int i = 0; i < length ; i++){
            int curr = (int) (datas[start + i] & 0xFF) ;
            value = (value << 8 ) + curr;
        }
        
        return value;
    }
    
    public static boolean bytesEqual(byte[] b1, byte[] b2){
        if(b1 == null || b2 == null || b1.length != b2.length){
            return false;
        }
        
        for(int i=0;i<b1.length;i++){
            if(b1[i] != b2[i]){
                return false;
            }
        }
        return true;
    }
    
    public static byte[] ipToMac(String ip){
        byte[] mac = new byte[]{0,0,0,0};
        
        if(StringUtils.isBlank(ip)){
            return mac;
        }
       
        String[] tmps = ip.split("\\.");
        if(tmps.length != 4){
            return mac;
        }
        
        for(int i=0;i<4;i++){
            int value = Integer.valueOf(tmps[i]);
            mac[i] = (byte)(0xFF & value);
        }

        return mac;
    }
    
    public static String byteToIp(byte[] bytes){
        if(bytes == null || bytes.length != 4){
            return null;
        }
        
        StringBuffer buffer = new StringBuffer();
        for(int i=0;i<4;i++){
            int value = 0xFF & bytes[i];
            buffer.append(value);
            if(i < 3){
                buffer.append('.');
            }
        }
        
        return buffer.toString();
    }
    
    public static void main(String[] args) {
//        byte[] test = new byte[]{39,19, -56, 83};
//        System.out.println(toInt(test, 0,2));
//        System.out.println(toInt(test, 2,2));
        byte[] mac = ipToMac("120.140.137.62");
        System.out.println(HexHelper.toString(mac));
        System.out.println(byteToIp(mac));
    }
}
