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

package org.apache.shardingsphere.transaction.spring.boot.util;

import org.apache.shardingsphere.transaction.aspect.ShardingTransactionalAspect;
import org.apache.shardingsphere.transaction.spring.boot.fixture.ShardingTransactionalTestService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.engine.spi.SessionImplementor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionManagerMockUtil {
    
    /**
     * Init transaction manager mock.
     *
     * @param statement mock statement
     * @param jpaTransactionManager mock jpa transaction manager
     * @param dataSourceTransactionManager mock dataSource transaction manager
     * @throws SQLException SQL exception
     */
    public static void initTransactionManagerMock(
            final Statement statement, final JpaTransactionManager jpaTransactionManager, final DataSourceTransactionManager dataSourceTransactionManager) throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        EntityManagerFactory entityManagerFactory = mock(EntityManagerFactory.class);
        EntityManager entityManager = mock(EntityManager.class);
        SessionImplementor sessionImplementor = mock(SessionImplementor.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(sessionImplementor.connection()).thenReturn(connection);
        when(entityManager.unwrap(SessionImplementor.class)).thenReturn(sessionImplementor);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
        when(jpaTransactionManager.getEntityManagerFactory()).thenReturn(entityManagerFactory);
        when(dataSourceTransactionManager.getDataSource()).thenReturn(dataSource);
    }
    
    /**
     * Test change proxy transaction type to LOCAL with specified transaction manager.
     *
     * @param testService sharding transaction test service
     * @param aspect sharding transaction aspect
     * @param transactionManager specified transaction manager
     * @throws SQLException SQL exception
     */
    public static void testChangeProxyTransactionTypeToLOCAL(final ShardingTransactionalTestService testService, final ShardingTransactionalAspect aspect, final PlatformTransactionManager transactionManager) throws SQLException {
        aspect.setTransactionManager(transactionManager);
        aspect.setEnvironment(getProxyDataSource());
        testService.testChangeTransactionTypeToLOCAL();
    }
    
    /**
     * Test change proxy transaction type to XA with specified transaction manager.
     *
     * @param testService sharding transaction test service
     * @param aspect sharding transaction aspect
     * @param transactionManager specified transaction manager
     * @throws SQLException SQL exception
     */
    public static void testChangeProxyTransactionTypeToXA(final ShardingTransactionalTestService testService, final ShardingTransactionalAspect aspect, final PlatformTransactionManager transactionManager) throws SQLException {
        aspect.setTransactionManager(transactionManager);
        aspect.setEnvironment(getProxyDataSource());
        testService.testChangeTransactionTypeToXA();
    }
    
    /**
     * Test change proxy transaction type to BASE with specified transaction manager.
     *
     * @param testService sharding transaction test service
     * @param aspect sharding transaction aspect
     * @param transactionManager specified transaction manager
     * @throws SQLException SQL exception
     */
    public static void testChangeProxyTransactionTypeToBASE(final ShardingTransactionalTestService testService, final ShardingTransactionalAspect aspect, final PlatformTransactionManager transactionManager) throws SQLException {
        aspect.setTransactionManager(transactionManager);
        aspect.setEnvironment(getProxyDataSource());
        testService.testChangeTransactionTypeToBASE();
    }
    
    private static DataSource[] getProxyDataSource() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(databaseMetaData.getDatabaseProductVersion()).thenReturn("5.6.0-Sharding-Proxy x.x.x");
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(dataSource.getConnection()).thenReturn(connection);
        return new DataSource[] {dataSource};
    }
}
