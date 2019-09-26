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

package org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint;

import lombok.SneakyThrows;
import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.error.ErrorResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryData;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.InvalidShardingCTLFormatException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.exception.UnsupportedShardingCTLTypeException;
import org.apache.shardingsphere.shardingproxy.backend.text.sctl.hint.internal.HintManagerHolder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingCTLHintBackendHandlerTest {
    
    @Mock
    private BackendConnection backendConnection;
    
    @Before
    public void setUp() {
        when(backendConnection.isSupportHint()).thenReturn(true);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertNotSupportHint() {
        when(backendConnection.isSupportHint()).thenReturn(false);
        new ShardingCTLHintBackendHandler("", backendConnection).execute();
    }
    
    @Test
    public void assertInvalidShardingCTLFormat() {
        clearThreadLocal();
        String sql = "sctl:hint1 xx=yy";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(((ErrorResponse) shardingCTLHintBackendHandler.execute()).getCause(), instanceOf(InvalidShardingCTLFormatException.class));
    }
    
    private void clearThreadLocal() {
        HintManagerHolder.get().close();
        HintManagerHolder.remove();
    }
    
    @Test
    public void assertSetMasterOnly() {
        clearThreadLocal();
        String sql = "sctl:hint set master_only=true ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertTrue(HintManager.isMasterRouteOnly());
    }
    
    @Test
    public void assertSetDatabaseShardingValueTable() {
        clearThreadLocal();
        String sql = "sctl:hint set databaseShardingValue=100";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertEquals(HintManager.getDatabaseShardingValues().iterator().next(), "100");
    }
    
    @Test
    public void assertAddDatabaseShardingValue() {
        clearThreadLocal();
        String sql = "sctl:hint addDatabaseShardingValue user=100 ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertEquals(HintManager.getDatabaseShardingValues("user").iterator().next(), "100");
    }
    
    @Test
    public void assertAddTableShardingValue() {
        clearThreadLocal();
        String sql = "sctl:hint addTableShardingValue  user=100 ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertEquals(HintManager.getTableShardingValues("user").iterator().next(), "100");
    }
    
    @Test
    public void assertClear() {
        clearThreadLocal();
        String sql = "sctl:hint clear ";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(shardingCTLHintBackendHandler.execute(), instanceOf(UpdateResponse.class));
        assertThat(HintManager.getInstance(), instanceOf(HintManager.class));
        HintManager.clear();
    }
    
    @Test
    @SneakyThrows
    public void assertShowStatus() {
        clearThreadLocal();
        String sql = "sctl:hint show status";
        ShardingCTLHintBackendHandler defaultShardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        BackendResponse backendResponse = defaultShardingCTLHintBackendHandler.execute();
        assertThat(backendResponse, instanceOf(QueryResponse.class));
        assertEquals(((QueryResponse) backendResponse).getQueryHeaders().get(0).getColumnLabel(), "master_only");
        assertEquals(((QueryResponse) backendResponse).getQueryHeaders().get(1).getColumnLabel(), "sharding_type");
        assertTrue(defaultShardingCTLHintBackendHandler.next());
        QueryData defaultQueryData = defaultShardingCTLHintBackendHandler.getQueryData();
        assertEquals(defaultQueryData.getColumnTypes().get(0), Integer.valueOf(Types.CHAR));
        assertEquals(defaultQueryData.getColumnTypes().get(1), Integer.valueOf(Types.CHAR));
        assertEquals(defaultQueryData.getData().get(0), "false");
        assertEquals(defaultQueryData.getData().get(1), "databases_tables");
        assertFalse(defaultShardingCTLHintBackendHandler.next());
        String setMasterOnlySql = "sctl:hint set master_only=true";
        String setDatabaseOnlySql = "sctl:hint set DatabaseShardingValue=100";
        new ShardingCTLHintBackendHandler(setMasterOnlySql, backendConnection).execute();
        new ShardingCTLHintBackendHandler(setDatabaseOnlySql, backendConnection).execute();
        ShardingCTLHintBackendHandler updateShardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        updateShardingCTLHintBackendHandler.execute();
        assertTrue(updateShardingCTLHintBackendHandler.next());
        QueryData updateQueryData = updateShardingCTLHintBackendHandler.getQueryData();
        assertEquals(updateQueryData.getData().get(0), "true");
        assertEquals(updateQueryData.getData().get(1), "databases_only");
        assertFalse(updateShardingCTLHintBackendHandler.next());
    }
    
    @Test
    @SneakyThrows
    public void assertShowTableStatus() {
        clearThreadLocal();
        TableRule tableRule = mock(TableRule.class);
        ShardingRule shardingRule = mock(ShardingRule.class);
        LogicSchema logicSchema = mock(LogicSchema.class);
        when(tableRule.getLogicTable()).thenReturn("user");
        when(shardingRule.getTableRules()).thenReturn(Collections.singletonList(tableRule));
        when(logicSchema.getShardingRule()).thenReturn(shardingRule);
        when(backendConnection.getLogicSchema()).thenReturn(logicSchema);
        String sql = "sctl:hint show table status";
        ShardingCTLHintBackendHandler defaultShardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        BackendResponse backendResponse = defaultShardingCTLHintBackendHandler.execute();
        assertThat(backendResponse, instanceOf(QueryResponse.class));
        assertEquals(((QueryResponse) backendResponse).getQueryHeaders().get(0).getColumnLabel(), "table_name");
        assertEquals(((QueryResponse) backendResponse).getQueryHeaders().get(1).getColumnLabel(), "database_sharding_values");
        assertEquals(((QueryResponse) backendResponse).getQueryHeaders().get(2).getColumnLabel(), "table_sharding_values");
        assertTrue(defaultShardingCTLHintBackendHandler.next());
        QueryData defaultQueryData = defaultShardingCTLHintBackendHandler.getQueryData();
        assertEquals(defaultQueryData.getData().get(0), "user");
        assertEquals(defaultQueryData.getData().get(1), "");
        assertEquals(defaultQueryData.getData().get(2), "");
        assertFalse(defaultShardingCTLHintBackendHandler.next());
        String addDatabaseShardingValueSql = "sctl:hint addDatabaseshardingvalue user=100";
        String addTableShardingValueSql1 = "sctl:hint addTableshardingvalue user=200";
        String addTableShardingValueSql2 = "sctl:hint addTableshardingvalue user=300";
        new ShardingCTLHintBackendHandler(addDatabaseShardingValueSql, backendConnection).execute();
        new ShardingCTLHintBackendHandler(addTableShardingValueSql1, backendConnection).execute();
        new ShardingCTLHintBackendHandler(addTableShardingValueSql2, backendConnection).execute();
        ShardingCTLHintBackendHandler updateShardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        updateShardingCTLHintBackendHandler.execute();
        assertTrue(updateShardingCTLHintBackendHandler.next());
        QueryData updateQueryData = updateShardingCTLHintBackendHandler.getQueryData();
        assertEquals(updateQueryData.getData().get(0), "user");
        assertEquals(updateQueryData.getData().get(1), "100");
        assertEquals(updateQueryData.getData().get(2), "200,300");
        assertFalse(updateShardingCTLHintBackendHandler.next());
    }
    
    @Test
    public void assertUnsupportedShardingCTLType() {
        clearThreadLocal();
        String sql = "sctl:hint xx=yy";
        ShardingCTLHintBackendHandler shardingCTLHintBackendHandler = new ShardingCTLHintBackendHandler(sql, backendConnection);
        assertThat(((ErrorResponse) shardingCTLHintBackendHandler.execute()).getCause(), instanceOf(UnsupportedShardingCTLTypeException.class));
    }
}
