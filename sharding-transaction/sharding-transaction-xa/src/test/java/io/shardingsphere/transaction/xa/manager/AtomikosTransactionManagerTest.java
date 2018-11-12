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

package io.shardingsphere.transaction.xa.manager;

import com.atomikos.beans.PropertyException;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.SneakyThrows;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AtomikosTransactionManagerTest {
    
    @Mock
    private UserTransactionManager userTransactionManager;
    
    private AtomikosTransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
    
    private TransactionManager underlyingTransactionManager = atomikosTransactionManager.getUnderlyingTransactionManager();
    
    @Before
    @SneakyThrows
    public void setUp() {
        ReflectiveUtil.setProperty(atomikosTransactionManager, "underlyingTransactionManager", userTransactionManager);
    }
    
    @After
    public void teardown() {
        ReflectiveUtil.setProperty(atomikosTransactionManager, "underlyingTransactionManager", underlyingTransactionManager);
        atomikosTransactionManager.destroy();
    }
    
    @Test(expected = ShardingException.class)
    @SneakyThrows
    public void assertUnderlyingTransactionManagerInitFailed() {
        doThrow(SystemException.class).when(userTransactionManager).init();
        ReflectiveUtil.methodInvoke(atomikosTransactionManager, "init");
    }
    
    @Test
    public void assertBeginWithoutException() throws Exception {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        verify(userTransactionManager).begin();
    }
    
    @Test(expected = ShardingException.class)
    public void assertBeginWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).begin();
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
    }
    
    @Test
    public void assertCommitWithoutException() throws Exception {
        atomikosTransactionManager.commit(new XATransactionEvent(TransactionOperationType.COMMIT));
        verify(userTransactionManager).commit();
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).commit();
        atomikosTransactionManager.commit(new XATransactionEvent(TransactionOperationType.COMMIT));
    }
    
    @Test
    public void assertRollbackWithoutException() throws Exception {
        atomikosTransactionManager.rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
        verify(userTransactionManager).rollback();
    }
    
    @Test(expected = ShardingException.class)
    public void assertRollbackWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).rollback();
        atomikosTransactionManager.rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertGetStatusWithoutException() throws Exception {
        when(userTransactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        assertThat(atomikosTransactionManager.getStatus(), is(Status.STATUS_ACTIVE));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetStatusWithException() throws Exception {
        when(userTransactionManager.getStatus()).thenThrow(SystemException.class);
        atomikosTransactionManager.getStatus();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertWrapDataSourceForOtherDataSource() {
        XADataSource xaDataSource = mock(XADataSource.class);
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setMaximumPoolSize(10);
        atomikosTransactionManager.wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
    }
    
    @Test
    public void assertWrapDataSourceForH2() {
        XADataSource xaDataSource = new JdbcDataSource();
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setUrl("db:url");
        dataSourceParameter.setMaximumPoolSize(10);
        DataSource actual = atomikosTransactionManager.wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
        assertThat(actual, instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test(expected = ShardingException.class)
    @SneakyThrows
    public void assertWrapDataSourceFailed() {
        XATransactionDataSourceWrapper xaDataSourceWrapper = mock(XATransactionDataSourceWrapper.class);
        doThrow(PropertyException.class).when(xaDataSourceWrapper).wrap((XADataSource) any(), anyString(), (DataSourceParameter) any());
        ReflectiveUtil.setProperty(atomikosTransactionManager, "xaDataSourceWrapper", xaDataSourceWrapper);
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setUrl("db:url");
        dataSourceParameter.setMaximumPoolSize(10);
        XADataSource xaDataSource = new MysqlXADataSource();
        atomikosTransactionManager.wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
    }
}
