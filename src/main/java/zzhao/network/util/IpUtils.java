package zzhao.network.util;

import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author zzhao
 * @version 2015年12月2日
 */
public class IpUtils {

    public static long ipToLong(String ip) {
        long ipLongValue = 0;

        String[] ipBlockStr = ip.split("\\.");

        long[] ipBlocks = new long[4];
        for (int i = 0; i < 4; i++) {
            try {
                ipBlocks[i] = Integer.parseInt(ipBlockStr[i]);
            } catch (Exception e) {
                ipBlocks[i] = 0;
            }
        }

        ipLongValue += ipBlocks[0] << 24;
        ipLongValue += ipBlocks[1] << 16;
        ipLongValue += ipBlocks[2] << 8;
        ipLongValue += ipBlocks[3];

        return ipLongValue;
    }

    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder();
        sb.append((ip >> 24) & 0xff).append(".");
        sb.append((ip >> 16) & 0xff).append(".");
        sb.append((ip >> 8) & 0xff).append(".");
        sb.append(ip & 0xff);
        return sb.toString();
    }

    public static boolean isInternalIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            // 空白认为是内网ip
            return true;
        }

        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }

        long iplong = ipToLong(ip);
        // 172.16.0.0 ~ 172.31.255.255
        if (iplong >= 2886729728L && iplong <= 2887778303L) {
            return true;
        }

        return false;
    }

    // 获取网关ip
    public static String getNetIp(String ip, String net) {
        long ipLong = ipToLong(ip);
        long netLong = ipToLong(net);
        long netIp = (ipLong & netLong) + 1;
        return longToIp(netIp);
    }

    public static String formateIp(String ip) {
        long value = ipToLong(ip);
        return longToIp(value);
    }
}
