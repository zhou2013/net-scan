package zzhao.network.jpcap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.packet.TCPPacket;
import zzhao.network.util.ByteUtils;
import zzhao.network.util.IpUtils;
import zzhao.network.util.NetUtils;
import zzhao.network.util.ThreadUtils;

/**
 * 包扫描器, 用来接收发送对应的检查包
 * @author zzhao
 * @version 2016年2月15日
 */
public class PacketScanner implements PacketReceiver {

    private static final Logger logger = LoggerFactory.getLogger(PacketScanner.class);

    private JpcapSender sender = null;

    private JpcapCaptor jpcap;

    private NetworkInterface networkInterface;

    private InetAddress localAddress = null;

    private String localIp = null;

    private byte[] sourceMac = null;

    private String netIp = null; // 网关ip

    private volatile byte[] netMac = null; // 这里应该是网关的mac

    private Thread reveiveThread;

    private PacketScanResultHandler handler;

    public PacketScanner(PacketScanResultHandler handler) {
        this.handler = handler;
    }

    public synchronized void initScanner() throws IOException {
        if (networkInterface != null) {
            return;
        }

        networkInterface = NetUtils.getActiveDevice(); // 获取可用的网卡
        // networkInterface = NetUtils.getPubDEvice(); // 获取一个有外网ip的网卡

        if (networkInterface == null) {
            throw new RuntimeException("can't get active pub network interface!");
        }

        NetworkInterfaceAddress address = NetUtils.getIpV4Address(networkInterface);
        String netAddress = address.subnet.getHostAddress();
        localAddress = address.address;
        localIp = address.address.getHostAddress();
        netIp = IpUtils.getNetIp(localIp, netAddress);
        sourceMac = networkInterface.mac_address;

        jpcap = JpcapCaptor.openDevice(networkInterface, 2000, false, -1);
        sender = JpcapSender.openDevice(networkInterface);

        final PacketReceiver recevier = this;
        reveiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("packet loop started!");
                    jpcap.loopPacket(-1, recevier);
                } catch (Throwable e) {
                    // 这里抛出来的异常调用堆栈有问题，不确定是否是调用c函数的关系
                    logger.error("loop package failed!", e);
                    if (handler != null) {
                        handler.onError(e.getMessage());
                    }
                }
            }
        });
        reveiveThread.setDaemon(true);
        reveiveThread.start();
        tryGetNetMac();
        logger.info("init success, localIp[{}], localmac[{}], netip[{}], netmac[{}]", localIp, ByteUtils.toMac(sourceMac),
                    netIp, ByteUtils.toMac(netMac));
    }

    public synchronized void stop() {
        logger.info("start stop scanner");
        this.handler = null;
        if (jpcap != null) {
            jpcap.breakLoop();
            ThreadUtils.sleepQuitely(100);
            jpcap.close();
            jpcap = null;
        }
        if (sender != null) {
            sender.close();
            sender = null;
        }
    }

    private void tryGetNetMac() {
        sendArpPackage(netIp);
        int count = 0;
        try {
            while (count < 5) {
                this.wait(1000);
                if (netMac != null) {
                    break;
                }
                count++;
                sendArpPackage(netIp);
            }
        } catch (Throwable e) {

        }
        if (netMac == null) {
            throw new RuntimeException("cant' get net mac!");
        }
    }

    private void sendArpPackage(String targetIp) {
        byte[] testMac = new byte[] {0, 0, 0, 0, 0, 0};
        ARPPacket packet = new ARPPacket();
        packet.setValue(ARPPacket.HARDTYPE_ETHER, ARPPacket.PROTOTYPE_IP, (short) 6, (short) 4, ARPPacket.ARP_REQUEST,
                        sourceMac, ByteUtils.ipToByte(localIp), testMac, ByteUtils.ipToByte(targetIp));
        EthernetPacket ether = new EthernetPacket();
        ether.frametype = EthernetPacket.ETHERTYPE_ARP;
        ether.src_mac = sourceMac;
        ether.dst_mac = new byte[] {-1, -1, -1, -1, -1, -1};
        packet.datalink = ether;
        sender.sendPacket(packet);
    }

    public void sendSynPacket(String ip, int port) throws UnknownHostException {
        InetAddress target = InetAddress.getByName(ip);
        TCPPacket packet = generateTcpPacket();
        PacketGenerator.prepareSynPacket(packet, target, port);
        sender.sendPacket(packet);
    }

    private void sendRstPacket(TCPPacket packet) {
        TCPPacket tmp = generateTcpPacket();
        tmp.dst_port = packet.src_port;
        tmp.dst_ip = packet.src_ip;
        tmp.src_port = packet.dst_port;
        tmp.src_ip = packet.dst_ip;
        tmp.rst = true;
        tmp.sequence = packet.ack_num;
        tmp.window = 0;
        sender.sendPacket(tmp);
    }

    private TCPPacket generateTcpPacket() {
        TCPPacket tcpPacket = PacketGenerator.generateTcpPacket(sourceMac, netMac);
        tcpPacket.src_ip = localAddress;
        return tcpPacket;
    }

    @Override
    public void receivePacket(Packet p) {
        if (p instanceof ARPPacket && netMac == null) {
            ARPPacket packet = (ARPPacket) p;
            if (packet.hlen == 6 && packet.plen == 4 && packet.operation == ARPPacket.ARP_REPLY
                            && ByteUtils.bytesEqual(sourceMac, packet.target_hardaddr)) {
                netMac = packet.sender_hardaddr;
                this.notify();
            }
        } else if (p instanceof TCPPacket) {
            TCPPacket packet = (TCPPacket) p;
            if (StringUtils.equals(localIp, packet.dst_ip.getHostAddress())) {
                if (packet.ack == true && packet.syn == true) {
                    if (PacketGenerator.checkTcpPacket(packet)) {
                        if(this.handler != null){
                            handler.portDetected(packet.src_ip.getHostAddress(), packet.src_port);
                        }
                        sendRstPacket(packet);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            final PacketScanner scanner = new PacketScanner(new PacketScanResultHandler() {
                @Override
                public void portDetected(String ip, int port) {
                    System.out.println(ip + "  " + port + "   is Open");
                }

                @Override
                public void onError(String msg) {
                    System.out.println("On error: " + msg);
                }
            });
            scanner.initScanner();
            String ip = "115.239.211.112";
            for (int port = 1; port < 1000; port++) {
                scanner.sendSynPacket(ip, port);
            }
            Thread.sleep(15000);
            scanner.stop();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
