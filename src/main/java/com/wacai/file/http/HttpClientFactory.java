package com.wacai.file.http;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by fulushou on 2016/12/22.
 * 如果单独使用httpClientFactory来创建httpClient，请记得在不使用httpClient的时候，调用httpClient.close 关闭资源。
 */
public class HttpClientFactory {
    private int maxTotal = 200;
    private int defaultMaxPerRoute = 20;
    private int socketTimeout = 10000;
    private int connectTimeout = 10000;
    private int connectionRequestTimeout = 10000;
    private int maxPerRoute = 100;
    private ConnectionKeepAliveStrategy connectionKeepAliveStrategy = new DefaultConnectionKeepAliveStrategy();
    private HttpRequestRetryHandler httpRequestRetryHandler = new DefaultHttpRequestRetryHandler(30,false);
    public CloseableHttpClient getHttpClient(){
        return getHttpClient(null,-1);
    }

    public CloseableHttpClient getHttpClient(String hostName){
        return getHttpClient(hostName,-1);
    }

    public CloseableHttpClient getHttpClient(String hostName, int port){

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //设置最大 http 长连接数
        cm.setMaxTotal(maxTotal);
        //设置每个路由的最大基础连接数
        cm.setDefaultMaxPerRoute(defaultMaxPerRoute);

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)    // 设置数据等待时间
                .setConnectTimeout(connectTimeout)   // 设置连接超时时间
                .setConnectionRequestTimeout(connectionRequestTimeout) //设置获取连接池中长连接的等待时间
                .build();

        if(hostName != null) {
            //设置目标主机的最大连接数,连接主机系统路由
            HttpHost requestHost = new HttpHost(hostName, port);
            cm.setMaxPerRoute(new HttpRoute(requestHost), maxPerRoute);
        }

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(connectionKeepAliveStrategy)
                .setConnectionManager(cm)
//                .setServiceUnavailableRetryStrategy(false)
                .setRetryHandler(httpRequestRetryHandler)
                .build();

        return httpClient;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    /**
     * 设置最大 http 长连接数
     * @param maxTotal
     */
    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getDefaultMaxPerRoute() {
        return defaultMaxPerRoute;
    }

    /**
     * 设置每个路由的最大基础连接数
     * @param defaultMaxPerRoute
     */
    public void setDefaultMaxPerRoute(int defaultMaxPerRoute) {
        this.defaultMaxPerRoute = defaultMaxPerRoute;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    /**
     * 设置目标主机的最大连接数
     * @param maxPerRoute
     */
    public void setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public ConnectionKeepAliveStrategy getConnectionKeepAliveStrategy() {
        return connectionKeepAliveStrategy;
    }

    /**
     * 设置保持keepAlive连接策略，默认等于服务端的keep-alive time
     * @param connectionKeepAliveStrategy
     */
    public void setConnectionKeepAliveStrategy(ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        this.connectionKeepAliveStrategy = connectionKeepAliveStrategy;
    }
}
