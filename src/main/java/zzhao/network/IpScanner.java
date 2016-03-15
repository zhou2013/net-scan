package zzhao.network;

import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import zzhao.network.jpcap.PacketScanResultHandler;
import zzhao.network.jpcap.PacketScanner;
import zzhao.network.util.DateTimeUtils;
import zzhao.network.util.ThreadUtils;

/**
 * ip 扫描器
 * @author zzhao
 * @version 2016年2月18日
 */
public class IpScanner implements PacketScanResultHandler {

    private static final Logger logger = LoggerFactory.getLogger(IpScanner.class);

    private static final int RETRY_LIMIT = 3;

    private volatile PacketScanner packetScanner;

    private Map<String, Integer> tmpScanMap = Maps.newConcurrentMap();

    private ProxyScanner proxyScanner;

    private AtomicInteger openPort = new AtomicInteger(0);

    private ScannerHandler handler;

    public IpScanner(ScannerHandler handler) {
        if (handler == null) {
            throw new RuntimeException("handler can't be null");
        }
        this.handler = handler;
        this.proxyScanner = new ProxyScanner(handler);
    }

    public void doScanner() {
        while (true) {
            boolean scanError = false;
            try {
                if (packetScanner == null) {
                    createPacketScanner();
                }
                long start = System.currentTimeMillis();
                int ipCount = initScanTarget();
                if(ipCount == 0){
                    logger.info("no target ips");
                    ThreadUtils.sleepQuitely(DateTimeUtils.MS_PER_MINUTE);
                    continue;
                }
                openPort.set(0);
                int try_count = 0;
                while (try_count < RETRY_LIMIT) {
                    try_count++;
                    logger.info("start {} packets, size = {}", try_count, tmpScanMap.size());
                    sendSynPackets();
                    ThreadUtils.sleepQuitely(3000);
                    if (tmpScanMap.size() == 0) {
                        break; // 所有端口已经扫描完成
                    }
                }
                logger.info("find open ports {}", openPort.get());
                proxyScanner.postResult();
                long timeUsed = (System.currentTimeMillis() - start) / 1000;
                logger.info("scan success, ip.size = {}, time used = {}", ipCount, timeUsed);
            } catch (Throwable e) {
                logger.error("scan error!", e);
                scanError = true;
            }
            if (scanError) {
                // 清理缓存数据
                tmpScanMap.clear();
                proxyScanner.deleteDropData();
            }
        }
    }

    // 循环
    private synchronized void createPacketScanner() {
        do {
            PacketScanner tmp = null;
            try {
                tmp = new PacketScanner(this);
                tmp.initScanner();
                packetScanner = tmp;
            } catch (Throwable e) {
                logger.error("failed to create package scanner!", e);
                ThreadUtils.sleepQuitely(2 * DateTimeUtils.MS_PER_MINUTE);
            }
        } while (packetScanner == null);
        logger.info("create packet scanner success!");
    }

    private int initScanTarget() {
        tmpScanMap.clear();
        Set<String> ips = handler.getScanIps();
        Set<Integer> ports = handler.getScanPorts();

        if (ips.size() == 0 || ports.size() == 0) {
            return 0;
        }
        for (String ip : ips) {
            for (Integer port : ports) {
                tmpScanMap.put(ip + ":" + port, 0);
            }
        }
        proxyScanner.initResultMap(Lists.newArrayList(ips));
        return ips.size();
    }

    private boolean sendSynPackets() throws UnknownHostException {
        Iterator<Entry<String, Integer>> itor = tmpScanMap.entrySet().iterator();
        while (itor.hasNext()) {
            Entry<String, Integer> entry = itor.next();
            String[] tmps = entry.getKey().split(":");
            String ip = tmps[0];
            int port = Integer.valueOf(tmps[1]);
            packetScanner.sendSynPacket(ip, port);
        }
        return true;
    }

    @Override
    public void portDetected(String ip, int port) {
        Integer obj = tmpScanMap.remove(ip + ":" + port);
        if (obj != null) {
            proxyScanner.doScan(ip, port);
            openPort.incrementAndGet();
        }
    }

    @Override
    public void onError(String msg) {
        packetScanner = null;
    }
}
