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

package org.apache.shardingsphere.infra.config.props;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurationPropertiesTest {
    
    @Test
    void assertGetValue() {
        ConfigurationProperties actual = new ConfigurationProperties(createProperties());
        assertThat(actual.getValue(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL), is(LoggerLevel.DEBUG));
        assertTrue((Boolean) actual.getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertTrue((Boolean) actual.getValue(ConfigurationPropertyKey.SQL_SIMPLE));
        assertThat(actual.getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), is(20));
        assertTrue((Boolean) actual.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE), is(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL")));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_DEFAULT_PORT), is(3308));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG), is(1024));
        assertThat(actual.getValue(ConfigurationPropertyKey.CDC_SERVER_PORT), is(33071));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL.getKey(), LoggerLevel.DEBUG.toString()),
                new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.SQL_SIMPLE.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE.getKey(), "20"),
                new Property(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), "20"),
                new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "PostgreSQL"),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getKey(), "3308"),
                new Property(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG.getKey(), "1024"),
                new Property(ConfigurationPropertyKey.CDC_SERVER_PORT.getKey(), "33071"));
    }
    
    @Test
    void assertGetDefaultValue() {
        ConfigurationProperties actual = new ConfigurationProperties(new Properties());
        assertThat(actual.getValue(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL), is(LoggerLevel.INFO));
        assertFalse((Boolean) actual.getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertFalse((Boolean) actual.getValue(ConfigurationPropertyKey.SQL_SIMPLE));
        assertThat(actual.getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(0));
        assertThat(actual.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), is(1));
        assertFalse((Boolean) actual.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED));
        assertNull(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(128));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE), is(-1));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE), is(0));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS), is(0));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_DEFAULT_PORT), is(3307));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG), is(1024));
        assertThat(actual.getValue(ConfigurationPropertyKey.CDC_SERVER_PORT), is(33071));
    }
}
