package com.ulearning.video.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.ulearning.video.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ?????????:HttpClient?????????
 *
 * @author hlz
 * @ClassName: HttpClientUtil
 * @date 2017???12???25??? ??????2:16:47
 * <p>
 * Copyright (c) 2006-2017.Beijing WenHua Online Sci-Tech Development Co. Ltd
 * All rights reserved.
 */
@Component
public class HttpUtil {
    public static final String CONTENTTYPE = "application/json";

    /**
     * ??????????????????
     */
    public static final Pattern URL_PATTERN = Pattern.compile("^(http|ftp|https)://\\S+");

    /**
     * ????????????????????????????????????
     * <p>??????????????????????????????????????????????????????ajax?????????????????????datatype??????json??????eval()??????</p>
     *
     * @param response
     * @param arg      ?????????json???XML?????????
     * @author WangJialu
     * @version Jul 22, 2013 10:41:53 PM
     */
    public static void writeToClient(HttpServletResponse response, String arg) {
        writeToClient(response, "text/html;charset=utf-8", "UTF-8", arg);
    }

    /**
     * ?????????????????????
     *
     * @param contentType ??????text/html;charset=utf-8
     * @param encoding    ??????UTF-8
     */
    public static void writeToClient(HttpServletResponse response, String contentType, String encoding, String resStr) {
        if (response != null) {
            if (StringUtils.isBlank(contentType)) {
                response.setContentType("text/html;charset=utf-8");
            }
            if (StringUtils.isBlank(encoding)) {
                response.setCharacterEncoding("UTF-8");
            }
            response.setContentType(contentType);
            response.setCharacterEncoding(encoding);
            PrintWriter pw = null;
            try {
                pw = response.getWriter();
                pw.print(resStr);
                pw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (pw != null) {
                    pw.close();
                }
            }
        }
    }

    /**
     * ????????????????????????????????????
     * <p>??????????????????????????????????????????????????????????????????????????????????????????</p>
     *
     * @param response
     * @param number   ????????????????????????
     * @author WangJialu
     * @version Jul 22, 2013 10:41:53 PM
     */
    public static void writeToClient(HttpServletResponse response, Number number) {
        response.setContentType("text/html;");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            out.print(number);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                out.close();
            }
        }

    }

    /**
     * ???????????????get???????????????????????????200????????????body???????????????200????????????null
     *
     * @param url
     * @return
     * @throws Exception
     * @Title: doGet
     */
    public static String doGet(String url) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // ?????? http get ??????
        HttpGet httpGet = new HttpGet(url);
        // ????????????
        CloseableHttpResponse response = httpClient.execute(httpGet);

        // ????????????????????????200
        if (response.getStatusLine().getStatusCode() == 200) {
            // ????????????????????????
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } else if (response.getStatusLine().getStatusCode() == 500) {
            throw new Exception("??????????????????");
        }
        return null;
    }

    /**
     * ???????????????get???????????????????????????200????????????body???????????????200????????????null
     *
     * @param urlStr  ??????????????????url
     * @param path   ?????????????????????????????????
     * @throws Exception
     */
    public static void downloadFile(String urlStr, String path) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // ?????? http get ??????   ??????https://bbs.csdn.net/topics/380184988
            URL url = new URL(urlStr);
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
            HttpGet httpGet = new HttpGet(uri);
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpGet);

            // ????????????????????????200
            if (response.getStatusLine().getStatusCode() == 200) {
                // ????????????????????????
                InputStream inputStream = response.getEntity().getContent();
                int index;
                byte[] bytes = new byte[1024];
                FileOutputStream downloadFile = new FileOutputStream(path);
                while ((index = inputStream.read(bytes)) != -1) {
                    downloadFile.write(bytes, 0, index);
                    downloadFile.flush();
                }
                inputStream.close();
                downloadFile.close();

            } else if (response.getStatusLine().getStatusCode() == 500) {

                throw new Exception("??????????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????get???????????????????????????200????????????body???????????????200????????????null
     *
     * @param urlStr ??????????????????url
     * @return base64
     * @throws Exception
     */
    public static InputStream downloadFile(String urlStr) {
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // ?????? http get ??????   ??????https://bbs.csdn.net/topics/380184988
            URL url = new URL(urlStr);
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
            HttpGet httpGet = new HttpGet(uri);
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpGet);

            // ????????????????????????200
            if (response.getStatusLine().getStatusCode() == 200) {
                // ????????????????????????
                InputStream inputStream = response.getEntity().getContent();
                return inputStream;
                // int index;
                // byte[] bytes = new byte[1024];
                // FileOutputStream downloadFile = new FileOutputStream(path);
                // while ((index = inputStream.read(bytes)) != -1) {
                //     downloadFile.write(bytes, 0, index);
                //     downloadFile.flush();
                // }
                // inputStream.close();
                // downloadFile.close();

            } else if (response.getStatusLine().getStatusCode() == 500) {
                throw new Exception("??????????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ???????????????????????????
     *
     * @param is
     * @return
     */
    public static byte[] readInputStream(InputStream is) {
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        byte[] buff = new byte[1024 * 2];
        int len = 0;
        try {
            while ((len = is.read(buff)) != -1) {
                writer.write(buff, 0, len);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toByteArray();
    }

    /**
     * ???????????????url???????????????base64?????????
     *
     * @param imgUrl ??????url
     * @return ????????????base64????????????
     */
    public static String image2Base64(String imgUrl) {
        URL url = null;
        InputStream is = null;
        ByteArrayOutputStream outStream = null;
        HttpURLConnection httpUrl = null;
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
            @Override
            public X509Certificate[] getAcceptedIssuers(){return null;}
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }};
        try {
            HttpsURLConnection.setDefaultHostnameVerifier((s, sslSession) -> true);
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            url = new URL(imgUrl);
            httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.connect();
            httpUrl.getInputStream();
            is = httpUrl.getInputStream();
            outStream = new ByteArrayOutputStream();
            //????????????Buffer?????????
            byte[] buffer = new byte[1024];
            //??????????????????????????????????????????-1???????????????????????????
            int len = 0;
            //????????????????????????buffer????????????????????????
            while ((len = is.read(buffer)) != -1) {
                //???????????????buffer???????????????????????????????????????????????????????????????len?????????????????????
                outStream.write(buffer, 0, len);
            }

            // ???????????????Base64??????
            BASE64Encoder decoder = new BASE64Encoder();
            String reg ="[\n-\r]";
            Pattern p = Pattern.compile(reg);
            Matcher m = p.matcher(decoder.encode(outStream.toByteArray()));
            return "data:image/jpg;base64," + m.replaceAll("");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (httpUrl != null) {
                httpUrl.disconnect();
            }
        }
    }

    /**
     * ????????????get???????????????????????????200????????????body???????????????200????????????null
     *
     * @param url
     * @param map
     * @return
     * @throws Exception
     * @Title: doGet
     */
    public static String doGet(String url, Map<String, Object> map) throws Exception {
        URIBuilder uriBuilder = new URIBuilder(url);
        if (map != null) {
            // ??????map,??????????????????
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue().toString());
            }
        }
        // ?????????????????????get??????
        return doGet(uriBuilder.build().toString());

    }


    public static String doGetWithHeader(String url, Map<String, Object> headers) throws Exception {
        CloseableHttpClient httpClient = getIgnoeSSLClient();
        // ?????? http get ??????
        HttpGet httpGet = new HttpGet(url);
        for (String key : headers.keySet()) {
            httpGet.setHeader(key, headers.get(key).toString());
        }
        // httpGet.setHeader("X-App-ID", "" + headers.get("X-App-ID"));
        // httpGet.setHeader("X-Timestamp", "" + headers.get("X-Timestamp"));
        // httpGet.setHeader("X-Sign-Type", "" + headers.get("X-Sign-Type"));
        // httpGet.setHeader("X-Sign", "" + headers.get("X-Sign"));
        // ????????????
        CloseableHttpResponse response = httpClient.execute(httpGet);

        // ????????????????????????200
        if (response.getStatusLine().getStatusCode() == 200) {
            // ????????????????????????
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } else if (response.getStatusLine().getStatusCode() == 500) {
            throw new Exception("??????????????????");
        }
        return null;


    }

    /**
     * ???????????????????????????client
     *
     * @return
     * @throws Exception
     */
    public static CloseableHttpClient getIgnoeSSLClient() throws Exception {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        }).build();

        //??????httpClient
        CloseableHttpClient client = HttpClients.custom().setSSLContext(sslContext).
                setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        return client;
    }

    /**
     * ????????????post??????
     *
     * @param url
     * @param map
     * @return
     * @throws Exception
     * @Title: doPost
     */
    public static String doPost(String url, Map<String, Object> map) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // ??????httpPost??????
        HttpPost httpPost = new HttpPost(url);
        // ??????map????????????????????????????????????????????????from????????????
        if (map != null) {
            List<NameValuePair> list = new ArrayList<NameValuePair>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
            }
            // ??????from????????????
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(list, "UTF-8");

            // ???????????????post???
            httpPost.setEntity(urlEncodedFormEntity);
        }
        // ????????????
        CloseableHttpResponse response = httpClient.execute(httpPost);
        if (response.getStatusLine().getStatusCode() == 200) {
            return EntityUtils.toString(response.getEntity(), "UTF-8");
        } else if (response.getStatusLine().getStatusCode() == 500) {
            throw new Exception("??????????????????");
        }
        return null;
    }


    public static String doPostObject(String url, Object obj) throws Exception {
        return doPost(url, objectToMap(obj));
    }

    public static String doGetObject(String url, Object obj) throws Exception {
        return doGet(url, objectToMap(obj));
    }

    /**
     * ????????????post??????
     *
     * @param url
     * @return
     * @throws Exception
     * @Title: doPost
     */
    public static String doPost(String url) throws Exception {
        return doPost(url, null);
    }

    /**
     * Object???Map
     *
     * @param obj
     * @return
     * @throws Exception
     * @Title: objectToMap
     */
    private static Map<String, Object> objectToMap(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            field.setAccessible(true);
            if (null != field.get(obj)) {
                map.put(field.getName(), field.get(obj));
            }
        }
        return map;
    }

    public static JSONObject getUrlAccess(String urlStr) {
        JSONObject result = new JSONObject();
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(urlStr);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                    .setSocketTimeout(5000).build();
            httpGet.setConfig(requestConfig);// ??????????????????
            CloseableHttpResponse response = httpClient.execute(httpGet);
            result.put("status", response.getStatusLine().getStatusCode());
            result.put("xFrameOptions", response.getHeaders("X-Frame-Options"));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.put("status", -1);
        result.put("xFrameOptions", null);
        return result;
    }


    /**
     * ?????????????????????Get??????
     *
     * @author yangjia
     */
    public static Map<String, Object> doGetWithDigest(String url, JSONObject json, String domainIp, String domainPort, String username, String password) throws Exception {
        CloseableHttpClient httpClient = null;
        String result = null;
        Map<String, Object> maps = new HashMap<String, Object>(16);
        try {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            int port = 80;
            if (StringUtil.valid(domainPort)) {
                port = StringUtil.getInt(domainPort);
            }
            credsProvider.setCredentials(new AuthScope(domainIp, port),
                    new UsernamePasswordCredentials(username, password));
            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            // ??????httpGet??????
            HttpGet httpGet = new HttpGet(url);
            // ???????????????
            httpGet.addHeader("Content-type", "application/json; charset=utf-8");
            httpGet.setHeader("Accept", "application/json");
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            maps.put("result", result);
            System.out.println("??????????????????" + result);
            System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
            System.out.println("???????????????: " + response.getEntity().getContentType());
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return maps;
    }

    /**
     * ?????????????????????Post??????
     *
     * @author yangjia
     */
    public static Map<String, Object> doPostWithDigest(String url, JSONObject json, String domainIp, String domainPort, String username, String password) throws Exception {
        CloseableHttpClient httpClient = null;
        String result = null;
        Map<String, Object> maps = new HashMap<String, Object>(16);
        try {
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            int port = 80;
            if (StringUtil.valid(domainPort)) {
                port = StringUtil.getInt(domainPort);
            }
            credsProvider.setCredentials(new AuthScope(domainIp, port),
                    new UsernamePasswordCredentials(username, password));
            httpClient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
            // ??????httpPost??????
            HttpPost httpPost = new HttpPost(url);
            if (json == null) {
                json = new JSONObject();
            }
            StringEntity paramEntity = new StringEntity(json.toJSONString(), "UTF-8");
            paramEntity.setContentType("application/json");
            httpPost.setEntity(paramEntity);
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            maps.put("result", result);
            System.out.println("??????????????????" + result);
            System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
            System.out.println("???????????????: " + response.getEntity().getContentType());
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return maps;
    }

    /**
     * ?????????
     *
     * @author yangjia
     */
    public static String doGetCloudMetting(String url, JSONObject body, JSONObject headers) {
        boolean logFlag = url.contains("realTimeInfo") || url.contains("conferences/token");
        if (!logFlag) {
            System.out.println("??????url:" + url);
        }
        CloseableHttpClient httpClient = null;
        String result = null;
        try {
            httpClient = HttpClients.custom().build();
            // ??????httpGet??????
            HttpGet httpGet = new HttpGet(url);
            // ???????????????
            httpGet.addHeader("Content-type", "application/json; charset=utf-8");
            httpGet.setHeader("Accept", "application/json");
            if (headers != null) {
                if (StringUtil.valid(headers.getString("Authorization"))) {
                    httpGet.setHeader("Authorization", headers.getString("Authorization"));
                }
                if (StringUtil.valid(headers.getString("Content-Type"))) {
                    httpGet.setHeader("Content-Type", headers.getString("Content-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Access-Token"))) {
                    httpGet.setHeader("X-Access-Token", headers.getString("X-Access-Token"));
                }
                if (StringUtil.valid(headers.getString("X-Login-Type"))) {
                    httpGet.setHeader("X-Login-Type", headers.getString("X-Login-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Password"))) {
                    httpGet.setHeader("X-Password", headers.getString("X-Password"));
                }
                if (StringUtil.valid(headers.getString("X-Conference-Authorization"))) {
                    httpGet.setHeader("X-Conference-Authorization", headers.getString("X-Conference-Authorization"));
                }
            }
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            if (!logFlag) {
                System.out.println("??????????????????" + result);
                System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
                System.out.println("???????????????: " + response.getEntity().getContentType());
            }
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * ?????????
     *
     * @author yangjia
     */
    public static String doPostCloudMetting(String url, JSONObject body, JSONObject headers) {
        System.out.println("??????url:" + url);
        CloseableHttpClient httpClient = null;
        String result = null;
        try {
            httpClient = HttpClients.custom().build();
            // ??????httpPost??????
            HttpPost httpPost = new HttpPost(url);
            if (body == null) {
                body = new JSONObject();
            }
            StringEntity paramEntity = new StringEntity(body.toJSONString(), "UTF-8");
            paramEntity.setContentType("application/json");
            httpPost.setEntity(paramEntity);
            if (headers != null) {
                if (StringUtil.valid(headers.getString("Authorization"))) {
                    httpPost.setHeader("Authorization", headers.getString("Authorization"));
                }
                if (StringUtil.valid(headers.getString("Content-Type"))) {
                    httpPost.setHeader("Content-Type", headers.getString("Content-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Access-Token"))) {
                    httpPost.setHeader("X-Access-Token", headers.getString("X-Access-Token"));
                }
                if (StringUtil.valid(headers.getString("X-Login-Type"))) {
                    httpPost.setHeader("X-Login-Type", headers.getString("X-Login-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Password"))) {
                    httpPost.setHeader("X-Password", headers.getString("X-Password"));
                }
                if (StringUtil.valid(headers.getString("X-Conference-Authorization"))) {
                    httpPost.setHeader("X-Conference-Authorization", headers.getString("X-Conference-Authorization"));
                }
            }
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            System.out.println("??????????????????" + result);
            System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
            System.out.println("???????????????: " + response.getEntity().getContentType());
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * ?????????
     *
     * @author yangjia
     */
    public static int doPostCloudMettingStatusCode(String url, JSONObject body, JSONObject headers) {
        System.out.println("??????url:" + url);
        CloseableHttpClient httpClient = null;
        String result = null;
        int statusCode = 0;
        try {
            httpClient = HttpClients.custom().build();
            // ??????httpPost??????
            HttpPost httpPost = new HttpPost(url);
            if (body == null) {
                body = new JSONObject();
            }
            StringEntity paramEntity = new StringEntity(body.toJSONString(), "UTF-8");
            paramEntity.setContentType("application/json");
            httpPost.setEntity(paramEntity);
            if (headers != null) {
                if (StringUtil.valid(headers.getString("Authorization"))) {
                    httpPost.setHeader("Authorization", headers.getString("Authorization"));
                }
                if (StringUtil.valid(headers.getString("Content-Type"))) {
                    httpPost.setHeader("Content-Type", headers.getString("Content-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Access-Token"))) {
                    httpPost.setHeader("X-Access-Token", headers.getString("X-Access-Token"));
                }
                if (StringUtil.valid(headers.getString("X-Login-Type"))) {
                    httpPost.setHeader("X-Login-Type", headers.getString("X-Login-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Password"))) {
                    httpPost.setHeader("X-Password", headers.getString("X-Password"));
                }
                if (StringUtil.valid(headers.getString("X-Conference-Authorization"))) {
                    httpPost.setHeader("X-Conference-Authorization", headers.getString("X-Conference-Authorization"));
                }
            }
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            System.out.println("??????????????????" + result);
            System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
            System.out.println("???????????????: " + response.getEntity().getContentType());
            statusCode = response.getStatusLine().getStatusCode();
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return statusCode;
    }

    /**
     * ?????????
     *
     * @author yangjia
     */
    public static String doPutCloudMetting(String url, JSONObject body, JSONObject headers) {
        System.out.println("??????url:" + url);
        CloseableHttpClient httpClient = null;
        String result = null;
        try {
            httpClient = HttpClients.custom().build();
            // ??????httpPost??????
            HttpPut httpPut = new HttpPut(url);
            if (body == null) {
                body = new JSONObject();
            }
            StringEntity paramEntity = new StringEntity(body.toJSONString(), "UTF-8");
            paramEntity.setContentType("application/json");
            httpPut.setEntity(paramEntity);
            if (headers != null) {
                if (StringUtil.valid(headers.getString("Authorization"))) {
                    httpPut.setHeader("Authorization", headers.getString("Authorization"));
                }
                if (StringUtil.valid(headers.getString("Content-Type"))) {
                    httpPut.setHeader("Content-Type", headers.getString("Content-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Access-Token"))) {
                    httpPut.setHeader("X-Access-Token", headers.getString("X-Access-Token"));
                }
                if (StringUtil.valid(headers.getString("X-Login-Type"))) {
                    httpPut.setHeader("X-Login-Type", headers.getString("X-Login-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Password"))) {
                    httpPut.setHeader("X-Password", headers.getString("X-Password"));
                }
                if (StringUtil.valid(headers.getString("X-Conference-Authorization"))) {
                    httpPut.setHeader("X-Conference-Authorization", headers.getString("X-Conference-Authorization"));
                }
            }
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            System.out.println("??????????????????" + result);
            System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
            System.out.println("???????????????: " + response.getEntity().getContentType());
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * ?????????
     *
     * @author yangjia
     */
    public static int doPutCloudMettingStatusCode(String url, JSONObject body, JSONObject headers) {
        int statusCode = 0;
        System.out.println("??????url:" + url);
        CloseableHttpClient httpClient = null;
        String result = null;
        try {
            httpClient = HttpClients.custom().build();
            // ??????httpPost??????
            HttpPut httpPut = new HttpPut(url);
            if (body == null) {
                body = new JSONObject();
            }
            StringEntity paramEntity = new StringEntity(body.toJSONString(), "UTF-8");
            paramEntity.setContentType("application/json");
            httpPut.setEntity(paramEntity);
            if (headers != null) {
                if (StringUtil.valid(headers.getString("Authorization"))) {
                    httpPut.setHeader("Authorization", headers.getString("Authorization"));
                }
                if (StringUtil.valid(headers.getString("Content-Type"))) {
                    httpPut.setHeader("Content-Type", headers.getString("Content-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Access-Token"))) {
                    httpPut.setHeader("X-Access-Token", headers.getString("X-Access-Token"));
                }
                if (StringUtil.valid(headers.getString("X-Login-Type"))) {
                    httpPut.setHeader("X-Login-Type", headers.getString("X-Login-Type"));
                }
                if (StringUtil.valid(headers.getString("X-Password"))) {
                    httpPut.setHeader("X-Password", headers.getString("X-Password"));
                }
                if (StringUtil.valid(headers.getString("X-Conference-Authorization"))) {
                    httpPut.setHeader("X-Conference-Authorization", headers.getString("X-Conference-Authorization"));
                }
            }
            // ????????????
            CloseableHttpResponse response = httpClient.execute(httpPut);
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
            System.out.println("??????????????????" + result);
            statusCode = response.getStatusLine().getStatusCode();
            System.out.println("???????????????: " + response.getStatusLine().getStatusCode());
            System.out.println("???????????????: " + response.getEntity().getContentType());
            EntityUtils.consume(response.getEntity());
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return statusCode;
    }

    public static String sendHttpPost(String url, String body) throws Exception {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(body));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        System.out.println(response.getStatusLine().getStatusCode() + "\n");
        HttpEntity entity = response.getEntity();
        String responseContent = EntityUtils.toString(entity, "UTF-8");
        System.out.println(responseContent);
        response.close();
        httpClient.close();
        return responseContent;
    }

    public static String sendPost(String url, String param, String contentType) {
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // ?????????URL???????????????
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            // ???????????????????????????
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type", contentType);
            // ??????POST??????????????????????????????
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // ??????URLConnection????????????????????????
            // out = new PrintWriter(conn.getOutputStream());
            out = new OutputStreamWriter(conn.getOutputStream(), "utf-8"); // 8859_1
            out.write(param); // post???????????????
            // ??????????????????
            // out.print(param);
            // flush??????????????????
            out.flush();
            // ??????BufferedReader??????????????????URL?????????
            int code = conn.getResponseCode();
            if (code == 200) {
                in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("?????? POST ?????????????????????" + e);
            e.printStackTrace();
        }
        // ??????finally?????????????????????????????????
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public static String post(String requestUrl, String accessToken, String params)
            throws Exception {
        String contentType = "application/x-www-form-urlencoded";
        return HttpUtil.post(requestUrl, accessToken, contentType, params);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params)
            throws Exception {
        String encoding = "UTF-8";
        if (requestUrl.contains("nlp")) {
            encoding = "GBK";
        }
        return HttpUtil.post(requestUrl, accessToken, contentType, params, encoding);
    }

    public static String post(String requestUrl, String accessToken, String contentType, String params, String encoding)
            throws Exception {
        String url = requestUrl + "?access_token=" + accessToken;
        return HttpUtil.postGeneralUrl(url, contentType, params, encoding);
    }

    public static String postGeneralUrl(String generalUrl, String contentType, String params, String encoding)
            throws Exception {
        URL url = new URL(generalUrl);
        // ?????????URL???????????????
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // ???????????????????????????
        connection.setRequestProperty("Content-Type", contentType);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // ??????????????????????????????
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());
        out.write(params.getBytes(encoding));
        out.flush();
        out.close();

        // ?????????????????????
        connection.connect();
        // ???????????????????????????
        Map<String, List<String>> headers = connection.getHeaderFields();
        // ??????????????????????????????
        for (String key : headers.keySet()) {
            System.err.println(key + "--->" + headers.get(key));
        }
        // ?????? BufferedReader??????????????????URL?????????
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), encoding));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        return result;
    }


}
