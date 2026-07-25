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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * MCP payload assertions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MCPPayloadAssertions {
    
    /**
     * Assert one item field.
     *
     * @param payload MCP payload
     * @param key item key
     * @param expectedValue expected value
     */
    public static void assertSingleItemValue(final Map<String, Object> payload, final String key, final String expectedValue) {
        assertThat(String.valueOf(getSingleItem(payload).get(key)), is(expectedValue));
    }
    
    /**
     * Assert item field values.
     *
     * @param payload MCP payload
     * @param key item key
     * @param expectedValues expected values
     */
    public static void assertItemValues(final Map<String, Object> payload, final String key, final List<String> expectedValues) {
        assertThat(getItemValues(payload, key), is(expectedValues));
    }
    
    /**
     * Get single item.
     *
     * @param payload MCP payload
     * @return single item
     */
    public static Map<String, Object> getSingleItem(final Map<String, Object> payload) {
        List<Map<String, Object>> items = getItems(payload);
        assertThat(items.size(), is(1));
        return items.get(0);
    }
    
    /**
     * Find item.
     *
     * @param payload MCP payload
     * @param key item key
     * @param expectedValue expected value
     * @return matched item
     */
    public static Map<String, Object> findItem(final Map<String, Object> payload, final String key, final String expectedValue) {
        return getItems(payload).stream().filter(each -> expectedValue.equals(each.get(key))).findFirst().orElseThrow();
    }
    
    private static List<String> getItemValues(final Map<String, Object> payload, final String key) {
        return getItems(payload).stream().map(each -> String.valueOf(each.get(key))).toList();
    }
    
    private static List<Map<String, Object>> getItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.getRequiredObjectList(payload, "items");
    }
    
}
