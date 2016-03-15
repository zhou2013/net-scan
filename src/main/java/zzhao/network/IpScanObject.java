package zzhao.network;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * ip扫描对象
 * @author zzhao
 * @version 2016年2月18日
 */
public class IpScanObject {

    private String ip;

    private Map<Integer, String> ports = Maps.newConcurrentMap();

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Map<Integer, String> getPorts() {
        return ports;
    }

    public void setPorts(Map<Integer, String> ports) {
        this.ports = ports;
    }
}
