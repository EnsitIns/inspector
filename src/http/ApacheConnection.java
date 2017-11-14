package http;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

/**
 * Created by 1 on 24.03.2017.
 */
public class ApacheConnection {
    private static final Logger logger = Logger
            .getLogger(ApacheConnection.class);

    public String executePost(String URLAddress,
                              Map<String, String> headerParams,
                              List<NameValuePair> postParams) throws MalformedURLException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(URLAddress);

        if (headerParams != null) {
            for (String header : headerParams.keySet()) {
                httpPost.addHeader(header, headerParams.get(header));
            }
        }

        // Set UTF-8 character encoding to ensure proper encoding structure in your
        // posts
        if (postParams != null)
            try {
                httpPost
                        .setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        // Set up the response handler
        ResponseHandler<String> handler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {

                int status = response.getStatusLine().getStatusCode();

                logger.info("Status: " + status);
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            }
        };
        String responseBody = null;
        try {
            responseBody = client.execute(httpPost, handler);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public String executeGet(String URLAddress,
                             Map<String, String> headerParams) throws MalformedURLException {

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet(URLAddress);

        if (headerParams != null) {
            for (String header : headerParams.keySet()) {
                httpGet.addHeader(header, headerParams.get(header));
            }
        }

        // Set up the response handler
        ResponseHandler<String> handler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {

                int status = response.getStatusLine().getStatusCode();

                logger.info("Status: " + status);
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            }
        };
        String responseBody = null;
        try {
            responseBody = client.execute(httpGet, handler);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseBody;
    }

    public String executeSSLPost(String URLAddress,
                                 Map<String, String> headerParams,
                                 List<NameValuePair> postParams) throws MalformedURLException,
            IOException, NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException {

        HttpClientBuilder builder = HttpClientBuilder.create();

        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(null, new TrustStrategy() {
                    public boolean isTrusted(X509Certificate[] arg0,
                                             String arg1) throws CertificateException {
                        return true;
                    }
                }).build();

        @SuppressWarnings("deprecation")
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create()
                .register("http",
                        PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory).build();

        PoolingHttpClientConnectionManager connectionMgr = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry);
        builder.setConnectionManager(connectionMgr);

        builder.setSslcontext(sslContext);

        HttpClient client = builder.build();
        HttpPost httpPost = new HttpPost(URLAddress);

        if (headerParams != null) {
            for (String header : headerParams.keySet()) {
                httpPost.addHeader(header, headerParams.get(header));
            }
        }

        // Set UTF-8 character encoding to ensure proper encoding structure in your
        // posts
        if (postParams != null)
            httpPost
                    .setEntity(new UrlEncodedFormEntity(postParams, "UTF-8"));

        // Set up the response handler
        ResponseHandler<String> handler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {

                int status = response.getStatusLine().getStatusCode();

                logger.info("Status: " + status);
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException(
                            "Unexpected response status: " + status);
                }
            }
        };
        String responseBody = client.execute(httpPost, handler);
        return responseBody;
    }

    public String executeSSLGet(String URLAddress,
                                Map<String, String> headerParams) throws MalformedURLException,
            IOException, KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException {
        HttpClient client = HttpClients.createDefault();
        HttpGet httpget = new HttpGet(URLAddress);

        if (headerParams != null) {
            for (String header : headerParams.keySet()) {
                httpget.addHeader(header, headerParams.get(header));
            }
        }

        logger.info("Executing request " + httpget.getRequestLine());

        // Set up the response handler
        ResponseHandler<String> handler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {

                int status = response.getStatusLine().getStatusCode();

                logger.info("Status: " + status); // consider log4j instead of sysout.
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException(
                            "Unexpected response status: " + status);
                }
            }
        };
        String responseBody = client.execute(httpget, handler);
        return responseBody;
    }

    public String executeSSLGetAllTrusting(String URLAddress)
            throws MalformedURLException, IOException,
            KeyManagementException, NoSuchAlgorithmException,
            KeyStoreException {
        TrustManager[] allTrustingCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        logger.info("Inside TrustManager getAcceptedIssuers...");
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs,
                                                   String authType) throws CertificateException {
                        logger.info("Inside TrustManager checkClientTrusted...");
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs,
                                                   String authType) {
                        logger.info("Inside TrustManager checkServerTrusted...");
                        logger.info("certs......: " + certs);
                        logger.info("authType...: " + authType);
                    }
                } };

        SSLContextBuilder sslBuilder = new SSLContextBuilder();
        sslBuilder.loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] chain,
                                     String authType) throws CertificateException {
                return true;
            }
        });

        @SuppressWarnings("deprecation")
        HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslBuilder.build());
        CloseableHttpClient client = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory).build();
        HttpGet httpget = new HttpGet(URLAddress);

        logger.info("Executing request " + httpget.getRequestLine());

        // Set up the response handler
        ResponseHandler<String> handler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(final HttpResponse response)
                    throws ClientProtocolException, IOException {

                int status = response.getStatusLine().getStatusCode();

                logger.info("Status: " + status);
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException(
                            "Unexpected response status: " + status);
                }
            }
        };
        String responseBody = client.execute(httpget, handler);
        return responseBody;
    }
}
