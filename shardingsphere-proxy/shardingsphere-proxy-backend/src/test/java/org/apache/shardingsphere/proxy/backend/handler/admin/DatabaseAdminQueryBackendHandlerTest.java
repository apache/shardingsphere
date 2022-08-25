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

package org.apache.shardingsphere.proxy.backend.handler.admin;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor.SelectTableExecutor;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseAdminQueryBackendHandlerTest extends ProxyContextRestorer {
    
    private DatabaseAdminQueryBackendHandler handler;
    
    @Before
    public void before() throws SQLException {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        ContextManager contextManager = new ContextManager(metaDataContexts, mock(InstanceContext.class));
        ProxyContext.init(contextManager);
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDatabaseName()).thenReturn("db");
        SelectTableExecutor executor = mock(SelectTableExecutor.class, RETURNS_DEEP_STUBS);
        MergedResult mergedResult = new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow("demo_ds_0", "demo_ds_1")));
        when(executor.getMergedResult()).thenReturn(mergedResult);
        when(executor.getQueryResultMetaData().getColumnCount()).thenReturn(1);
        handler = new DatabaseAdminQueryBackendHandler(connectionSession, executor);
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("db");
        when(database.getResource().getDatabaseType()).thenReturn(new MySQLDatabaseType());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("db", database);
        return result;
    }
    
    @Test
    public void assertExecute() throws SQLException {
        assertThat(((QueryResponseHeader) handler.execute()).getQueryHeaders().size(), is(1));
    }
    
    @Test
    public void assertNext() throws SQLException {
        handler.execute();
        assertTrue(handler.next());
        assertFalse(handler.next());
    }
    
    @Test
    public void assertGetRowData() throws SQLException {
        handler.execute();
        assertTrue(handler.next());
        assertThat(handler.getRowData().getData().size(), is(1));
    }
}
