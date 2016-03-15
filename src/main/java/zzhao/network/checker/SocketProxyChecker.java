package zzhao.network.checker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;

import zzhao.network.PortNotOpenException;

/**
 * 检查对应端口是否是socket代理
 * 协议具体内容参考https://zh.wikipedia.org/wiki/SOCKS
 * @author zzhao
 * @version 2016年2月2日
 */
public class SocketProxyChecker {

    private final static byte[] socket5InitBytes = {0x05, 0x03, 0x00, 0x01, 0x02}; // 支持3种认证方式
    private final static byte[] socket4InitBytes = {0x04, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    private final static int CONNECT_TIMEOUT = 5000;
    private final static int READ_TIMEOUT = 3000;

    public static void main(String[] args) {
        try {
            System.out.println(checkSocketProxy("118.163.108.104", 3128));
        } catch (PortNotOpenException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String checkSocketProxy(String host, int port) throws PortNotOpenException {
        if (checkSocket4Proxy(host, port)) {
            return "Socket4";
        }
        
        if (checkSocket5Proxy(host, port)) {
            return "Socket5";
        }
        return null;
    }

    private static boolean checkSocket5Proxy(String host, int port) throws PortNotOpenException {
        boolean ret = false;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setSoTimeout(READ_TIMEOUT);
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
            if (socket.isConnected()) {
                OutputStream os = socket.getOutputStream();
                os.write(socket5InitBytes);
                os.flush();
                InputStream is = socket.getInputStream();
                byte[] tmp = new byte[256];
                int count = is.read(tmp, 0, 10);
                if (count == 2 && tmp[0] == 0x05) {
                    ret = true;
                }
            }
        } catch (ConnectException e) {
            throw new PortNotOpenException();
        } catch (Throwable e) {

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }

    private static boolean checkSocket4Proxy(String host, int port) throws PortNotOpenException {
        boolean ret = false;
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setSoTimeout(READ_TIMEOUT);
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT);
            if (socket.isConnected()) {
                OutputStream os = socket.getOutputStream();
                os.write(socket4InitBytes);
                os.flush();
                InputStream is = socket.getInputStream();
                byte[] tmp = new byte[256];
                int count = is.read(tmp, 0, 10);
                if (count == 8 && tmp[0] == 0x00 && tmp[1] >= 0x5A && tmp[1] <= 0x5D) {
                    ret = true;
                }
            }
        } catch (ConnectException e) {
            throw new PortNotOpenException();
        } catch (Throwable e) {

        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return ret;
    }
}
