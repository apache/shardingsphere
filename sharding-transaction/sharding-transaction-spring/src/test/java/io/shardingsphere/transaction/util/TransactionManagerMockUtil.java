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

package io.shardingsphere.transaction.util;

import io.shardingsphere.transaction.aspect.ShardingTransactionalAspect;
import io.shardingsphere.transaction.fixture.ShardingTransactionalTestService;
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
     */
    public static void initTransactionManagerMock(Statement statement, JpaTransactionManager jpaTransactionManager, DataSourceTransactionManager dataSourceTransactionManager) throws SQLException {
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
     * Test change proxy transaction type to LOCAL with specified transaction manager
     *
     * @param testService sharding transaction test service
     * @param aspect sharding transaction aspect
     * @param transactionManager specified transaction manager
     */
    public static void testChangeProxyTransactionTypeToLOCAL(ShardingTransactionalTestService testService, ShardingTransactionalAspect aspect, PlatformTransactionManager transactionManager) {
        aspect.setTransactionManager(transactionManager);
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
    }
    
    /**
     * Test change proxy transaction type to XA with specified transaction manager
     *
     * @param testService sharding transaction test service
     * @param aspect sharding transaction aspect
     * @param transactionManager specified transaction manager
     */
    public static void testChangeProxyTransactionTypeToXA(ShardingTransactionalTestService testService, ShardingTransactionalAspect aspect, PlatformTransactionManager transactionManager) {
        aspect.setTransactionManager(transactionManager);
        testService.testChangeTransactionTypeToXAWithEnvironment();
    }
    
    /**
     * Test change proxy transaction type to BASE with specified transaction manager
     *
     * @param testService sharding transaction test service
     * @param aspect sharding transaction aspect
     * @param transactionManager specified transaction manager
     */
    public static void testChangeProxyTransactionTypeToBASE(ShardingTransactionalTestService testService, ShardingTransactionalAspect aspect, PlatformTransactionManager transactionManager) {
        aspect.setTransactionManager(transactionManager);
        testService.testChangeTransactionTypeToBASEWithEnvironment();
    }
}
