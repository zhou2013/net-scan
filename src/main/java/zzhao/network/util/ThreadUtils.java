package zzhao.network.util;

import java.util.concurrent.ThreadFactory;

/**
 *
 * @author zzhao
 * @version 2016年1月18日
 */
public class ThreadUtils {

    public final static ThreadFactory demoFactory = new ThreadFactory() {

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        }
    };

    public static void sleepQuitely(long millis){
        try{
            Thread.sleep(millis);
        } catch (Throwable e) {
            // do nothing;
        }
    }

}
