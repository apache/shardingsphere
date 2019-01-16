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

package io.shardingsphere.transaction.xa;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.core.TransactionType;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import io.shardingsphere.transaction.xa.jta.connection.SingleXAConnection;
import io.shardingsphere.transaction.xa.jta.datasource.SingleXADataSource;
import io.shardingsphere.transaction.xa.spi.SingleXAResource;
import io.shardingsphere.transaction.xa.spi.XATransactionManager;
import lombok.SneakyThrows;
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
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class XAShardingTransactionManagerTest {
    
    private XAShardingTransactionManager xaShardingTransactionManager = new XAShardingTransactionManager();
    
    @Mock
    private XATransactionManager xaTransactionManager;
    
    @Mock
    private TransactionManager transactionManager;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        when(xaTransactionManager.getTransactionManager()).thenReturn(transactionManager);
        setXATransactionManager();
    }
    
    private void setXATransactionManager() throws ReflectiveOperationException {
        Field field = XAShardingTransactionManager.class.getDeclaredField("xaTransactionManager");
        field.setAccessible(true);
        field.set(xaShardingTransactionManager, xaTransactionManager);
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(xaShardingTransactionManager.getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertRegisterXATransactionalDataSources() {
        Map<String, DataSource> dataSourceMap = createDataSourceMap(DruidXADataSource.class, DatabaseType.MySQL);
        xaShardingTransactionManager.init(DatabaseType.MySQL, dataSourceMap);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            verify(xaTransactionManager).registerRecoveryResource(entry.getKey(), (XADataSource) entry.getValue());
        }
    }
    
    @Test
    public void assertRegisterAtomikosDataSourceBeans() {
        Map<String, DataSource> dataSourceMap = createAtomikosDataSourceBeanMap();
        xaShardingTransactionManager.init(DatabaseType.MySQL, dataSourceMap);
        verify(xaTransactionManager, times(0)).registerRecoveryResource(anyString(), any(XADataSource.class));
    }
    
    @Test
    public void assertRegisterNoneXATransactionalDAtaSources() {
        Map<String, DataSource> dataSourceMap = createDataSourceMap(HikariDataSource.class, DatabaseType.MySQL);
        xaShardingTransactionManager.init(DatabaseType.MySQL, dataSourceMap);
        Map<String, SingleXADataSource> cachedXADatasourceMap = getCachedSingleXADataSourceMap();
        assertThat(cachedXADatasourceMap.size(), is(2));
    }
    
    @Test
    public void assertIsInTransaction() throws SystemException {
        when(transactionManager.getStatus()).thenReturn(Status.STATUS_ACTIVE);
        assertTrue(xaShardingTransactionManager.isInTransaction());
    }
    
    @Test
    public void assertIsNotInTransaction() throws SystemException {
        when(transactionManager.getStatus()).thenReturn(Status.STATUS_NO_TRANSACTION);
        assertFalse(xaShardingTransactionManager.isInTransaction());
    }
    
    @Test
    public void assertGetConnection() {
        setCachedSingleXADataSourceMap("ds1");
        Connection actual = xaShardingTransactionManager.getConnection("ds1");
        assertThat(actual, instanceOf(Connection.class));
        verify(xaTransactionManager).enlistResource(any(SingleXAResource.class));
    }
    
    @Test
    public void assertClose() throws Exception {
        setCachedSingleXADataSourceMap("ds1");
        xaShardingTransactionManager.close();
        Map<String, SingleXADataSource> cachedSingleXADataSourceMap = getCachedSingleXADataSourceMap();
        verify(xaTransactionManager).removeRecoveryResource(anyString(), any(XADataSource.class));
        assertThat(cachedSingleXADataSourceMap.size(), is(0));
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Map<String, SingleXADataSource> getCachedSingleXADataSourceMap() {
        Field field = xaShardingTransactionManager.getClass().getDeclaredField("cachedSingleXADataSourceMap");
        field.setAccessible(true);
        return (Map<String, SingleXADataSource>) field.get(xaShardingTransactionManager);
    }
    
    @SneakyThrows
    private void setCachedSingleXADataSourceMap(final String datasourceName) {
        Field field = xaShardingTransactionManager.getClass().getDeclaredField("cachedSingleXADataSourceMap");
        field.setAccessible(true);
        field.set(xaShardingTransactionManager, createMockSingleXADataSourceMap(datasourceName));
    }
    
    @SneakyThrows
    private Map<String, SingleXADataSource> createMockSingleXADataSourceMap(final String datasourceName) {
        SingleXADataSource singleXADataSource = mock(SingleXADataSource.class);
        SingleXAConnection singleXAConnection = mock(SingleXAConnection.class);
        XADataSource xaDataSource = mock(XADataSource.class);
        SingleXAResource singleXAResource = mock(SingleXAResource.class);
        Connection connection = mock(Connection.class);
        when(singleXAConnection.getConnection()).thenReturn(connection);
        when(singleXAConnection.getXAResource()).thenReturn(singleXAResource);
        when(singleXADataSource.getXAConnection()).thenReturn(singleXAConnection);
        when(singleXADataSource.getResourceName()).thenReturn(datasourceName);
        when(singleXADataSource.getXaDataSource()).thenReturn(xaDataSource);
        Map<String, SingleXADataSource> result = new HashMap<>();
        result.put(datasourceName, singleXADataSource);
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap(final Class<? extends DataSource> dataSourceClass, final DatabaseType databaseType) {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds1", DataSourceUtils.build(dataSourceClass, databaseType, "demo_ds_1"));
        result.put("ds2", DataSourceUtils.build(dataSourceClass, databaseType, "demo_ds_2"));
        return result;
    }
    
    private Map<String, DataSource> createAtomikosDataSourceBeanMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds1", new AtomikosDataSourceBean());
        result.put("ds2", new AtomikosDataSourceBean());
        return result;
    }
}
