package zzhao.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class JDWPTest {
    public static void main(String[] args) {
        try{
            Socket socket = new Socket("10.240.137.162", 9003);
            PrintStream out = new PrintStream(socket.getOutputStream());  
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            
            String msg = "JDWP-Handshake\0\0\0\11\0\0\0\1\0\1\1";
            out.println(msg);
            char[] cbuf = new char[200];
            int num = input.read(cbuf);
            System.out.println(new String(cbuf,0 ,num));
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
