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

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.SneakyThrows;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AtomikosTransactionManagerTest {
    
    private AtomikosTransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
    
    @Mock
    private UserTransactionManager userTransactionManager;
    
    @Mock
    private UserTransactionService userTransactionService;
    
    @Mock
    private XADataSource xaDataSource;
    
    @Before
    @SneakyThrows
    public void setUp() {
        ReflectiveUtil.setProperty(atomikosTransactionManager, "underlyingTransactionManager", userTransactionManager);
        ReflectiveUtil.setProperty(atomikosTransactionManager, "userTransactionService", userTransactionService);
    }
    
    @Test
    public void assertStartup() {
        atomikosTransactionManager.startup();
        verify(userTransactionService).init();
    }
    
    @Test
    public void assertShutdown() {
        atomikosTransactionManager.destroy();
        verify(userTransactionService).shutdown(true);
    }
    
    @Test
    public void assertBeginWithoutException() throws Exception {
        atomikosTransactionManager.begin();
        verify(userTransactionManager).begin();
    }
    
    @Test(expected = ShardingException.class)
    public void assertBeginWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).begin();
        atomikosTransactionManager.begin();
    }
    
    @Test
    public void assertCommitWithoutException() throws Exception {
        atomikosTransactionManager.commit();
        verify(userTransactionManager).commit();
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).commit();
        atomikosTransactionManager.commit();
    }
    
    @Test
    public void assertRollbackWithoutException() throws Exception {
        atomikosTransactionManager.rollback();
        verify(userTransactionManager).rollback();
    }
    
    @Test(expected = ShardingException.class)
    public void assertRollbackWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).rollback();
        atomikosTransactionManager.rollback();
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
    
    @Test(expected = ShardingException.class)
    public void assertWrapDataSourceForOtherDataSource() {
        XADataSource xaDataSource = mock(XADataSource.class);
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setMaxPoolSize(10);
        atomikosTransactionManager.wrapDataSource(DatabaseType.MySQL, xaDataSource, "ds_name", dataSourceParameter);
    }
    
    @Test
    public void assertWrapDataSourceForH2() {
        XADataSource xaDataSource = new JdbcDataSource();
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setUrl("db:url");
        dataSourceParameter.setMaxPoolSize(10);
        DataSource actual = atomikosTransactionManager.wrapDataSource(DatabaseType.H2, xaDataSource, "ds_name", dataSourceParameter);
        assertThat(actual, instanceOf(AtomikosDataSourceBean.class));
    }
    
    @Test(expected = ShardingException.class)
    public void assertWrapDataSourceFailed() {
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setUrl("db:url");
        dataSourceParameter.setMaxPoolSize(0);
        XADataSource xaDataSource = new MysqlXADataSource();
        atomikosTransactionManager.wrapDataSource(DatabaseType.MySQL, xaDataSource, "ds_name", dataSourceParameter);
    }
    
    @Test
    public void assertRegisterRecoveryResource() {
        atomikosTransactionManager.registerRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).registerResource(any(AtomikosXARecoverableResource.class));
    }
    
    @Test
    public void assertRemoveRecoveryResource() {
        atomikosTransactionManager.removeRecoveryResource("ds1", xaDataSource);
        verify(userTransactionService).removeResource(any(AtomikosXARecoverableResource.class));
    }
}
