/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.ui.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Http client util.
 */
public final class HttpClientUtil {
    
    private static final String ENCODING = "UTF-8";
    
    private static final int CONNECT_TIMEOUT = 60 * 1000;
    
    private static final int SOCKET_TIMEOUT = 60 * 1000;
    
    /**
     * Do get method.
     *
     * @param url url of get method
     * @param headers headers of get method
     * @param params parameters of get method
     * @return response string of get method
     * @throws IOException io exception
     * @throws URISyntaxException uri syntax exception
     */
    public static String doGet(final String url, final Map<String, String> headers, final Map<String, String> params) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(newInstanceURIBuilder(url, params).build());
            httpGet.setConfig(RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build());
            packageHeader(headers, httpGet);
            return getHttpClientResult(httpClient, httpGet);
        }
    }
    
    /**
     * Do get method with default headers.
     *
     * @param url url of get method
     * @param params parameters of get method
     * @return response string of get method
     * @throws IOException io exception
     * @throws URISyntaxException uri syntax exception
     */
    public static String doGet(final String url, final Map<String, String> params) throws IOException, URISyntaxException {
        return doGet(url, null, params);
    }
    
    /**
     * Do get method with default headers and without parameters.
     *
     * @param url url of get method
     * @return response string of get method
     * @throws IOException io exception
     * @throws URISyntaxException uri syntax exception
     */
    public static String doGet(final String url) throws IOException, URISyntaxException {
        return doGet(url, null, null);
    }
    
    /**
     * Do post method.
     *
     * @param url url of post method
     * @param headers headers of post method
     * @param params parameters of post method
     * @return response string of post method
     * @throws IOException io exception
     */
    public static String doPost(final String url, final Map<String, String> headers, final Map<String, String> params) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build());
            packageHeader(headers, httpPost);
            packageParam(params, httpPost);
            return getHttpClientResult(httpClient, httpPost);
        }
    }
    
    /**
     * Do post method with json request body.
     *
     * @param url url of post method
     * @param requestBody json request body
     * @return response string of post method
     * @throws IOException io exception
     */
    public static String doPostWithJsonRequestBody(final String url, final String requestBody) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build());
            httpPost.setHeader("Content-type", "application/json");
            StringEntity requestEntity = new StringEntity(requestBody, ENCODING);
            requestEntity.setContentEncoding(ENCODING);
            httpPost.setEntity(requestEntity);
            return getHttpClientResult(httpClient, httpPost);
        }
    }
    
    private static URIBuilder newInstanceURIBuilder(final String url, final Map<String, String> params) throws URISyntaxException {
        URIBuilder result = new URIBuilder(url);
        if (null != params) {
            for (Entry<String, String> each : params.entrySet()) {
                result.setParameter(each.getKey(), each.getValue());
            }
        }
        return result;
    }
    
    private static void packageHeader(final Map<String, String> headers, final HttpRequestBase httpMethod) {
        if (null != headers) {
            for (Entry<String, String> each : headers.entrySet()) {
                httpMethod.setHeader(each.getKey(), each.getValue());
            }
        }
    }
    
    private static void packageParam(final Map<String, String> params, final HttpEntityEnclosingRequestBase httpMethod) throws UnsupportedEncodingException {
        if (null != params && !params.isEmpty()) {
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            for (Entry<String, String> each : params.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(each.getKey(), each.getValue()));
            }
            httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs, ENCODING));
        }
    }
    
    private static String getHttpClientResult(final CloseableHttpClient httpClient, final HttpRequestBase httpMethod) throws IOException {
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpMethod)) {
            return isValidResponse(httpResponse) ? EntityUtils.toString(httpResponse.getEntity(), ENCODING) : "";
        }
    }
    
    private static boolean isValidResponse(final CloseableHttpResponse httpResponse) {
        return null != httpResponse && null != httpResponse.getEntity();
    }
}
