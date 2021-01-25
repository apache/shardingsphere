package org.apache.shardingsphere.proxy.backend.text.sctl.explain;

import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.CachedDatabaseMetaData;
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
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
import static org.junit.Assert.assertNull;
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
        handler = new ShardingCTLExplainBackendHandler("sctl:explain select 1", connection);
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(getMetaDataMap(), mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
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
        assertNull(iterator.next());
        assertThat(iterator.next(), is("select 1"));
    }
}
