package zzhao.network.http;

import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.BasicClientConnectionManager;

/**
 *
 * @author zzhao
 * @version 2016年3月30日
 */
public class HttpClientConnectionManager extends BasicClientConnectionManager {

    @Override
    protected ClientConnectionOperator createConnectionOperator(SchemeRegistry schreg) {
        return new HttpClientConnectionOperator(schreg);
    }
}
