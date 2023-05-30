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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;

class PoolPropertySynonymsTest {
    
    @Test
    void assertGetStandardPropertiesWithStandardProperties() {
        PoolPropertySynonyms actual = new PoolPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        assertThat(actual.getStandardProperties().size(), is(6));
        assertThat(actual.getStandardProperties().get("connectionTimeoutMilliseconds"), is(30000));
        assertThat(actual.getStandardProperties().get("idleTimeoutMilliseconds"), is(180000));
        assertThat(actual.getStandardProperties().get("maxLifetimeMilliseconds"), is(180000));
        assertThat(actual.getStandardProperties().get("maxPoolSize"), is(30));
        assertThat(actual.getStandardProperties().get("minPoolSize"), is(10));
        assertFalse((Boolean) actual.getStandardProperties().get("readOnly"));
    }
    
    @Test
    void assertGetStandardPropertiesWithLocalProperties() {
        PoolPropertySynonyms actual = new PoolPropertySynonyms(createLocalProperties(), createPropertySynonyms());
        assertThat(actual.getStandardProperties().size(), is(6));
        assertThat(actual.getStandardProperties().get("connectionTimeoutMilliseconds"), is(30000));
        assertThat(actual.getStandardProperties().get("idleTimeoutMilliseconds"), is(180000));
        assertThat(actual.getStandardProperties().get("maxLifetimeMilliseconds"), is(180000));
        assertThat(actual.getStandardProperties().get("maxPoolSize"), is(30));
        assertThat(actual.getStandardProperties().get("minPoolSize"), is(10));
        assertFalse((Boolean) actual.getStandardProperties().get("readOnly"));
    }
    
    @Test
    void assertGetLocalPropertiesWithStandardProperties() {
        PoolPropertySynonyms actual = new PoolPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        assertThat(actual.getLocalProperties().size(), is(6));
        assertThat(actual.getLocalProperties().get("connectionTimeout"), is(30000));
        assertThat(actual.getLocalProperties().get("idleTimeout"), is(180000));
        assertThat(actual.getLocalProperties().get("maxLifetime"), is(180000));
        assertThat(actual.getLocalProperties().get("maximumPoolSize"), is(30));
        assertThat(actual.getLocalProperties().get("minimumIdle"), is(10));
        assertFalse((Boolean) actual.getLocalProperties().get("readOnly"));
    }
    
    @Test
    void assertGetLocalPropertiesWithLocalProperties() {
        PoolPropertySynonyms actual = new PoolPropertySynonyms(createLocalProperties(), createPropertySynonyms());
        assertThat(actual.getLocalProperties().size(), is(6));
        assertThat(actual.getLocalProperties().get("connectionTimeout"), is(30000));
        assertThat(actual.getLocalProperties().get("idleTimeout"), is(180000));
        assertThat(actual.getLocalProperties().get("maxLifetime"), is(180000));
        assertThat(actual.getLocalProperties().get("maximumPoolSize"), is(30));
        assertThat(actual.getLocalProperties().get("minimumIdle"), is(10));
        assertFalse((Boolean) actual.getLocalProperties().get("readOnly"));
    }
    
    private Map<String, Object> createStandardProperties() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("connectionTimeoutMilliseconds", 30000);
        result.put("idleTimeoutMilliseconds", 180000);
        result.put("maxLifetimeMilliseconds", 180000);
        result.put("maxPoolSize", 30);
        result.put("minPoolSize", 10);
        result.put("readOnly", false);
        return result;
    }
    
    private Map<String, Object> createLocalProperties() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("connectionTimeout", 30000);
        result.put("idleTimeout", 180000);
        result.put("maxLifetime", 180000);
        result.put("maximumPoolSize", 30);
        result.put("minimumIdle", 10);
        result.put("readOnly", false);
        return result;
    }
    
    private Map<String, String> createPropertySynonyms() {
        Map<String, String> result = new LinkedHashMap<>(5, 1F);
        result.put("connectionTimeoutMilliseconds", "connectionTimeout");
        result.put("idleTimeoutMilliseconds", "idleTimeout");
        result.put("maxLifetimeMilliseconds", "maxLifetime");
        result.put("maxPoolSize", "maximumPoolSize");
        result.put("minPoolSize", "minimumIdle");
        return result;
    }
}
