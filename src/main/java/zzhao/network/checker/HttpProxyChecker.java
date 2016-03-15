package zzhao.network.checker;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;

import zzhao.network.PortNotOpenException;

/**
 * http 代理检查
 * @author zzhao
 * @version 2016年2月3日
 */
public class HttpProxyChecker {

    private final static int CONNECT_TIMEOUT = 10000;
    private final static int READ_TIMEOUT = 10000;

    private static final String HTTP_SERVER = "Http-Server";
    private static final String HTTP_PROXY = "Http-Proxy";
    private static final String HTTP_PROXY_WITH_ACCOUNT = "Http-Proxy-With-Account";

    public static void main(String[] args) {
        try {
            System.out.println("is proxy : " + getHttpType("116.52.132.34", 80));
        } catch (PortNotOpenException e) {
            e.printStackTrace();
        }
    }

    public static String getHttpServerType(String host, int port) throws PortNotOpenException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);
        HttpHead head = new HttpHead("http://" + host + ":" + port + "/");
        head.setHeader(HttpHeaders.ACCEPT, "text/html");
        head.setHeader(HttpHeaders.CONNECTION, "close");
        try {
            HttpResponse response = httpClient.execute(head);
            Header header = response.getFirstHeader("Server");
            return header.getValue();
        } catch (ConnectTimeoutException e) {
            throw new PortNotOpenException();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getHttpType(String host, int port) throws PortNotOpenException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(0, false));
        httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECT_TIMEOUT);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, READ_TIMEOUT);
        HttpHost proxy = new HttpHost(host, port);
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        HttpHead head = new HttpHead("http://www.baidu.com");
        head.setHeader(HttpHeaders.ACCEPT, "text/html");
        head.setHeader(HttpHeaders.CONNECTION, "close");
        try {
            HttpResponse response = httpClient.execute(head);
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                Header[] headers = response.getHeaders("Set-Cookie");
                for (Header header : headers) {
                    if (header.getValue().contains("baidu.com")) {
                        return HTTP_PROXY;
                    }
                }
                return HTTP_SERVER;
            } else if (status == HttpStatus.SC_MOVED_TEMPORARILY) {
                Header location = response.getFirstHeader(HttpHeaders.LOCATION);
                if (location != null && "https://www.baidu.com/".equalsIgnoreCase(location.getValue())) {
                    return HTTP_PROXY;
                }
            } else if (status == HttpStatus.SC_CONFLICT) {
                return HTTP_PROXY;
            } else if (status == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
                return HTTP_PROXY_WITH_ACCOUNT;
            }
        } catch (ConnectTimeoutException e) {
            throw new PortNotOpenException();
        } catch (Throwable e) {

        }
        return "";
    }
}
