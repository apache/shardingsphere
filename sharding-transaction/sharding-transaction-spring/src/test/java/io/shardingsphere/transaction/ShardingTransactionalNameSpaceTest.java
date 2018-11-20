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
import io.shardingsphere.transaction.fixture.FixedDataSource;
import io.shardingsphere.transaction.fixture.ShardingTransactionalTestService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(locations = "classpath:shardingTransactionTest.xml")
public class ShardingTransactionalNameSpaceTest extends AbstractJUnit4SpringContextTests {
    
    @Autowired
    private ShardingTransactionalTestService testService;
    
    @Autowired
    private ShardingTransactionalAspect aspect;
    
    @Before
    public void setUp() {
        TransactionTypeHolder.set(TransactionType.LOCAL);
    }
    
    @Test
    public void assertChangeTransactionTypeToXA() {
        testService.testChangeTransactionTypeToXA();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
    }
    
    @Test
    public void assertChangeTransactionTypeToBASE() {
        testService.testChangeTransactionTypeToBASE();
        assertThat(TransactionTypeHolder.get(), is(TransactionType.BASE));
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
        assertThat(TransactionTypeHolder.get(), is(TransactionType.XA));
    }
    
    
    @Test
    public void assertInjectedDataSource() throws NoSuchFieldException, IllegalAccessException {
        Field dataSourceField = ShardingTransactionalAspect.class.getDeclaredField("dataSource");
        dataSourceField.setAccessible(true);
        DataSource injected = (DataSource) dataSourceField.get(aspect);
        assertThat(injected, instanceOf(FixedDataSource.class));
    }
    
    @Test(expected = ShardingException.class)
    public void assertChangeTransactionTypeForProxyWithNullDataSource() {
        aspect.setDataSource(new DataSource[] {null});
        
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
    }
    
    @Test(expected = ShardingException.class)
    public void assertChangeTransactionTypeForProxyFailed() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.execute(anyString())).thenThrow(new SQLException("test switch exception"));
        aspect.setDataSource(new DataSource[] {dataSource});
        
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
    }
    
    @Test
    public void assertChangeTransactionTypeToLOCALForProxy() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        aspect.setDataSource(new DataSource[] {dataSource});
        
        testService.testChangeTransactionTypeToLOCALWithEnvironment();
        verify(statement).execute("SET TRANSACTION_TYPE=LOCAL");
    }
    
    @Test
    public void assertChangeTransactionTypeToXAForProxy() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        aspect.setDataSource(new DataSource[] {dataSource});
        
        testService.testChangeTransactionTypeToXAWithEnvironment();
        verify(statement).execute("SET TRANSACTION_TYPE=XA");
    }
    
    @Test
    public void assertChangeTransactionTypeToBASEForProxy() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        aspect.setDataSource(new DataSource[] {dataSource});
        
        testService.testChangeTransactionTypeToBASEWithEnvironment();
        verify(statement).execute("SET TRANSACTION_TYPE=BASE");
    }
}
