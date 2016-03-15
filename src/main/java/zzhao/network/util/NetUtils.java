package zzhao.network.util;

import java.net.Inet4Address;
import java.net.InetAddress;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jpcap.JpcapCaptor;
import jpcap.NetworkInterface;
import jpcap.NetworkInterfaceAddress;

/**
 *
 * @author zzhao
 * @version 2016年2月15日
 */
public class NetUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetUtils.class);

    public static NetworkInterface getPubDEvice() {
        try {
            for (NetworkInterface net : JpcapCaptor.getDeviceList()) {
                NetworkInterfaceAddress address = getIpV4Address(net);
                if (address != null && !IpUtils.isInternalIp(address.address.getHostAddress())) {
                    return net;
                }
            }
            return null;
        } catch (Exception e) {
            logger.info("failed to get Pub Device!", e);
        }
        return null;
    }

    public static NetworkInterface getActiveDevice() {
        try {
            InetAddress host = InetAddress.getLocalHost();
            String loadAddress = host.getHostAddress();
            for (NetworkInterface net : JpcapCaptor.getDeviceList()) {
                NetworkInterfaceAddress address = getIpV4Address(net);
                if (address != null && StringUtils.equalsIgnoreCase(loadAddress, address.address.getHostAddress())) {
                    return net;
                }
            }
        } catch (Exception e) {
            logger.info("failed to get Active Device!", e);
        }
        return null;
    }

    public static NetworkInterfaceAddress getIpV4Address(NetworkInterface net) {
        if (net.addresses != null && net.addresses.length > 0) {
            for (NetworkInterfaceAddress address : net.addresses) {
                if (address.address instanceof Inet4Address && address.subnet != null
                                && address.subnet instanceof Inet4Address) {
                    return address;
                }
            }
        }
        return null;
    }
}
