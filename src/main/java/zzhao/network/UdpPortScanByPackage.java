package zzhao.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Random;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.UDPPacket;
import jpcap.util.HexHelper;
import jpcap.util.NetUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class UdpPortScanByPackage {
    private static final Logger logger = Logger.getLogger(UdpPortScanByPackage.class);
    
    public static void main(String[] args) {
        new UdpPortScanByPackage().doScan();
    }

    int testPort = 58888;
    NetworkInterface net;
    Inet4Address address;
    InetAddress target;
    String targetAddress;
    byte[] targetMac;
    
    JpcapCaptor jpcap;
    JpcapSender sender;
    Random random;

    public UdpPortScanByPackage(){
        
    }
    
    private void init() throws Exception{
        net = NetUtil.getActiveDevice();
        address = NetUtil.getIpV4Address(net);
        jpcap = JpcapCaptor.openDevice(net, 2000, false, -1);
        sender = JpcapSender.openRawSocket();
        target = InetAddress.getByName("10.240.140.109");
        targetMac = HexHelper.macToByte("00:0c:29:87:40:9e");
        targetAddress = target.getHostAddress();
        
        //targetMac =  java.net.NetworkInterface.getByInetAddress(target).getHardwareAddress();
        
        random = new Random();
        
        logger.info("local net :" + net.description);
        logger.info("local address : " + address.getHostAddress());
        logger.info("target address : " + target.getHostAddress());
    }
    
    private void doScan(){
        try {
            init();

            Thread recevieThread = new Thread(new AckCheck());
            recevieThread.setDaemon(true);
            recevieThread.start();

            int startPort = 10000;
            int endPort = 10010;

            for(int i=0; i < 3; i++){
                for (int port = startPort; port < endPort; port++) {
                    UDPPacket udpPackage = new UDPPacket(testPort , port);
                    udpPackage.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 5000 + random.nextInt(300), 128, IPPacket.IPPROTO_UDP,
                                               address, target);
                    udpPackage.data = "".getBytes();
                    udpPackage.len = 8;

//                    EthernetPacket ether = new EthernetPacket();
//                    ether.frametype = EthernetPacket.ETHERTYPE_IP;
//                    ether.src_mac = net.mac_address;
//                    ether.dst_mac = targetMac;
//                    udpPackage.datalink = ether;
                    sender.sendPacket(udpPackage);
                    Thread.sleep(1000);
                }
                Thread.sleep(5000);
            }
            Thread.sleep(300000);
            jpcap.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  class AckCheck implements Runnable {

        public void run() {
            try {
                jpcap.loopPacket(-1, new Handler());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Handler implements PacketReceiver {

        public void receivePacket(Packet p) {
            try {
                if(p instanceof ICMPPacket){
                    ICMPPacket pacakge = (ICMPPacket) p;
                    if(StringUtils.equals(targetAddress, pacakge.src_ip.getHostAddress())
                        && pacakge.type == ICMPPacket.ICMP_UNREACH
                        && pacakge.code == ICMPPacket.ICMP_UNREACH_PORT){
                        byte[] datas= pacakge.data;
                        int srcPort = ByteUtils.toInt(datas, 20, 2);
                        int port =  ByteUtils.toInt(datas, 22, 2);
                        System.out.println(port  + " is Closed  and src port is " + srcPort);
                    }
                }else if(p instanceof UDPPacket){
                    UDPPacket pacakge = (UDPPacket) p;
                    if(StringUtils.equals(address.getHostAddress(), pacakge.src_ip.getHostAddress())
                        && StringUtils.equals(target.getHostAddress(), pacakge.dst_ip.getHostAddress())){
//                        System.out.println(pacakge.src_ip);
                        EthernetPacket ether  = (EthernetPacket)pacakge.datalink;
                        System.out.println(HexHelper.toMac(ether.src_mac));
                        System.out.println(HexHelper.toMac(ether.dst_mac));
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
