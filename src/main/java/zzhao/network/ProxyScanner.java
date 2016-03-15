package zzhao.network;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import zzhao.network.checker.HttpProxyChecker;
import zzhao.network.checker.SocketProxyChecker;
import zzhao.network.util.ThreadUtils;

/**
 * 端口协议的扫描
 * @author zzhao
 * @version 2016年2月18日
 */

public class ProxyScanner {
    
    private static final Logger logger = LoggerFactory.getLogger(ProxyScanner.class);

    // 用来执行端口检查的线程池
    private ThreadPoolExecutor executors = (ThreadPoolExecutor) Executors.newFixedThreadPool(40, ThreadUtils.demoFactory);
    
    private Map<String, IpScanObject> resultMap = Maps.newConcurrentMap();
    
    private ScannerHandler handler;

    public ProxyScanner(ScannerHandler handler) {
        this.handler = handler;
    }

    public synchronized void initResultMap(List<String> ips) {
        resultMap.clear();
        for (String ip : ips) {
            IpScanObject obj = new IpScanObject();
            obj.setIp(ip);
            resultMap.put(ip, obj);
        }
    }

    /**
     * 出现异常状况时放弃所有数据
     */
    public synchronized void deleteDropData() {
        resultMap.clear();
        executors.shutdown();
        executors = (ThreadPoolExecutor) Executors.newFixedThreadPool(40, ThreadUtils.demoFactory);
    }

    /**
     * 提交扫描结果
     */
    public synchronized void postResult() {
        // 首先等待所有任务执行完
        while (executors.getActiveCount() != 0) {
            ThreadUtils.sleepQuitely(500);
        }
        handler.processResult(resultMap.values());
    }

    public void doScan(String ip, int port) {
        executors.submit(new ProxyScanTask(ip, port));
    }

    private void doProxyScan(String ip, int port) {
        String type = "";
        try {
            type = HttpProxyChecker.getHttpType(ip, port);
            if (StringUtils.isBlank(type)) {
                type = SocketProxyChecker.checkSocketProxy(ip, port);
            }
        } catch (PortNotOpenException e) {
            // 端口无法连接
        } catch (Throwable e) {

        }

        IpScanObject obj = resultMap.get(ip);
        if (obj != null) {
            obj.getPorts().put(port, type == null ? "" : type);
        }
    }

    private class ProxyScanTask implements Runnable {
        private String ip;
        private int port;

        ProxyScanTask(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run() {
            doProxyScan(ip, port);
        }
    }
}
