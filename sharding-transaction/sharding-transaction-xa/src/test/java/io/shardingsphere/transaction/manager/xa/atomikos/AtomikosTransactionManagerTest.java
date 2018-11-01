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
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.rule.DataSourceParameter;
import lombok.SneakyThrows;
import org.hamcrest.CoreMatchers;
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
    
    @Test(expected = ShardingException.class)
    public void assertBeginWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).begin();
        new AtomikosTransactionManager().begin(new XATransactionEvent(TransactionOperationType.BEGIN));
    }
    
    @Test
    public void assertCommitWithoutException() throws Exception {
        new AtomikosTransactionManager().commit(new XATransactionEvent(TransactionOperationType.COMMIT));
        verify(userTransactionManager).commit();
    }
    
    @Test(expected = ShardingException.class)
    public void assertCommitWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).commit();
        new AtomikosTransactionManager().commit(new XATransactionEvent(TransactionOperationType.COMMIT));
    }
    
    @Test
    public void assertRollbackWithoutException() throws Exception {
        new AtomikosTransactionManager().rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
        verify(userTransactionManager).rollback();
    }
    
    @Test(expected = ShardingException.class)
    public void assertRollbackWithException() throws Exception {
        doThrow(SystemException.class).when(userTransactionManager).rollback();
        new AtomikosTransactionManager().rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
    }
    
    @Test
    public void assertGetStatusWithoutException() throws Exception {
        when(userTransactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        assertThat(new AtomikosTransactionManager().getStatus(), is(Status.STATUS_ACTIVE));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetStatusWithException() throws Exception {
        when(userTransactionManager.getStatus()).thenThrow(SystemException.class);
        new AtomikosTransactionManager().getStatus();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertWrapDataSourceForOtherDataSource() {
        XADataSource xaDataSource = mock(XADataSource.class);
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setMaximumPoolSize(10);
        new AtomikosTransactionManager().wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
    }
    
    @Test
    public void assertWrapDataSourceForMySQL() {
        XADataSource xaDataSource = new MysqlXADataSource();
        DataSourceParameter dataSourceParameter = new DataSourceParameter();
        dataSourceParameter.setUsername("root");
        dataSourceParameter.setPassword("root");
        dataSourceParameter.setUrl("db:url");
        dataSourceParameter.setMaximumPoolSize(10);
        DataSource actual = new AtomikosTransactionManager().wrapDataSource(xaDataSource, "ds_name", dataSourceParameter);
        assertThat(actual, CoreMatchers.<DataSource>instanceOf(AtomikosDataSourceBean.class));
    }
}
