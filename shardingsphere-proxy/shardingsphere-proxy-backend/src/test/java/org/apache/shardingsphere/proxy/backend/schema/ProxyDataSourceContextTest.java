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
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.factory.JDBCRawBackendDataSourceFactory;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ProxyDataSourceContextTest {
    
    @Mock
    private JDBCRawBackendDataSourceFactory factory;
    
    @After
    public void tearDown() {
        reset(factory);
    }
    
    @Test
    public void assertEmptySchemaDataSources() {
        ProxyDataSourceContext proxyDataSourceContext = new ProxyDataSourceContext(Collections.emptyMap());
        assertThat(proxyDataSourceContext.getDatabaseType(), instanceOf(MySQLDatabaseType.class));
        assertTrue(proxyDataSourceContext.getDataSourcesMap().isEmpty());
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertWrongSchemaDataSources() {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = createDataSourceParametersMap("jdbc11:mysql11:xxx");
        new ProxyDataSourceContext(schemaDataSources);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertThrowByBuild() {
        when(factory.build(anyString(), any())).thenThrow(new ShardingSphereException(""));
        setFactoryInstance(factory);
        new ProxyDataSourceContext(createDataSourceParametersMap("jdbc:mysql:xxx"));
    }
    
    @Test
    public void assertRightMysqlSchemaDataSources() {
        when(factory.build(anyString(), any())).thenReturn(new HikariDataSource());
        setFactoryInstance(factory);
        ProxyDataSourceContext proxyDataSourceContext = new ProxyDataSourceContext(createDataSourceParametersMap("jdbc:mysql:xxx"));
        assertThat(proxyDataSourceContext.getDatabaseType(), instanceOf(MySQLDatabaseType.class));
        assertThat(proxyDataSourceContext.getDataSourcesMap().size(), is(1));
    }
    
    private Map<String, Map<String, DataSourceParameter>> createDataSourceParametersMap(final String url) {
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl(url);
        Map<String, DataSourceParameter> dataSourceParameterMap = new LinkedHashMap<>(1, 1);
        dataSourceParameterMap.put("order1", dataSourceParameter);
        Map<String, Map<String, DataSourceParameter>> result = new HashMap<>(1, 1);
        result.put("order", dataSourceParameterMap);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setFactoryInstance(final JDBCRawBackendDataSourceFactory factory) {
        JDBCRawBackendDataSourceFactory jdbcBackendDataSourceFactory = (JDBCRawBackendDataSourceFactory) JDBCRawBackendDataSourceFactory.getInstance();
        Class<?> factoryClass = jdbcBackendDataSourceFactory.getClass();
        Field field = factoryClass.getDeclaredField("INSTANCE");
        Field modifiers = field.getClass().getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.setAccessible(true);
        field.set(field, factory);
    }
}
