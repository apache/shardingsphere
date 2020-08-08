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
import org.apache.shardingsphere.kernel.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.datasource.JDBCRawBackendDataSourceFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.recognizer.JDBCDriverURLRecognizerEngine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JDBCRawBackendDataSourceFactory.class, JDBCDriverURLRecognizerEngine.class})
public class ProxyDataSourceContextTest {

    @Test
    public void assertEmptySchemaDataSources() {
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        ProxyDataSourceContext proxyDataSourceContext = new ProxyDataSourceContext(schemaDataSources);
        assertTrue(proxyDataSourceContext.getDatabaseType() instanceof MySQLDatabaseType);
        assertTrue(proxyDataSourceContext.getDataSourcesMap().isEmpty());
    }

    @Test(expected = ShardingSphereException.class)
    public void assertWrongMysqlSchemaDataSources() {
        // schameName -- dataSourceName -- dataSource
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl("jdbc:mysql://localhost:3306/test");
        Map<String, DataSourceParameter> dataSourceParameterMap = new LinkedHashMap<>();
        dataSourceParameterMap.put("order1", dataSourceParameter);
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        schemaDataSources.put("order", dataSourceParameterMap);
        ProxyDataSourceContext proxyDataSourceContext = new ProxyDataSourceContext(schemaDataSources);
        assertTrue(proxyDataSourceContext.getDatabaseType() instanceof MySQLDatabaseType);
    }

    @Test(expected = ShardingSphereException.class)
    public void assertThrowFromGetDatabaseTypeName() {
        // schameName -- dataSourceName -- dataSource
        PowerMockito.mockStatic(JDBCDriverURLRecognizerEngine.class);
        PowerMockito.when(JDBCDriverURLRecognizerEngine.getJDBCDriverURLRecognizer(Mockito.anyString())).thenThrow(new ShardingSphereException(""));

        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl("jdbc:mysql://localhost:3306/test");
        Map<String, DataSourceParameter> dataSourceParameterMap = new LinkedHashMap<>();
        dataSourceParameterMap.put("order1", dataSourceParameter);
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        schemaDataSources.put("order", dataSourceParameterMap);
        new ProxyDataSourceContext(schemaDataSources);
    }

    @Test
    public void assertRightMysqlSchemaDataSources() throws Exception {
        // schameName -- dataSourceName -- dataSource
        JDBCBackendDataSourceFactory mock = PowerMockito.mock(JDBCRawBackendDataSourceFactory.class);
        PowerMockito.mockStatic(JDBCRawBackendDataSourceFactory.class);
        PowerMockito.when(JDBCRawBackendDataSourceFactory.class, "getInstance").thenReturn(mock);
        PowerMockito.when(mock.build(Mockito.any(), Mockito.any())).thenReturn(new HikariDataSource());
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUrl("jdbc:mysql://localhost:3306/test");
        Map<String, DataSourceParameter> dataSourceParameterMap = new LinkedHashMap<>();
        dataSourceParameterMap.put("order1", dataSourceParameter);
        Map<String, Map<String, DataSourceParameter>> schemaDataSources = new HashMap<>();
        schemaDataSources.put("order", dataSourceParameterMap);
        ProxyDataSourceContext proxyDataSourceContext = new ProxyDataSourceContext(schemaDataSources);
        assertTrue(proxyDataSourceContext.getDatabaseType() instanceof MySQLDatabaseType);
        assertTrue(proxyDataSourceContext.getDataSourcesMap().size() == 1);
    }
}
