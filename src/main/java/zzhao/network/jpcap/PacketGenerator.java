package zzhao.network.jpcap;

import java.net.InetAddress;
import java.security.SecureRandom;

import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.TCPPacket;

/**
 * 通用的包生成器
 * @author zzhao
 * @version 2016年2月15日
 */
public class PacketGenerator {

    private static SecureRandom random = new SecureRandom();

    private final static long INIT_SYN = 3234567890L;

    public static TCPPacket generateTcpPacket(byte[] sourceMac, byte[] targetMac) {
        TCPPacket tcpPacket = new TCPPacket(0, 0, 0, 0, false, false, false, false, false, false, false, false, 8192, 0);
        tcpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 5000 + random.nextInt(300), 128,
                                   IPPacket.IPPROTO_TCP, null, null);
        tcpPacket.data = "".getBytes();
        EthernetPacket ether = new EthernetPacket();
        ether.frametype = EthernetPacket.ETHERTYPE_IP;
        ether.src_mac = sourceMac;
        ether.dst_mac = targetMac;
        tcpPacket.datalink = ether;
        return tcpPacket;
    }

    public static void prepareSynPacket(TCPPacket tp, InetAddress target, int destPort) {
        tp.dst_port = destPort;
        tp.dst_ip = target;
        String tmp = tp.dst_ip.getHostAddress() + ":" + tp.dst_port;
        int hashCode = tmp.hashCode() & 0x7FFFFFFF;
        int srcPort = hashCode & 0x4000 + 10000; // 取右边的14位作为发送端口
        long sequence = hashCode >> 14;
        sequence = INIT_SYN + sequence;
        tp.src_port = srcPort;
        tp.sequence = sequence;
        tp.syn = true;
    }

    public static boolean checkTcpPacket(TCPPacket tp) {
        String tmp = tp.src_ip.getHostAddress() + ":" + tp.src_port;
        long hashCode = tmp.hashCode() & 0x7FFFFFFF;
        int receivePort = tp.dst_port;
        long sendPort = hashCode & 0x4000 + 10000;
        long ack = tp.ack_num;
        long sequence = hashCode >> 14;
        sequence = INIT_SYN + sequence;
        if (sendPort == receivePort && ack == sequence + 1) {
            return true;
        }
        return false;
    }
}
