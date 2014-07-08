package jpcap.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class NetUtil {
    private static final Logger logger = Logger.getLogger(NetUtil.class);
    
    public static NetworkInterface getActiveDevice() throws UnknownHostException {
        try {
            InetAddress host = InetAddress.getLocalHost();
            String loadAddress = host.getHostAddress();
            for (NetworkInterface net : JpcapCaptor.getDeviceList()) {
                if (net.addresses != null && net.addresses.length > 0) {
                    for (NetworkInterfaceAddress address : net.addresses) {
                        if (StringUtils.equalsIgnoreCase(loadAddress, address.address.getHostAddress())) {
                            return net;
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.info("failed to get Active Device!",e);
        }
        return null;
    }

    public static Inet4Address getIpV4Address(NetworkInterface net) {
        if (net.addresses != null && net.addresses.length > 0) {
            for (NetworkInterfaceAddress address : net.addresses) {
                if (address.address instanceof Inet4Address && address.subnet != null && address.subnet instanceof Inet4Address) {
                    return (Inet4Address) address.address;
                }
            }
        }
        return null;
    }
}
