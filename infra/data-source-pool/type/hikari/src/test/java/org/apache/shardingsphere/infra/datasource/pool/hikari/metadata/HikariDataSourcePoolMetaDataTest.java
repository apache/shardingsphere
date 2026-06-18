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

package org.apache.shardingsphere.infra.datasource.pool.hikari.metadata;

import org.apache.shardingsphere.infra.datasource.pool.metadata.DataSourcePoolMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HikariDataSourcePoolMetaDataTest {
    
    private final DataSourcePoolMetaData metaData = TypedSPILoader.getService(DataSourcePoolMetaData.class, "com.zaxxer.hikari.HikariDataSource");
    
    @Test
    void assertGetDefaultProperties() {
        Map<String, Object> actual = metaData.getDefaultProperties();
        assertThat(actual, hasEntry("connectionTimeout", 30L * 1000L));
        assertThat(actual, hasEntry("idleTimeout", 60L * 1000L));
        assertThat(actual, hasEntry("maxLifetime", 30L * 70L * 1000L));
        assertThat(actual, hasEntry("maximumPoolSize", 50));
        assertThat(actual, hasEntry("minimumIdle", 1));
        assertThat(actual, hasEntry("readOnly", false));
        assertThat(actual, hasEntry("keepaliveTime", 0));
        assertTrue(metaData.isDefault());
    }
    
    @Test
    void assertGetSkippedProperties() {
        Map<String, Object> actual = metaData.getSkippedProperties();
        assertThat(actual, hasEntry("minimumIdle", -1));
        assertThat(actual, hasEntry("maximumPoolSize", -1));
    }
    
    @Test
    void assertGetPropertySynonyms() {
        Map<String, String> actual = metaData.getPropertySynonyms();
        assertThat(actual, hasEntry("url", "jdbcUrl"));
        assertThat(actual, hasEntry("connectionTimeoutMilliseconds", "connectionTimeout"));
        assertThat(actual, hasEntry("idleTimeoutMilliseconds", "idleTimeout"));
        assertThat(actual, hasEntry("maxLifetimeMilliseconds", "maxLifetime"));
        assertThat(actual, hasEntry("maxPoolSize", "maximumPoolSize"));
        assertThat(actual, hasEntry("minPoolSize", "minimumIdle"));
    }
    
    @Test
    void assertGetTransientFieldNames() {
        assertThat(metaData.getTransientFieldNames(), hasItems("running", "poolName", "closed"));
    }
    
    @Test
    void assertGetFieldMetaData() {
        assertThat(metaData.getFieldMetaData(), isA(HikariDataSourcePoolFieldMetaData.class));
    }
}
