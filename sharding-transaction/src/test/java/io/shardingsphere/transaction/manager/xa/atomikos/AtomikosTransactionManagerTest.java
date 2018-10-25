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

package io.shardingsphere.transaction.manager.xa.atomikos;

import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.mysql.jdbc.jdbc2.optional.MysqlXADataSource;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.SneakyThrows;
import org.apache.tomcat.dbcp.dbcp2.managed.BasicManagedDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AtomikosTransactionManagerTest {
    
    @Mock
    private UserTransactionManager userTransactionManager;
    
    @Before
    @SneakyThrows
    public void setUp() {
        Field field = AtomikosTransactionManager.class.getDeclaredField("USER_TRANSACTION_MANAGER");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, userTransactionManager);
    }
    
    @Test
    public void assertBeginWithoutException() throws Exception {
        new AtomikosTransactionManager().begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        verify(userTransactionManager).begin();
    }
    
    @Test(expected = SQLException.class)
    public void assertBeginWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).begin();
        new AtomikosTransactionManager().begin(new XATransactionEvent(TransactionOperationType.BEGIN));
    }
    
    @Test
    public void assertCommitWithoutException() throws Exception {
        new AtomikosTransactionManager().commit(new XATransactionEvent(TransactionOperationType.COMMIT));
        verify(userTransactionManager).commit();
    }
    
    @Test(expected = SQLException.class)
    public void assertCommitWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).commit();
        new AtomikosTransactionManager().commit(new XATransactionEvent(TransactionOperationType.COMMIT));
    }
    
    @Test
    public void assertRollbackWithoutException() throws Exception {
        new AtomikosTransactionManager().rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
        verify(userTransactionManager).rollback();
    }
    
    @Test(expected = SQLException.class)
    public void assertRollbackWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).rollback();
        new AtomikosTransactionManager().rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertGetStatusWithoutException() throws Exception {
        when(userTransactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        assertThat(new AtomikosTransactionManager().getStatus(), is(Status.STATUS_ACTIVE));
    }
    
    @Test(expected = SQLException.class)
    public void assertGetStatusWithException() throws Exception {
        when(userTransactionManager.getStatus()).thenThrow(SystemException.class);
        new AtomikosTransactionManager().getStatus();
    }
    
    @Test
    public void assertWrapDataSourceForOtherDataSource() throws Exception {
        XADataSource xaDataSource = mock(XADataSource.class);
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setMaximumPoolSize(10);
        DataSource actual = new AtomikosTransactionManager().wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
        assertDataSourceBean(xaDataSource, actual);
        if (actual instanceof AtomikosDataSourceBean) {
            assertThat(((AtomikosDataSourceBean) actual).getXaProperties(), is(new Properties()));
        }
    }
    
    @Test
    public void assertWrapDataSourceForMySQL() throws Exception {
        XADataSource xaDataSource = new MysqlXADataSource();
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setUrl("db:url");
        dataSourceParameter.setMaximumPoolSize(10);
        DataSource actual = new AtomikosTransactionManager().wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
        assertDataSourceBean(xaDataSource, actual);
        if (actual instanceof AtomikosDataSourceBean) {
            AtomikosDataSourceBean atomikosDataSourceBean = (AtomikosDataSourceBean) actual;
            assertThat(atomikosDataSourceBean.getXaDataSource(), is(xaDataSource));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("user"), is("root"));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("password"), is("root"));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("URL"), is("db:url"));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("pinGlobalTxToPhysicalConnection"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("autoReconnect"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("useServerPrepStmts"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("cachePrepStmts"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("prepStmtCacheSize"), is("250"));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("prepStmtCacheSqlLimit"), is("2048"));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("useLocalSessionState"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("rewriteBatchedStatements"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("cacheResultSetMetadata"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("cacheServerConfiguration"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("elideSetAutoCommits"), is(Boolean.TRUE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("maintainTimeStats"), is(Boolean.FALSE.toString()));
            assertThat(atomikosDataSourceBean.getXaProperties().getProperty("netTimeoutForStreamingResults"), is("0"));
        }
    }
    
    private void assertDataSourceBean(final XADataSource xaDataSource, final DataSource actual) {
        if (actual instanceof AtomikosDataSourceBean) {
            AtomikosDataSourceBean atomikosDataSourceBean = (AtomikosDataSourceBean) actual;
            assertThat(atomikosDataSourceBean.getUniqueResourceName(), is("ds_name"));
            assertThat(atomikosDataSourceBean.getMaxPoolSize(), is(10));
            assertThat(atomikosDataSourceBean.getTestQuery(), is("SELECT 1"));
        }
        if (actual instanceof BasicManagedDataSource) {
            BasicManagedDataSource basicManagedDataSource = (BasicManagedDataSource) actual;
            assertThat(basicManagedDataSource.getMaxTotal(), is(10));
            assertThat(basicManagedDataSource.getXaDataSourceInstance(), is(xaDataSource));
        }
    }
}
