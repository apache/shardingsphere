/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.backend.jdbc.connection;

import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.constant.transaction.TransactionType;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class BackendTransactionManagerTest {
    
    private BackendConnection backendConnection = new BackendConnection(TransactionType.LOCAL);
    
    private BackendTransactionManager backendTransactionManager = new BackendTransactionManager(backendConnection);
    
    @Test
    public void assertLocalTransactionCommit() throws SQLException {
        backendTransactionManager.doInTransaction(TransactionOperationType.BEGIN);
        assertThat(backendConnection.getStatus(), is(ConnectionStatus.TRANSACTION));
        assertThat(backendConnection.getMethodInvocations().size(), is(1));
        assertThat(backendConnection.getMethodInvocations().iterator().next().getArguments(), is(new Object[]{false}));
        backendTransactionManager.doInTransaction(TransactionOperationType.COMMIT);
    }
    
    private List<Connection> mockNewConnections(final int connectionSize) {
        List<Connection> result = new ArrayList<>();
        for (int i = 0; i < connectionSize; i++) {
            Connection connection = mock(Connection.class);
            result.add(connection);
        }
        return result;
    }
    
    @Test
    public void assertLocalTransactionRollback() {
    
    }
    
    @Test
    public void assertXATransactionCommit() {
    
    }
    
    @Test
    public void assertXATransactionRollback() {
    
    }
}
