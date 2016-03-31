package zzhao.network.http;

import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.io.HttpMessageParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.params.HttpParams;

/**
 *
 * @author zzhao
 * @version 2016年3月30日
 */
public class HttpClientConnection extends DefaultClientConnection {

    @Override
    protected HttpMessageParser<HttpResponse> createResponseParser(SessionInputBuffer buffer,
                    HttpResponseFactory responseFactory, HttpParams params) {
        // TODO Auto-generated method stub
        return new HttpResponseParser(buffer, null, responseFactory, params);
    }
}
