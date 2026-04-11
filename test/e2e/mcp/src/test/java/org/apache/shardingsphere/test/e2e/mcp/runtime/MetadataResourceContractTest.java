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

package org.apache.shardingsphere.test.e2e.mcp.runtime;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Metadata resource contract test.
 */
public interface MetadataResourceContractTest {
    
    /**
     * Launch contract runtime.
     *
     * @throws IOException IO exception
     */
    void launchContractRuntime() throws IOException;
    
    /**
     * Create contract HTTP client.
     *
     * @return contract HTTP client
     */
    HttpClient createContractHttpClient();
    
    /**
     * Initialize contract session.
     *
     * @param httpClient http client
     * @return session ID
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    String initializeContractSession(HttpClient httpClient) throws IOException, InterruptedException;
    
    /**
     * Read contract resource.
     *
     * @param httpClient http client
     * @param sessionId session ID
     * @param resourceUri resource URI
     * @return HTTP response
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    HttpResponse<String> readContractResource(HttpClient httpClient, String sessionId, String resourceUri) throws IOException, InterruptedException;
    
    /**
     * Get contract resource payload.
     *
     * @param responseBody response body
     * @return payload
     */
    Map<String, Object> getContractResourcePayload(String responseBody);
    
    /**
     * Get expected table columns.
     *
     * @return expected table columns
     */
    List<String> getExpectedTableColumns();
    
    /**
     * Get expected table indexes.
     *
     * @return expected table indexes
     */
    List<String> getExpectedTableIndexes();
    
    /**
     * Assert service capabilities resource.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    @Test
    default void assertServiceCapabilitiesResource() throws IOException, InterruptedException {
        launchContractRuntime();
        HttpClient httpClient = createContractHttpClient();
        String sessionId = initializeContractSession(httpClient);
        HttpResponse<String> actual = readContractResource(httpClient, sessionId, "shardingsphere://capabilities");
        assertThat(actual.statusCode(), is(200));
        assertThat(getContractResourcePayload(actual.body()).get("supportedTools"), is(List.of("search_metadata", "execute_query")));
    }
    
    /**
     * Assert table detail resource.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    @Test
    default void assertTableDetailResource() throws IOException, InterruptedException {
        launchContractRuntime();
        HttpClient httpClient = createContractHttpClient();
        String sessionId = initializeContractSession(httpClient);
        HttpResponse<String> actual = readContractResource(httpClient, sessionId, "shardingsphere://databases/logic_db/schemas/public/tables/orders");
        assertThat(actual.statusCode(), is(200));
        List<?> items = getPayloadItems(getContractResourcePayload(actual.body()));
        assertThat(items.size(), is(1));
        Map<?, ?> item = (Map<?, ?>) items.get(0);
        assertThat(String.valueOf(item.get("table")), is("orders"));
        assertThat(getNestedNames(item, "columns", "column"), is(getExpectedTableColumns()));
        assertThat(getNestedNames(item, "indexes", "index"), is(getExpectedTableIndexes()));
    }
    
    private static List<?> getPayloadItems(final Map<String, Object> payload) {
        return (List<?>) payload.get("items");
    }
    
    private static List<String> getNestedNames(final Map<?, ?> item, final String nestedKey, final String nameKey) {
        return ((List<?>) item.get(nestedKey)).stream().map(each -> String.valueOf(((Map<?, ?>) each).get(nameKey))).toList();
    }
}
