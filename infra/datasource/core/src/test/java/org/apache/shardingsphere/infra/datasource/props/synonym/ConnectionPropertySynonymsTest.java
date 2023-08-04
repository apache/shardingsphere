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

package org.apache.shardingsphere.infra.datasource.props.synonym;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ConnectionPropertySynonymsTest {
    
    @Test
    void assertGetStandardPropertiesWithStandardProperties() {
        ConnectionPropertySynonyms actual = new ConnectionPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        assertThat(actual.getStandardProperties().size(), is(4));
        assertThat(actual.getStandardProperties().get("dataSourceClassName"), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getStandardProperties().get("url"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getStandardProperties().get("username"), is("root"));
        assertThat(actual.getStandardProperties().get("password"), is("root"));
    }
    
    @Test
    void assertGetStandardPropertiesWithLocalProperties() {
        ConnectionPropertySynonyms actual = new ConnectionPropertySynonyms(createLocalProperties(), createPropertySynonyms());
        assertThat(actual.getStandardProperties().size(), is(4));
        assertThat(actual.getStandardProperties().get("dataSourceClassName"), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getStandardProperties().get("url"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getStandardProperties().get("username"), is("root"));
        assertThat(actual.getStandardProperties().get("password"), is("root"));
    }
    
    @Test
    void assertGetLocalPropertiesWithStandardProperties() {
        ConnectionPropertySynonyms actual = new ConnectionPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        assertThat(actual.getLocalProperties().size(), is(4));
        assertThat(actual.getStandardProperties().get("dataSourceClassName"), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getLocalProperties().get("jdbcUrl"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getLocalProperties().get("user"), is("root"));
        assertThat(actual.getLocalProperties().get("password"), is("root"));
    }
    
    @Test
    void assertGetLocalPropertiesWithLocalProperties() {
        ConnectionPropertySynonyms actual = new ConnectionPropertySynonyms(createLocalProperties(), createPropertySynonyms());
        assertThat(actual.getLocalProperties().size(), is(4));
        assertThat(actual.getStandardProperties().get("dataSourceClassName"), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getLocalProperties().get("jdbcUrl"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getLocalProperties().get("user"), is("root"));
        assertThat(actual.getLocalProperties().get("password"), is("root"));
    }
    
    private Map<String, Object> createStandardProperties() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
    
    private Map<String, Object> createLocalProperties() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        result.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("user", "root");
        result.put("password", "root");
        return result;
    }
    
    private Map<String, String> createPropertySynonyms() {
        Map<String, String> result = new LinkedHashMap<>(2, 1F);
        result.put("url", "jdbcUrl");
        result.put("username", "user");
        return result;
    }
}
