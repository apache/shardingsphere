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

package org.apache.shardingsphere.transaction.handler;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.hibernate.engine.spi.SessionImplementor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class JpaTransactionManagerHandlerTest {
    
    @Mock
    private JpaTransactionManager transactionManager;
    
    @Mock
    private Statement statement;
    
    @Mock
    private EntityManagerFactory entityManagerFactory;
    
    private JpaTransactionManagerHandler jpaTransactionManagerHandler;
    
    @Before
    public void setUp() throws SQLException {
        Connection connection = mock(Connection.class);
        EntityManager entityManager = mock(EntityManager.class);
        SessionImplementor sessionImplementor = mock(SessionImplementor.class);
        when(connection.createStatement()).thenReturn(statement);
        when(sessionImplementor.connection()).thenReturn(connection);
        when(entityManager.unwrap(SessionImplementor.class)).thenReturn(sessionImplementor);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(transactionManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        jpaTransactionManagerHandler = new JpaTransactionManagerHandler(transactionManager);
    }
    
    @Test
    public void assertSwitchTransactionTypeSuccess() throws SQLException {
        jpaTransactionManagerHandler.switchTransactionType(TransactionType.XA);
        verify(statement).execute(anyString());
        TransactionSynchronizationManager.unbindResourceIfPossible(entityManagerFactory);
    }
    
    @Test(expected = ShardingException.class)
    public void assertSwitchTransactionTypeFailExecute() throws SQLException {
        when(statement.execute(anyString())).thenThrow(new SQLException("Mock send switch transaction type SQL failed"));
        try {
            jpaTransactionManagerHandler.switchTransactionType(TransactionType.XA);
        } finally {
            TransactionSynchronizationManager.unbindResourceIfPossible(entityManagerFactory);
        }
    }
    
    @Test
    public void assertUnbindResource() {
        EntityManagerHolder holder = mock(EntityManagerHolder.class);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        when(holder.getEntityManager()).thenReturn(entityManager);
        TransactionSynchronizationManager.bindResource(entityManagerFactory, holder);
        jpaTransactionManagerHandler.unbindResource();
        assertNull(TransactionSynchronizationManager.getResource(entityManagerFactory));
    }
}
