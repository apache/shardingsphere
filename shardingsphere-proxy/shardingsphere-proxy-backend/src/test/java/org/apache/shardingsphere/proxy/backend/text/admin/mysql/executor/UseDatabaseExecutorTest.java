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
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLUseStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UseDatabaseExecutorTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        backendConnection = mock(BackendConnection.class);
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), new StandardMetaDataContexts(getMetaDataMap(), mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), 
                new ShardingSphereUsers(Collections.singleton(new ShardingSphereUser("root", "root", ""))), new ConfigurationProperties(new Properties())));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
            when(metaData.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.emptyList()));
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    @Test
    public void assertExecuteUseStatementBackendHandler() {
        MySQLUseStatement useStatement = mock(MySQLUseStatement.class);
        when(useStatement.getSchema()).thenReturn(String.format(SCHEMA_PATTERN, 0));
        UseDatabaseExecutor useSchemaBackendHandler = new UseDatabaseExecutor(useStatement);
        useSchemaBackendHandler.execute(backendConnection);
        verify(backendConnection).setCurrentSchema(anyString());
    }
}
