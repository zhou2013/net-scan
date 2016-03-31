package zzhao.network.http;

import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;

/**
 *
 * @author zzhao
 * @version 2016年3月30日
 */
public class HttpClientConnectionOperator extends DefaultClientConnectionOperator {

    /**
     * @param schemes
     */
    public HttpClientConnectionOperator(SchemeRegistry schemes) {
        super(schemes);
    }

    public HttpClientConnectionOperator(final SchemeRegistry schemes, final DnsResolver dnsResolver) {
        super(schemes, dnsResolver);
    }

    @Override
    public OperatedClientConnection createConnection() {
        return new HttpClientConnection();
    }
}
