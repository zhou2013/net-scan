/*
 * @(#) TcpPortSacnByPackage.java 2014-7-7
 * 
 * Copyright 2013 NetEase.com, Inc. All rights reserved.
 */
package zzhao.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;
import jpcap.util.NetUtil;


/**
 * 模拟tcp建立链接过程的形式来扫描端口
 * @author hzzhaozhou
 * @version 2014-7-7
 */
public class TcpPortSacnByPackage {
    private static final Logger logger = Logger.getLogger(TcpPortSacnByPackage.class);
    
    private static final int testPort = 58888;

    public static void main(String[] args) {
        try {
            NetworkInterface net = NetUtil.getActiveDevice();
            Inet4Address address = NetUtil.getIpV4Address(net);
            
            logger.info("local address : " + address.getHostAddress());
            
            JpcapCaptor jpcap = JpcapCaptor.openDevice(net, 2000, true, -1);

            Thread recevieThread = new Thread(new AckCheck(jpcap,address.getHostAddress()));
            recevieThread.setDaemon(true);
            recevieThread.start();

            InetAddress target = InetAddress.getByName("www.baidu.com");
            int startPort = 80;
            int endPort = 10000;
            
            JpcapSender sender = JpcapSender.openDevice(net);

            for (int port = startPort; port < endPort; port++) {
                TCPPacket tcpPacket = new TCPPacket(testPort, port, 356233164L, 0, false, false, false, false, true, false, false,
                                false, 8192, 0);
                tcpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 6244, 128, IPPacket.IPPROTO_TCP, address, target);
                tcpPacket.data = "".getBytes();
                
                EthernetPacket ether=new EthernetPacket(); 
                ether.frametype=EthernetPacket.ETHERTYPE_IP; 
                ether.src_mac=net.mac_address;
                ether.dst_mac=new byte[]{(byte)0,(byte)6,(byte)7,(byte)8,(byte)9,(byte)10}; 
                tcpPacket.datalink=ether;
                
                sender.sendPacket(tcpPacket);
            }
            Thread.sleep(30000);
            jpcap.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static class AckCheck implements Runnable {

        private JpcapCaptor jpcap;
        private String localAddress;

        public AckCheck(JpcapCaptor jpcap, String localAddress) {
            this.jpcap = jpcap;
            this.localAddress = localAddress;
        }

        public void run() {
            try {
                jpcap.loopPacket(-1, new Handler(localAddress));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static class Handler implements PacketReceiver {

        private String localAddress;
        
        public Handler( String localAddress) {
            this.localAddress = localAddress;
        }
        
        public void receivePacket(Packet p) {
            try {
                if (p instanceof TCPPacket) {
                    TCPPacket pacakge = (TCPPacket) p;
                    if(StringUtils.equals(localAddress, pacakge.dst_ip.getHostAddress())){
                        if (pacakge.ack == true && pacakge.syn == true && pacakge.dst_port == testPort) {
                            System.out.println(pacakge.src_ip.getHostAddress() + " : " + pacakge.src_port + " is open!");
                        }
                    }
                } else if (p instanceof UDPPacket) {
                    UDPPacket pacakge = (UDPPacket) p;

                } else if (p instanceof ARPPacket) {
                    ARPPacket pacakge = (ARPPacket) p;
                    
                } else if (p instanceof ICMPPacket) {
                    ICMPPacket pacakge = (ICMPPacket) p;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
