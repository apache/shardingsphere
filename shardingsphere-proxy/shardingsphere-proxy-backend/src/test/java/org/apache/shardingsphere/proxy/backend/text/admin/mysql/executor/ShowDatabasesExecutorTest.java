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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShowDatabasesExecutorTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private ShowDatabasesExecutor showDatabasesExecutor;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        showDatabasesExecutor = new ShowDatabasesExecutor();
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        StandardMetaDataContexts metaDataContexts = new StandardMetaDataContexts(mock(DistMetaDataPersistService.class), getMetaDataMap(), mock(ShardingSphereRuleMetaData.class),
                mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new LinkedHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
            when(metaData.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        showDatabasesExecutor.execute(mockBackendConnection());
        assertThat(showDatabasesExecutor.getQueryResultMetaData().getColumnCount(), is(1));
        int count = 0;
        while (showDatabasesExecutor.getMergedResult().next()) {
            assertThat(showDatabasesExecutor.getMergedResult().getValue(1, Object.class), is(String.format(SCHEMA_PATTERN, count)));
            count++;
        }
    }
    
    private BackendConnection mockBackendConnection() {
        BackendConnection result = mock(BackendConnection.class);
        when(result.getGrantee()).thenReturn(new Grantee("root", ""));
        return result;
    }
}
