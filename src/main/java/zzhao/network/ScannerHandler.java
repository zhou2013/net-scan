package zzhao.network;

import java.util.Collection;
import java.util.Set;

/**
 *
 * @author zzhao
 * @version 2016年3月11日
 */
public interface ScannerHandler {

    public Set<String> getScanIps();

    public Set<Integer> getScanPorts();

    public boolean processResult(Collection<IpScanObject> result);
}
