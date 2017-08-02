/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.jdbc.adapter;

import com.dangdang.ddframe.rdb.common.sql.base.AbstractShardingJDBCDatabaseAndTableTest;
import com.dangdang.ddframe.rdb.sharding.exception.ShardingJdbcException;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.connection.ShardingConnection;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class DataSourceAdapterTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    private Collection<ShardingDataSource> shardingDataSources;
    
    @Before
    public void init() throws SQLException {
        shardingDataSources = getShardingDataSources().values();
    }
    
    @Test
    public void assertUnwrapSuccess() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            assertThat(each.unwrap(Object.class), is((Object) each));
        }
    }
    
    @Test(expected = SQLException.class)
    public void assertUnwrapFailure() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            each.unwrap(String.class);
        }
    }
    
    @Test
    public void assertIsWrapperFor() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            assertTrue(each.isWrapperFor(Object.class));
        }
    }
    
    @Test
    public void assertIsNotWrapperFor() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            assertFalse(each.isWrapperFor(String.class));
        }
    }
    
    @Test
    public void assertRecordMethodInvocationSuccess() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            List<?> list = mock(List.class);
            when(list.isEmpty()).thenReturn(true);
            each.recordMethodInvocation(List.class, "isEmpty", new Class[]{}, new Object[]{});
            each.replayMethodsInvocation(list);
            verify(list).isEmpty();
        }
    }
    
    @Test(expected = ShardingJdbcException.class)
    public void assertRecordMethodInvocationFailure() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            each.recordMethodInvocation(String.class, "none", new Class[]{}, new Object[]{});
        }
    }
    
    @Test
    public void assertSetLogWriter() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            assertThat(each.getLogWriter(), instanceOf(PrintWriter.class));
            each.setLogWriter(null);
            assertNull(each.getLogWriter());
        }
    }
    
    @Test
    public void assertGetParentLogger() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            assertThat(each.getParentLogger().getName(), is(Logger.GLOBAL_LOGGER_NAME));
        }
    }
    
    @Test
    public void assertGetConnectionWithUsername() throws SQLException {
        for (ShardingDataSource each : shardingDataSources) {
            assertThat(each.getConnection("username", "password"), instanceOf(ShardingConnection.class));
        }
    }
}
