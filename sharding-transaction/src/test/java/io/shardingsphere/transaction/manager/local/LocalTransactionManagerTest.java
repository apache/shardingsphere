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

package io.shardingsphere.transaction.manager.local;

import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.transaction.event.local.LocalTransactionEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.transaction.Status;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class LocalTransactionManagerTest {
    
    @Mock
    private Connection connection1;
    
    @Mock
    private Connection connection2;
    
    @Test
    public void assertBeginWithoutException() throws SQLException {
        new LocalTransactionManager().begin(new LocalTransactionEvent(TransactionOperationType.BEGIN, Arrays.asList(connection1, connection2), false));
        verify(connection1).setAutoCommit(false);
        verify(connection2).setAutoCommit(false);
    }
    
    @Test(expected = SQLException.class)
    public void assertBeginWithException() throws SQLException {
        doThrow(new SQLException()).when(connection1).setAutoCommit(false);
        try {
            new LocalTransactionManager().begin(new LocalTransactionEvent(TransactionOperationType.BEGIN, Arrays.asList(connection1, connection2), false));
        } finally {
            verify(connection1).setAutoCommit(false);
            verify(connection2).setAutoCommit(false);
        }
    }
    
    @Test
    public void assertCommitWithoutException() throws SQLException {
        new LocalTransactionManager().commit(new LocalTransactionEvent(TransactionOperationType.COMMIT, Arrays.asList(connection1, connection2), false));
        verify(connection1).commit();
        verify(connection2).commit();
    }
    
    @Test(expected = SQLException.class)
    public void assertCommitWithException() throws SQLException {
        doThrow(new SQLException()).when(connection1).commit();
        try {
            new LocalTransactionManager().commit(new LocalTransactionEvent(TransactionOperationType.COMMIT, Arrays.asList(connection1, connection2), false));
        } finally {
            verify(connection1).commit();
            verify(connection2).commit();
        }
    }
    
    @Test
    public void assertRollbackWithoutException() throws SQLException {
        new LocalTransactionManager().rollback(new LocalTransactionEvent(TransactionOperationType.ROLLBACK, Arrays.asList(connection1, connection2), false));
        verify(connection1).rollback();
        verify(connection2).rollback();
    }
    
    @Test(expected = SQLException.class)
    public void assertRollbackWithException() throws SQLException {
        doThrow(new SQLException()).when(connection1).rollback();
        try {
            new LocalTransactionManager().rollback(new LocalTransactionEvent(TransactionOperationType.ROLLBACK, Arrays.asList(connection1, connection2), false));
        } finally {
            verify(connection1).rollback();
            verify(connection2).rollback();
        }
    }
    
    @Test
    public void assertGetStatus() {
        assertThat(new LocalTransactionManager().getStatus(), is(Status.STATUS_NO_TRANSACTION));
    }
}
