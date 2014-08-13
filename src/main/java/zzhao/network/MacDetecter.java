package zzhao.network;

import java.net.Inet4Address;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jpcap.JpcapCaptor;
import jpcap.JpcapSender;
import jpcap.NetworkInterface;
import jpcap.PacketReceiver;
import jpcap.packet.ARPPacket;
import jpcap.packet.EthernetPacket;
import jpcap.packet.Packet;
import jpcap.util.HexHelper;
import jpcap.util.NetUtil;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MacDetecter {
    private static final Logger logger = Logger.getLogger(TcpPortSacnByPackage.class);
    
    public static void main(String[] args) {
        List<String> ips = Lists.newArrayList("220.181.112.244");
        MacDetecter  detecter = new MacDetecter();
        System.out.println(detecter.detectMacs(ips));
        detecter.close();
    }
    
    private boolean inited = false;
    
    NetworkInterface netcard;
    Inet4Address localAddress;
    String localAddressStr;
    JpcapCaptor jpcap;
    JpcapSender sender;
    byte[] localMac;
    byte[] testMac = new byte[]{0,0,0,0,0,0};
  
    public MacDetecter(){
       
    }
    
    private synchronized void init() throws Exception{
        if(!inited){
            netcard = NetUtil.getActiveDevice(); 
            localAddress = NetUtil.getIpV4Address(netcard);
            localMac = netcard.mac_address;
            localAddressStr = localAddress.getHostAddress();
            jpcap = JpcapCaptor.openDevice(netcard, 2000, false, -1);
            sender = JpcapSender.openDevice(netcard);
            inited = true;
        }
    }
    
    public synchronized void close(){
        if(jpcap != null){
            jpcap.close();
            jpcap = null;
        }
        
        if(sender != null){
            sender.close();
            sender = null;
        }
    }
    
    public synchronized Map<String,String> detectMacs(List<String> ips){
        Map<String, String> result = Maps.newConcurrentMap();
        try{
            init();
            final ArpHandler handler = new ArpHandler(ips, result);
            
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try{
                        jpcap.loopPacket(-1,handler);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });
            thread.setDaemon(false);
            thread.start();
            
            for(String ip : ips){
                sendArpPackage(ip);
            }
            
            int ipSize = ips.size();
            int count = 0;
            while( count < 100000){
                int currSize = result.size();
                if(currSize == ipSize){
                    break;
                }
                Thread.sleep(1000);
                count++;
            }
            
            jpcap.breakLoop();
            thread.join();
            
        }catch(Exception e){
            logger.error("Captuer Error!", e);
        }
    
        return result;
    }
    
    private void sendArpPackage(String targetIp){
        ARPPacket packet = new ARPPacket();
        packet.setValue( ARPPacket.HARDTYPE_ETHER, ARPPacket.PROTOTYPE_IP, (short)6,(short) 4, ARPPacket.ARP_REQUEST,
                         localMac, ByteUtils.ipToMac(localAddressStr), testMac, ByteUtils.ipToMac(targetIp));
        EthernetPacket ether = new EthernetPacket();
        ether.frametype = EthernetPacket.ETHERTYPE_ARP;
        ether.src_mac = localMac;;
        ether.dst_mac = new byte[]{-1,-1,-1,-1,-1,-1};
        packet.datalink = ether;
        sender.sendPacket(packet);
    }
    
    
    public class ArpHandler implements PacketReceiver {

        private Map<String,String> macMap;
        
        private Set<String> targets;
        
        public ArpHandler(List<String> ipLists, Map<String,String> macMap){
            targets = Sets.newHashSet(ipLists);
            this.macMap = macMap;
        }
        
        public void receivePacket(Packet p) {
            if(p instanceof ARPPacket){
                ARPPacket packet = (ARPPacket) p;
               if( packet.hlen == 6 && packet.plen == 4 
                   && packet.operation == ARPPacket.ARP_REPLY  
                   && ByteUtils.bytesEqual(localMac, packet.target_hardaddr) ){
                   String targetmac = HexHelper.toMac(packet.sender_hardaddr);
                   String targetIp = ByteUtils.byteToIp(packet.sender_protoaddr);
                    if(targets.contains(targetIp)){
                        macMap.put(targetIp, targetmac);
                    }
               }
            }
        }
    }
}
