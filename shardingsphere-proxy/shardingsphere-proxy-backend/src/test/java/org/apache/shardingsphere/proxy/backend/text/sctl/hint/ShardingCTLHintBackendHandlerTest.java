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

package org.apache.shardingsphere.proxy.backend.text.sctl.hint;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.proxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.proxy.backend.text.sctl.hint.internal.HintManagerHolder;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingCTLHintBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getProps().getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)).thenReturn(true);
        ProxyContext.getInstance().init(metaDataContexts, mock(TransactionContexts.class));
    }
    
    @Test(expected = InvalidShardingCTLFormatException.class)
    public void assertInvalidShardingCTLFormat() {
        clearThreadLocal();
        String sql = "sctl:hint1 xx=yy";
        new ShardingCTLHintBackendHandler(sql, backendConnection).execute();
    }
    
    private void clearThreadLocal() {
        HintManagerHolder.get().close();
        HintManagerHolder.remove();
    }
    
    @Test
    public void assertSetPrimaryOnly() {
        clearThreadLocal();
        String sql = "sctl:hint set primary_only=true ";
        ShardingCTLHintBackendHandler hintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(hintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertTrue(HintManager.isPrimaryRouteOnly());
    }
    
    @Test
    public void assertSetDatabaseShardingValueTable() {
        clearThreadLocal();
        String sql = "sctl:hint set databaseShardingValue=100";
        ShardingCTLHintBackendHandler hintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(hintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertThat(HintManager.getDatabaseShardingValues().iterator().next().toString(), is("100"));
    }
    
    @Test
    public void assertAddDatabaseShardingValue() {
        clearThreadLocal();
        String sql = "sctl:hint addDatabaseShardingValue user=100 ";
        ShardingCTLHintBackendHandler hintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(hintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertThat(HintManager.getDatabaseShardingValues("user").iterator().next().toString(), is("100"));
    }
    
    @Test
    public void assertAddTableShardingValue() {
        clearThreadLocal();
        String sql = "sctl:hint addTableShardingValue  user=100 ";
        ShardingCTLHintBackendHandler hintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(hintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertThat(HintManager.getTableShardingValues("user").iterator().next().toString(), is("100"));
    }
    
    @Test
    public void assertClear() {
        clearThreadLocal();
        String sql = "sctl:hint clear ";
        ShardingCTLHintBackendHandler hintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(hintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertThat(HintManager.getInstance(), instanceOf(HintManager.class));
        HintManager.clear();
    }
    
    @Test
    public void assertShowStatus() throws SQLException {
        clearThreadLocal();
        String sql = "sctl:hint show status";
        ShardingCTLHintBackendHandler defaultHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        BackendResponse backendResponse = defaultHintBackendHandler.execute();
        assertThat(backendResponse, instanceOf(QueryResponse.class));
        assertThat(((QueryResponse) backendResponse).getQueryHeaders().get(0).getColumnLabel(), is("primary_only"));
        assertThat(((QueryResponse) backendResponse).getQueryHeaders().get(1).getColumnLabel(), is("sharding_type"));
        assertTrue(defaultHintBackendHandler.next());
        Iterator<Object> defaultRowData = defaultHintBackendHandler.getRowData().iterator();
        assertThat(defaultRowData.next().toString(), is("false"));
        assertThat(defaultRowData.next().toString(), is("databases_tables"));
        assertFalse(defaultHintBackendHandler.next());
        String setPrimaryOnlySQL = "sctl:hint set primary_only=true";
        String setDatabaseOnlySQL = "sctl:hint set DatabaseShardingValue=100";
        new ShardingCTLHintBackendHandler(setPrimaryOnlySQL, backendConnection).execute();
        new ShardingCTLHintBackendHandler(setDatabaseOnlySQL, backendConnection).execute();
        ShardingCTLHintBackendHandler updateHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        updateHintBackendHandler.execute();
        assertTrue(updateHintBackendHandler.next());
        Iterator<Object> updateRowData = updateHintBackendHandler.getRowData().iterator();
        assertThat(updateRowData.next().toString(), is("true"));
        assertThat(updateRowData.next().toString(), is("databases_only"));
        assertFalse(updateHintBackendHandler.next());
    }
    
    @Test
    public void assertShowTableStatus() throws SQLException, NoSuchFieldException, IllegalAccessException {
        clearThreadLocal();
        when(backendConnection.getSchemaName()).thenReturn("schema");
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.PROXY_HINT_ENABLED.getKey(), Boolean.TRUE.toString());
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(getMetaDataMap(), mock(ExecutorEngine.class), new Authentication(), new ConfigurationProperties(props), new MySQLDatabaseType()));
        String sql = "sctl:hint show table status";
        ShardingCTLHintBackendHandler defaultHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        BackendResponse backendResponse = defaultHintBackendHandler.execute();
        assertThat(backendResponse, instanceOf(QueryResponse.class));
        assertThat(((QueryResponse) backendResponse).getQueryHeaders().get(0).getColumnLabel(), is("table_name"));
        assertThat(((QueryResponse) backendResponse).getQueryHeaders().get(1).getColumnLabel(), is("database_sharding_values"));
        assertThat(((QueryResponse) backendResponse).getQueryHeaders().get(2).getColumnLabel(), is("table_sharding_values"));
        assertTrue(defaultHintBackendHandler.next());
        Iterator<Object> defaultRowData = defaultHintBackendHandler.getRowData().iterator();
        assertThat(defaultRowData.next().toString(), is("user"));
        assertThat(defaultRowData.next().toString(), is(""));
        assertThat(defaultRowData.next().toString(), is(""));
        assertFalse(defaultHintBackendHandler.next());
        String addDatabaseShardingValueSQL = "sctl:hint addDatabaseshardingvalue user=100";
        String addTableShardingValueSQL1 = "sctl:hint addTableshardingvalue user=200";
        String addTableShardingValueSQL2 = "sctl:hint addTableshardingvalue user=300";
        new ShardingCTLHintBackendHandler(addDatabaseShardingValueSQL, backendConnection).execute();
        new ShardingCTLHintBackendHandler(addTableShardingValueSQL1, backendConnection).execute();
        new ShardingCTLHintBackendHandler(addTableShardingValueSQL2, backendConnection).execute();
        ShardingCTLHintBackendHandler updateHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        updateHintBackendHandler.execute();
        assertTrue(updateHintBackendHandler.next());
        Iterator<Object> updateRowData = updateHintBackendHandler.getRowData().iterator();
        assertThat(updateRowData.next().toString(), is("user"));
        assertThat(updateRowData.next().toString(), is("100"));
        assertThat(updateRowData.next().toString(), is("200,300"));
        assertFalse(updateHintBackendHandler.next());
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.getSchema()).thenReturn(new ShardingSphereSchema(ImmutableMap.of("user", mock(TableMetaData.class))));
        when(metaData.isComplete()).thenReturn(true);
        return Collections.singletonMap("schema", metaData);
    }
    
    @Test(expected = UnsupportedShardingCTLTypeException.class)
    public void assertUnsupportedShardingCTLType() {
        clearThreadLocal();
        String sql = "sctl:hint xx=yy";
        new ShardingCTLHintBackendHandler(sql, backendConnection).execute();
    }
}
