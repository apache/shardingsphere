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

package org.apache.shardingsphere.driver.jdbc.adapter;

import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ConnectionAdapterTest {
    
    @Test
    public void assertClose() throws SQLException {
        ShardingSphereConnection actual = mockShardingSphereConnection(mock(Connection.class));
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(getCachedConnections(actual).isEmpty());
    }
    
    @Test
    public void assertCloseShouldNotClearTransactionType() throws SQLException {
        ShardingSphereConnection actual = mockShardingSphereConnection(mock(Connection.class));
        TransactionTypeHolder.set(TransactionType.XA);
        actual.close();
        assertTrue(actual.isClosed());
        assertTrue(getCachedConnections(actual).isEmpty());
        assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
    }
    
    @Test
    public void assertSetReadOnly() throws SQLException {
        Connection connection = mock(Connection.class);
        ShardingSphereConnection actual = mockShardingSphereConnection(connection);
        assertFalse(actual.isReadOnly());
        actual.setReadOnly(true);
        assertTrue(actual.isReadOnly());
        verify(connection).setReadOnly(true);
    }
    
    @Test
    public void assertGetTransactionIsolationWithoutCachedConnections() throws SQLException {
        assertThat(mockShardingSphereConnection().getTransactionIsolation(), is(Connection.TRANSACTION_READ_UNCOMMITTED));
    }
    
    @Test
    public void assertSetTransactionIsolation() throws SQLException {
        Connection connection = mock(Connection.class);
        ShardingSphereConnection actual = mockShardingSphereConnection(connection);
        actual.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        verify(connection).setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
    }
    
    @Test
    public void assertGetWarnings() {
        assertNull(mockShardingSphereConnection().getWarnings());
    }
    
    @Test
    public void assertClearWarnings() {
        mockShardingSphereConnection().clearWarnings();
    }
    
    @Test
    public void assertGetHoldability() {
        assertThat(mockShardingSphereConnection().getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
    }
    
    @Test
    public void assertSetHoldability() {
        mockShardingSphereConnection().setHoldability(ResultSet.CONCUR_READ_ONLY);
        assertThat(mockShardingSphereConnection().getHoldability(), is(ResultSet.CLOSE_CURSORS_AT_COMMIT));
    }
    
    @Test
    public void assertGetCatalog() {
        assertNull(mockShardingSphereConnection().getCatalog());
    }
    
    @Test
    public void assertSetCatalog() {
        ShardingSphereConnection actual = mockShardingSphereConnection();
        actual.setCatalog("");
        assertNull(actual.getCatalog());
    }
    
    @Test
    public void assertGetSchema() {
        assertNull(mockShardingSphereConnection().getSchema());
    }
    
    @Test
    public void assertSetSchema() {
        ShardingSphereConnection actual = mockShardingSphereConnection();
        actual.setSchema("");
        assertNull(actual.getSchema());
    }
    
    private ShardingSphereConnection mockShardingSphereConnection(final Connection... connections) {
        ShardingSphereConnection result = new ShardingSphereConnection(
                Collections.emptyMap(), mock(MetaDataContexts.class), mock(TransactionContexts.class, RETURNS_DEEP_STUBS), TransactionType.LOCAL);
        result.getCachedConnections().putAll("", Arrays.asList(connections));
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    private Multimap<String, Connection> getCachedConnections(final AbstractConnectionAdapter connectionAdapter) {
        Field field = AbstractConnectionAdapter.class.getDeclaredField("cachedConnections");
        field.setAccessible(true);
        return (Multimap<String, Connection>) field.get(connectionAdapter);
    }
}
