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

package io.shardingsphere.transaction;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.transaction.TransactionTypeHolder;
import io.shardingsphere.transaction.aspect.ShardingTransactionalAspect;
import io.shardingsphere.transaction.fixture.ShardingTransactionalTestService;
import org.hibernate.engine.spi.SessionImplementor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = "classpath:shardingTransactionTest.xml")
public class ShardingTransactionalNameSpaceTest extends AbstractJUnit4SpringContextTests {
    
    @Autowired
    private ShardingTransactionalTestService testService;
    
    @Autowired
    private ShardingTransactionalAspect aspect;
    
    private final Statement statement = mock(Statement.class);
    
    private final JpaTransactionManager jpaTransactionManager = mock(JpaTransactionManager.class);
    
    private final DataSourceTransactionManager dataSourceTransactionManager = mock(DataSourceTransactionManager.class);
    
    @Before
    public void setUp() throws SQLException {
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
    
    @Test
    public void assertChangeTransactionTypeToXA() {
        testService.testChangeTransactionTypeToXA();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertChangeTransactionTypeToBASE() {
        testService.testChangeTransactionTypeToBASE();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertChangeTransactionTypeToLocal() {
        TransactionTypeHolder.set(TransactionType.XA);
        testService.testChangeTransactionTypeToLOCAL();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test
    public void assertChangeTransactionTypeInClass() {
        testService.testChangeTransactionTypeInClass();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.LOCAL));
    }
    
    @Test(expected = ShardingException.class)
    public void assertChangeTransactionTypeForProxyWithIllegalTransactionManager() {
        aspect.setTransactionManager(mock(PlatformTransactionManager.class));
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
    }
    
    @Test(expected = ShardingException.class)
    public void assertChangeTransactionTypeForProxyFailed() throws SQLException {
        when(statement.execute(anyString())).thenThrow(new SQLException("test switch exception"));
        aspect.setTransactionManager(dataSourceTransactionManager);
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
    }
    
    @Test
    public void assertChangeTransactionTypeToLOCALForProxy() throws SQLException {
        when(statement.execute(anyString())).thenReturn(true);
        aspect.setTransactionManager(jpaTransactionManager);
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
        aspect.setTransactionManager(dataSourceTransactionManager);
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
        verify(statement, times(2)).execute("SET TRANSACTION_TYPE=LOCAL");
    }
    
    @Test
    public void assertChangeTransactionTypeToXAForProxy() throws SQLException {
        when(statement.execute(anyString())).thenReturn(true);
        aspect.setTransactionManager(jpaTransactionManager);
        testService.testChangeTransactionTypeToXAWithEnvironment();
        aspect.setTransactionManager(dataSourceTransactionManager);
        testService.testChangeTransactionTypeToXAWithEnvironment();
        verify(statement, times(2)).execute("SET TRANSACTION_TYPE=XA");
    }
    
    @Test
    public void assertChangeTransactionTypeToBASEForProxy() throws SQLException {
        when(statement.execute(anyString())).thenReturn(true);
        aspect.setTransactionManager(jpaTransactionManager);
        testService.testChangeTransactionTypeToBASEWithEnvironment();
        aspect.setTransactionManager(dataSourceTransactionManager);
        testService.testChangeTransactionTypeToBASEWithEnvironment();
        verify(statement, times(2)).execute("SET TRANSACTION_TYPE=BASE");
    }
}
