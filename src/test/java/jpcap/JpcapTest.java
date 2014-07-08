/*
 * @(#) NetworkCardTest.java 2014-7-4
 * 
 * Copyright 2013 NetEase.com, Inc. All rights reserved.
 */
package jpcap;

import java.io.IOException;
import java.net.Inet4Address;

import jpcap.packet.ARPPacket;
import jpcap.packet.ICMPPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import jpcap.packet.UDPPacket;
import jpcap.util.HexHelper;
import jpcap.util.NetUtil;

import org.junit.Test;

/**
 *
 * @author hzzhaozhou
 * @version 2014-7-4
 */
public class JpcapTest {
    @Test
    public void testNetCard() throws IOException {
        NetworkInterface net = NetUtil.getActiveDevice();

        System.out.println(net.name);
        System.out.println(net.description);
        System.out.println(HexHelper.toMac(net.mac_address));
        System.out.println(net.datalink_name);
        System.out.println(net.datalink_description);
        System.out.println(net.loopback);
        Inet4Address address = NetUtil.getIpV4Address(net);
        System.out.println(address.getHostAddress());

        System.out.println();

    }

    @Test
    public void testRevevice() throws IOException {
        NetworkInterface net = NetUtil.getActiveDevice();
        JpcapCaptor captor = JpcapCaptor.openDevice(net, 2000, false, 3000);
        int count = captor.loopPacket(100, new Handler());
        System.out.println(count);
    }

    private class Handler implements PacketReceiver {

        public void receivePacket(Packet p) {
            try {
                if (p instanceof TCPPacket) {
                    TCPPacket pacakge = (TCPPacket) p;
                    System.out.println("Tcp package from : " + pacakge.src_ip.getHostAddress());
                } else if (p instanceof UDPPacket) {
                    UDPPacket pacakge = (UDPPacket) p;
                    System.out.println("Udp package from : " + pacakge.src_ip.getHostAddress());
                } else if (p instanceof ARPPacket) {
                    ARPPacket pacakge = (ARPPacket) p;
                    System.out.println("Arp package from : " + pacakge.toString());
                } else if (p instanceof ICMPPacket) {
                    ICMPPacket pacakge = (ICMPPacket) p;
                    System.out.println("icmp package from : " + pacakge.toString());
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }
}
