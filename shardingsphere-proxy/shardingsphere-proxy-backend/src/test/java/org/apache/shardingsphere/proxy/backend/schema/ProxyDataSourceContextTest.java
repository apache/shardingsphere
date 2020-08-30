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

package org.apache.shardingsphere.proxy.backend.schema;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCRawBackendDataSourceFactory;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.reset;

public final class ProxyDataSourceContextTest {

    @Test
    public void assertEmptySchemaDataSources() {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        ProxyDataSourceContext proxyDataSourceContext = new ProxyDataSourceContext(schemaDataSources);
        assertThat(proxyDataSourceContext.getDatabaseType(), instanceOf(MySQLDatabaseType.class));
        assertTrue(proxyDataSourceContext.getDataSourcesMap().isEmpty());
    }

    @Test(expected = ShardingSphereException.class)
    public void assertWrongSchemaDataSources() {
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl("jdbc11:mysql11:xxx");
        Map<String, DataSourceParameter> dataSourceParameterMap = new LinkedHashMap<>();
        dataSourceParameterMap.put("order1", dataSourceParameter);
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        schemaDataSources.put("order", dataSourceParameterMap);
        new ProxyDataSourceContext(schemaDataSources);
    }

    @Test(expected = ShardingSphereException.class)
    public void assertThrowByBuild() throws Exception {
        JDBCRawBackendDataSourceFactory jdbcRawBackendDataSourceFactory = mock(JDBCRawBackendDataSourceFactory.class);
        when(jdbcRawBackendDataSourceFactory.build(anyString(), any())).thenThrow(new ShardingSphereException(""));
        build(jdbcRawBackendDataSourceFactory);
        reset(jdbcRawBackendDataSourceFactory);
    }

    @Test
    public void assertRightMysqlSchemaDataSources() throws Exception {
        JDBCRawBackendDataSourceFactory jdbcRawBackendDataSourceFactory = mock(JDBCRawBackendDataSourceFactory.class);
        when(jdbcRawBackendDataSourceFactory.build(anyString(), any())).thenReturn(new HikariDataSource());
        ProxyDataSourceContext proxyDataSourceContext = build(jdbcRawBackendDataSourceFactory);
        assertThat(proxyDataSourceContext.getDatabaseType(), instanceOf(MySQLDatabaseType.class));
        assertTrue(proxyDataSourceContext.getDataSourcesMap().size() == 1);
        reset(jdbcRawBackendDataSourceFactory);
    }

    private ProxyDataSourceContext build(final JDBCRawBackendDataSourceFactory jdbcRawBackendDataSourceFactory) throws Exception {
        JDBCRawBackendDataSourceFactory jdbcBackendDataSourceFactory = (JDBCRawBackendDataSourceFactory) JDBCRawBackendDataSourceFactory.getInstance();
        Class<?> jdbcBackendDataSourceFactoryClass = jdbcBackendDataSourceFactory.getClass();
        Field field = jdbcBackendDataSourceFactoryClass.getDeclaredField("INSTANCE");
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(field, jdbcRawBackendDataSourceFactory);
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl("jdbc:mysql:xxx");
        Map<String, DataSourceParameter> dataSourceParameterMap = new LinkedHashMap<>();
        dataSourceParameterMap.put("order1", dataSourceParameter);
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        schemaDataSources.put("order", dataSourceParameterMap);
        return new ProxyDataSourceContext(schemaDataSources);
    }
}
