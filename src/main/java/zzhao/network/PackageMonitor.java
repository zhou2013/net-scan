package zzhao.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Random;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;
import jpcap.util.HexHelper;
import jpcap.util.NetUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

public class PackageMonitor {

    private static final Logger logger = Logger.getLogger(PackageMonitor.class);

    public static void main(String[] args) {
        new PackageMonitor().doMonitor();
    }

    private NetworkInterface net;

    private Inet4Address address;

    private String localAddress;

    private JpcapCaptor jpcap;

    private InetAddress target;

    private String targetAddress;

    private void init() throws UnknownHostException {
        net = NetUtil.getActiveDevice();
        address = NetUtil.getIpV4Address(net);
        localAddress = address.getHostAddress();
        target = InetAddress.getByName("10.240.136.6");
        targetAddress = target.getHostAddress();
    }

    public void doMonitor() {
        try {
            init();

            logger.info("local address : " + localAddress);
            logger.info("target address : " + targetAddress);

            jpcap = JpcapCaptor.openDevice(net, 2000, false, -1);
            jpcap.loopPacket(-1, new Handler());

            jpcap.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class Handler implements PacketReceiver {

        public void receivePacket(Packet p) {
            try {
                if (p instanceof ARPPacket) {
                    ARPPacket packet = (ARPPacket) p;
                    if(packet.operation == 1){
                        String targetmac = HexHelper.toMac(packet.sender_hardaddr);
                        String targetIp = ByteUtils.byteToIp(packet.sender_protoaddr);
                        if(StringUtils.equals(targetIp, target.getHostAddress())){
                            System.out.println(packet);
                        }
                        System.out.println(targetIp + "   " + targetmac + " " + HexHelper.toMac(packet.target_hardaddr)+ ByteUtils.byteToIp(packet.target_protoaddr)+ " ");
                    }
                }
                
//                if (p instanceof IPPacket) {
//                    IPPacket ipPacket = (IPPacket) p;
//                    if (StringUtils.equals(ipPacket.dst_ip.getHostAddress(), localAddress)) {
//                        if ( ipPacket.offset > 0) {
//                            System.out.println(ipPacket.length + " " + ipPacket.ident + "  " + ipPacket.offset);
//                        }
//                    }
//                }
//                
//                if (p instanceof UDPPacket) {
//                    UDPPacket packet = (UDPPacket) p;
//                    IPPacket ipPacket = (IPPacket) p;
//                    if (StringUtils.equals(packet.dst_ip.getHostAddress(), localAddress)) {
//                        System.out.println(packet.length + "  " +  ipPacket.length);
//                    }
//                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
