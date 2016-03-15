package zzhao.network.jpcap;

/**
 * 扫描结果回调函数
 * @author zzhao
 * @version 2016年2月15日
 */
public interface PacketScanResultHandler {

    public void portDetected(String ip, int port);

    public void onError(String msg);
}
