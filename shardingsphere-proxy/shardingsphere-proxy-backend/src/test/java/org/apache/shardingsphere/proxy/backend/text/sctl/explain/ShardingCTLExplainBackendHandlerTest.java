package org.apache.shardingsphere.proxy.backend.text.sctl.explain;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.optimize.context.OptimizeContextFactory;
import org.apache.shardingsphere.infra.persist.DistMetaDataPersistService;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

public final class ShardingCTLExplainBackendHandlerTest {
    
    private ShardingCTLExplainBackendHandler handler;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        BackendConnection connection = mock(BackendConnection.class);
        when(connection.getSchemaName()).thenReturn("schema");
        when(connection.getDefaultSchemaName()).thenReturn("schema");
        handler = new ShardingCTLExplainBackendHandler("sctl:explain select 1", connection);
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(DistMetaDataPersistService.class), getMetaDataMap(),
                mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizeContextFactory.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereResource resource = new ShardingSphereResource(
                Collections.singletonMap("ds0", mock(DataSource.class)), mock(DataSourcesMetaData.class, RETURNS_DEEP_STUBS), mock(CachedDatabaseMetaData.class), new MySQLDatabaseType());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("schema", 
                resource, new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(mock(ShardingSphereRule.class))), mock(ShardingSphereSchema.class));
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test
    public void assertGetRowData() throws SQLException {
        handler.execute();
        assertTrue(handler.next());
        Iterator<Object> iterator = handler.getRowData().iterator();
        assertThat(iterator.next(), is("ds0"));
        assertThat(iterator.next(), is("select 1"));
    }
}
