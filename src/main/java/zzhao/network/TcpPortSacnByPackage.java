/*
 * @(#) TcpPortSacnByPackage.java 2014-7-7
 * 
 * Copyright 2013 NetEase.com, Inc. All rights reserved.
 */
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
import jpcap.packet.EthernetPacket;
import jpcap.packet.IPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.util.HexHelper;
import jpcap.util.NetUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Sets;

/**
 * 模拟tcp建立链接过程的形式来扫描端口
 * @author hzzhaozhou
 * @version 2014-7-7
 */
public class TcpPortSacnByPackage {

    public static void main(String[] args) {
        new TcpPortSacnByPackage().doScan();
    }

    private static final Logger logger = Logger.getLogger(TcpPortSacnByPackage.class);

    private Random random = new Random(System.currentTimeMillis());

    private HashSet<String> ports = Sets.newHashSet();
    
    private JpcapSender sender = null;

    private NetworkInterface net;

    private Inet4Address address;

    private String localAddress;

    private JpcapCaptor jpcap;

    private InetAddress target;

    private String targetAddress;
    
    private byte[] targetMac;

    private void init() throws UnknownHostException {
        net = NetUtil.getActiveDevice();
        address = NetUtil.getIpV4Address(net);
        localAddress = address.getHostAddress();
        target = InetAddress.getByName("10.240.137.162");
        targetAddress = target.getHostAddress();
        targetMac = HexHelper.macToByte("44:37:E6:99:DC:92");
    }

    public void doScan() {
        try {
            init();

            logger.info("local address : " + localAddress);
            logger.info("target address : " + targetAddress);

            jpcap = JpcapCaptor.openDevice(net, 2000, false, -1);

            Thread recevieThread = new Thread(new AckCheck());
            recevieThread.setDaemon(true);
            recevieThread.start();

            int startPort = 1;
            int endPort = 60000;

            sender = JpcapSender.openDevice(net);

            for (int port = startPort; port < endPort; port++) {
                    TCPPacket tcpPacket = generateTcpPackage();
                    prepareSYNPackage(tcpPacket, target, port);
                    sender.sendPacket(tcpPacket);
                    tcpPacket = null;
            }

            Thread.sleep(30000000);
            System.out.println(ports + " is open!");
            jpcap.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class AckCheck implements Runnable {
 
        public void run() {
            try {
                JpcapSender insender = JpcapSender.openDevice(net); 
                jpcap.loopPacket(-1, new Handler(insender));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class Handler implements PacketReceiver {

        private   JpcapSender  insender = null;
        
        public Handler(JpcapSender sender){
            insender = sender;
        }
        
        public void receivePacket(Packet p) {
            try {
                if (p instanceof TCPPacket) {
                    TCPPacket pacakge = (TCPPacket) p;
                    if (StringUtils.equals(localAddress, pacakge.dst_ip.getHostAddress()) 
                                    && StringUtils.equals(targetAddress, pacakge.src_ip.getHostAddress()))  {
                        if (pacakge.ack == true && pacakge.syn == true) {
                            if(checkTcpPackage(pacakge)){
                                System.out.println(pacakge.src_ip.getHostAddress() + " : " + pacakge.src_port + " is open!");
                                ports.add(String.valueOf(pacakge.src_port));
                                TCPPacket tmp = generateTcpPackage();
                                tmp.dst_port = pacakge.src_port;
                                tmp.dst_ip = pacakge.src_ip;
                                tmp.src_port = pacakge.dst_port;
                                tmp.src_ip = pacakge.dst_ip;
                                tmp.rst = true;
                                tmp.sequence = pacakge.ack_num;
                                tmp.window = 0;
                                insender.sendPacket(tmp);
                            }
                        }
                    }
                 }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private TCPPacket generateTcpPackage() {
        TCPPacket tcpPacket = new TCPPacket(0, 0, 0, 0, false, false, false, false, false, false, false, false, 8192, 0);
        tcpPacket.setIPv4Parameter(0, false, false, false, 0, false, false, false, 0, 5000 + random.nextInt(300), 128,
                                   IPPacket.IPPROTO_TCP, address, null);
        tcpPacket.data = "".getBytes();

        EthernetPacket ether = new EthernetPacket();
        ether.frametype = EthernetPacket.ETHERTYPE_IP;
        ether.src_mac = net.mac_address;
        ether.dst_mac = targetMac;
        tcpPacket.datalink = ether;
        return tcpPacket;
    }

    private void prepareSYNPackage(TCPPacket tp, InetAddress target, int destPort) {
        tp.dst_port = destPort;
        tp.dst_ip = target;
        String tmp = tp.dst_ip.getHostAddress() + ":" + tp.dst_port;
        int hashCode = tmp.hashCode() & 0x7FFFFFFF;
        int srcPort = hashCode & 0x4000 + 10000; // 取右边的14位作为发送端口
        long sequence = hashCode >> 14 ;
        sequence = 3234567890L +sequence;
        tp.src_port = srcPort;
        tp.sequence = sequence;
        tp.syn = true;
    }

    private boolean checkTcpPackage(TCPPacket tp) {
        String tmp = tp.src_ip.getHostAddress() + ":" + tp.src_port;
        long hashCode = tmp.hashCode() & 0x7FFFFFFF;
        int receivePort = tp.dst_port;
        long sendPort = hashCode & 0x4000 + 10000;
        long ack = tp.ack_num;
        long sequence =  hashCode >> 14 ;
        sequence = 3234567890L +sequence;
        if (sendPort == receivePort && ack == sequence + 1) {
            return true;
        }
        return false;
    }
}
