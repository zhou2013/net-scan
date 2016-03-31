package zzhao.network.http;

import org.apache.http.HttpResponseFactory;
import org.apache.http.impl.conn.DefaultHttpResponseParser;
import org.apache.http.io.SessionInputBuffer;
import org.apache.http.message.LineParser;
import org.apache.http.params.HttpParams;
import org.apache.http.util.CharArrayBuffer;

/**
 *
 * @author zzhao
 * @version 2016年3月30日
 */
public class HttpResponseParser extends DefaultHttpResponseParser {

    /**
     * @param buffer
     * @param parser
     * @param responseFactory
     * @param params
     */
    public HttpResponseParser(SessionInputBuffer buffer, LineParser parser, HttpResponseFactory responseFactory,
                    HttpParams params) {
        super(buffer, parser, responseFactory, params);
    }

    @Override
    protected boolean reject(CharArrayBuffer line, int count) {
        if (count >= 3) {
            return true;
        }
        return false;
    }

}
