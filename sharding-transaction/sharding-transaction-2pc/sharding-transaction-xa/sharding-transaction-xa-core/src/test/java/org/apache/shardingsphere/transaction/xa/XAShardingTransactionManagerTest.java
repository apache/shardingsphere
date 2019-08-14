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

package org.apache.shardingsphere.transaction.xa;

import com.alibaba.druid.pool.xa.DruidXADataSource;
import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import org.apache.shardingsphere.transaction.xa.jta.connection.SingleXAConnection;
import org.apache.shardingsphere.transaction.xa.jta.datasource.SingleXADataSource;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;
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
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    
    private final XAShardingTransactionManager xaShardingTransactionManager = new XAShardingTransactionManager();
    
    @Mock
    private XATransactionManager xaTransactionManager;
    
    @Mock
    private TransactionManager transactionManager;
    
    @Before
    public void setUp() {
        when(xaTransactionManager.getTransactionManager()).thenReturn(transactionManager);
        ReflectiveUtil.setProperty(xaShardingTransactionManager, "xaTransactionManager", xaTransactionManager);
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(xaShardingTransactionManager.getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertRegisterXATransactionalDataSources() {
        Collection<ResourceDataSource> resourceDataSources = createResourceDataSources(DruidXADataSource.class, DatabaseTypes.getActualDatabaseType("MySQL"));
        xaShardingTransactionManager.init(DatabaseTypes.getActualDatabaseType("MySQL"), resourceDataSources);
        for (ResourceDataSource each : resourceDataSources) {
            verify(xaTransactionManager).registerRecoveryResource(each.getUniqueResourceName(), (XADataSource) each.getDataSource());
        }
    }
    
    @Test
    public void assertRegisterAtomikosDataSourceBeans() {
        xaShardingTransactionManager.init(DatabaseTypes.getActualDatabaseType("MySQL"), createAtomikosDataSourceBeanResource());
        verify(xaTransactionManager, times(0)).registerRecoveryResource(anyString(), any(XADataSource.class));
    }
    
    @Test
    public void assertRegisterNoneXATransactionalDAtaSources() {
        Collection<ResourceDataSource> resourceDataSources = createResourceDataSources(HikariDataSource.class, DatabaseTypes.getActualDatabaseType("MySQL"));
        xaShardingTransactionManager.init(DatabaseTypes.getActualDatabaseType("MySQL"), resourceDataSources);
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
    public void assertGetConnection() throws SQLException {
        setCachedSingleXADataSourceMap("ds1");
        Connection actual = xaShardingTransactionManager.getConnection("ds1");
        assertThat(actual, instanceOf(Connection.class));
        verify(xaTransactionManager).enlistResource(any(SingleXAResource.class));
    }
    
    @Test
    public void assertGetConnectionWithoutEnlist() throws SQLException {
        setCachedSingleXADataSourceMap("ds1");
        Connection actual = xaShardingTransactionManager.getConnection("ds1");
        assertThat(actual, instanceOf(Connection.class));
        xaShardingTransactionManager.getConnection("ds1");
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
        Field field = xaShardingTransactionManager.getClass().getDeclaredField("singleXADataSourceMap");
        field.setAccessible(true);
        return (Map<String, SingleXADataSource>) field.get(xaShardingTransactionManager);
    }
    
    @SneakyThrows
    private void setCachedSingleXADataSourceMap(final String datasourceName) {
        Field field = xaShardingTransactionManager.getClass().getDeclaredField("singleXADataSourceMap");
        field.setAccessible(true);
        field.set(xaShardingTransactionManager, createMockSingleXADataSourceMap(datasourceName));
    }
    
    private Map<String, SingleXADataSource> createMockSingleXADataSourceMap(final String datasourceName) throws SQLException {
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
    
    private Collection<ResourceDataSource> createResourceDataSources(final Class<? extends DataSource> dataSourceClass, final DatabaseType databaseType) {
        List<ResourceDataSource> result = new LinkedList<>();
        result.add(new ResourceDataSource("ds1", DataSourceUtils.build(dataSourceClass, databaseType, "demo_ds_1")));
        result.add(new ResourceDataSource("ds2", DataSourceUtils.build(dataSourceClass, databaseType, "demo_ds_2")));
        return result;
    }
    
    private Collection<ResourceDataSource> createAtomikosDataSourceBeanResource() {
        List<ResourceDataSource> result = new LinkedList<>();
        result.add(new ResourceDataSource("ds1", new AtomikosDataSourceBean()));
        result.add(new ResourceDataSource("ds2", new AtomikosDataSourceBean()));
        return result;
    }
}
