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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction;

import org.firebirdsql.gds.TransactionParameterBuffer;
import org.firebirdsql.jaybird.xca.FBLocalTransaction;
import org.firebirdsql.jaybird.xca.FBManagedConnection;
import org.firebirdsql.jdbc.FBConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdRawTransactionOptionsHandlerTest {
    
    @Mock
    private Connection connection;
    
    @Mock
    private FBConnection firebird;
    
    @Mock
    private FBManagedConnection managedConnection;
    
    @Mock
    private FBLocalTransaction transaction;
    
    @Mock
    private TransactionParameterBuffer original;
    
    @Mock
    private TransactionParameterBuffer saved;
    
    @Test
    void assertInitialize() throws SQLException {
        setUpConnection();
        byte[] raw = {3, 8, 7};
        FirebirdRawTransactionOptionsHandler handler = new FirebirdRawTransactionOptionsHandler(raw);
        handler.initialize("foo_db", "foo_ds", connection);
        verify(connection).setAutoCommit(false);
        ArgumentCaptor<TransactionParameterBuffer> actual = ArgumentCaptor.forClass(TransactionParameterBuffer.class);
        verify(firebird).setTransactionParameters(actual.capture());
        assertThat(actual.getValue().toBytesWithType(), is(raw));
        verify(transaction).begin();
    }
    
    private void setUpConnection() throws SQLException {
        when(connection.unwrap(FBConnection.class)).thenReturn(firebird);
        when(firebird.getManagedConnection()).thenReturn(managedConnection);
        when(managedConnection.getTransactionParameters()).thenReturn(original);
        when(original.deepCopy()).thenReturn(saved);
        when(firebird.getLocalTransaction()).thenReturn(transaction);
    }
    
    @Test
    void assertReleaseRestoresParameters() throws SQLException {
        setUpConnection();
        when(transaction.inTransaction()).thenReturn(true);
        FirebirdRawTransactionOptionsHandler handler = new FirebirdRawTransactionOptionsHandler(new byte[]{3, 8});
        handler.initialize("foo_db", "foo_ds", connection);
        handler.release(connection);
        verify(connection).rollback();
        verify(firebird).setTransactionParameters(saved);
    }
    
    @Test
    void assertReleaseWithoutInitializedConnection() throws SQLException {
        Connection actual = mock(Connection.class);
        new FirebirdRawTransactionOptionsHandler(new byte[]{3, 8}).release(actual);
        verifyNoInteractions(actual);
    }
}
