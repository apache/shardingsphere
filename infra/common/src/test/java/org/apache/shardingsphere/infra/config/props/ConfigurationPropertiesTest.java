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

import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class ConfigurationPropertiesTest {
    
    @Test
    public void assertGetValue() {
        ConfigurationProperties actual = new ConfigurationProperties(createProperties());
        assertThat(actual.getValue(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL), is(LoggerLevel.DEBUG));
        assertTrue(actual.getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertTrue(actual.getValue(ConfigurationPropertyKey.SQL_SIMPLE));
        assertThat(actual.getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), is(20));
        assertTrue(actual.getValue(ConfigurationPropertyKey.CHECK_TABLE_META_DATA_ENABLED));
        assertThat(actual.getValue(ConfigurationPropertyKey.SQL_FEDERATION_TYPE), is("ORIGINAL"));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE), is("PostgreSQL"));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(20));
        assertTrue(actual.getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE), is(BackendExecutorType.OLTP));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS), is(20));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_MYSQL_DEFAULT_VERSION), is("5.7.22"));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_DEFAULT_PORT), is(3308));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG), is(1024));
    }
    
    private Properties createProperties() {
        return PropertiesBuilder.build(
                new Property(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL.getKey(), LoggerLevel.DEBUG.toString()),
                new Property(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.SQL_SIMPLE.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE.getKey(), "20"),
                new Property(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY.getKey(), "20"),
                new Property(ConfigurationPropertyKey.CHECK_TABLE_META_DATA_ENABLED.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.SQL_FEDERATION_TYPE.getKey(), "ORIGINAL"),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "PostgreSQL"),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_HINT_ENABLED.getKey(), Boolean.TRUE.toString()),
                new Property(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE.getKey(), BackendExecutorType.OLTP.name()),
                new Property(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS.getKey(), "20"),
                new Property(ConfigurationPropertyKey.PROXY_MYSQL_DEFAULT_VERSION.getKey(), "5.7.22"),
                new Property(ConfigurationPropertyKey.PROXY_DEFAULT_PORT.getKey(), "3308"),
                new Property(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG.getKey(), "1024"));
    }
    
    @Test
    public void assertGetDefaultValue() {
        ConfigurationProperties actual = new ConfigurationProperties(new Properties());
        assertThat(actual.getValue(ConfigurationPropertyKey.SYSTEM_LOG_LEVEL), is(LoggerLevel.INFO));
        assertFalse(actual.getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertFalse(actual.getValue(ConfigurationPropertyKey.SQL_SIMPLE));
        assertThat(actual.getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(0));
        assertThat(actual.getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), is(1));
        assertFalse(actual.getValue(ConfigurationPropertyKey.CHECK_TABLE_META_DATA_ENABLED));
        assertThat(actual.getValue(ConfigurationPropertyKey.SQL_FEDERATION_TYPE), is("NONE"));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE), is(""));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_FLUSH_THRESHOLD), is(128));
        assertFalse(actual.getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_BACKEND_QUERY_FETCH_SIZE), is(-1));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_EXECUTOR_SIZE), is(0));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_BACKEND_EXECUTOR_SUITABLE), is(BackendExecutorType.OLAP));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_FRONTEND_MAX_CONNECTIONS), is(0));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_MYSQL_DEFAULT_VERSION), is("5.7.22"));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_DEFAULT_PORT), is(3307));
        assertThat(actual.getValue(ConfigurationPropertyKey.PROXY_NETTY_BACKLOG), is(1024));
    }
}
