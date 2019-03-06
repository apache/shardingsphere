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

package org.apache.shardingsphere.transaction;

import org.apache.shardingsphere.transaction.aspect.ShardingTransactionalAspect;
import org.apache.shardingsphere.transaction.fixture.ShardingTransactionalTestService;
import org.apache.shardingsphere.transaction.util.TransactionManagerMockUtil;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
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
        TransactionManagerMockUtil.initTransactionManagerMock(statement, jpaTransactionManager, dataSourceTransactionManager);
    }
    
    @After
    public void tearDown() {
        aspect.setEnvironment(new DataSource[]{});
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
    public void assertChangeTransactionTypeForProxyWithIllegalTransactionManager() throws SQLException {
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToLOCAL(testService, aspect, mock(PlatformTransactionManager.class));
    }
    
    @Test(expected = ShardingException.class)
    public void assertChangeTransactionTypeForProxyFailed() throws SQLException {
        when(statement.execute(anyString())).thenThrow(new SQLException("test switch exception"));
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToLOCAL(testService, aspect, dataSourceTransactionManager);
    }
    
    @Test
    public void assertChangeTransactionTypeToLOCALForProxy() throws SQLException {
        when(statement.execute(anyString())).thenReturn(true);
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToLOCAL(testService, aspect, dataSourceTransactionManager);
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToLOCAL(testService, aspect, jpaTransactionManager);
        verify(statement, times(2)).execute("SCTL:SET TRANSACTION_TYPE=LOCAL");
    }
    
    @Test
    public void assertChangeTransactionTypeToXAForProxy() throws SQLException {
        when(statement.execute(anyString())).thenReturn(true);
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToXA(testService, aspect, dataSourceTransactionManager);
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToXA(testService, aspect, jpaTransactionManager);
        verify(statement, times(2)).execute("SCTL:SET TRANSACTION_TYPE=XA");
    }
    
    @Test
    public void assertChangeTransactionTypeToBASEForProxy() throws SQLException {
        when(statement.execute(anyString())).thenReturn(true);
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToBASE(testService, aspect, dataSourceTransactionManager);
        TransactionManagerMockUtil.testChangeProxyTransactionTypeToBASE(testService, aspect, jpaTransactionManager);
        verify(statement, times(2)).execute("SCTL:SET TRANSACTION_TYPE=BASE");
    }
}
