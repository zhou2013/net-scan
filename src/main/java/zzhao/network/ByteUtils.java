package zzhao.network;

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
    
    public static void main(String[] args) {
        byte[] test = new byte[]{39,19, -56, 83};
        System.out.println(toInt(test, 0,2));
        System.out.println(toInt(test, 2,2));
    }
}
