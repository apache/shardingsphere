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

package org.apache.shardingsphere.proxy.backend.communication;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.persist.ConfigCenter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.optimize.core.metadata.FederateSchemaMetadatas;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseCommunicationEngineFactoryTest {
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), new StandardMetaDataContexts(mock(ConfigCenter.class), getMetaDataMap(), mock(ShardingSphereRuleMetaData.class), 
                mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), getOptimizeContextFactory()));
        BackendConnection backendConnection = mock(BackendConnection.class, RETURNS_DEEP_STUBS);
        when(backendConnection.getSchemaName()).thenReturn("schema");
        when(backendConnection.isSerialExecute()).thenReturn(true);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return Collections.singletonMap("schema", result);
    }

    private OptimizeContextFactory getOptimizeContextFactory() {
        OptimizeContextFactory optimizeContextFactory = mock(OptimizeContextFactory.class, RETURNS_DEEP_STUBS);
        when(optimizeContextFactory.getSchemaMetadatas()).thenReturn(new FederateSchemaMetadatas(new HashMap<>()));
        return optimizeContextFactory;
    }
    
    @Test
    public void assertNewTextProtocolInstance() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchemaName()).thenReturn("schema");
        DatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newTextProtocolInstance(mock(SQLStatementContext.class), "schemaName", backendConnection);
        assertNotNull(engine);
        assertThat(engine, instanceOf(DatabaseCommunicationEngine.class));
    }
    
    @Test
    public void assertNewBinaryProtocolInstance() {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getSchemaName()).thenReturn("schema");
        DatabaseCommunicationEngine engine =
                DatabaseCommunicationEngineFactory.getInstance().newBinaryProtocolInstance(mock(SQLStatementContext.class), "schemaName", Collections.emptyList(), backendConnection);
        assertNotNull(engine);
        assertThat(engine, instanceOf(DatabaseCommunicationEngine.class));
    }
}
