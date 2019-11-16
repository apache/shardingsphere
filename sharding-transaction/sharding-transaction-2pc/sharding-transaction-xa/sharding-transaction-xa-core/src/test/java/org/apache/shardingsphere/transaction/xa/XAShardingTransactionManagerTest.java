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

import com.atomikos.jdbc.AtomikosDataSourceBean;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource;
import org.apache.shardingsphere.transaction.xa.manager.XATransactionManagerLoader;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.XADataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class XAShardingTransactionManagerTest {
    
    private final XAShardingTransactionManager xaShardingTransactionManager = new XAShardingTransactionManager();
    
    @Spy
    private final XATransactionManager xaTransactionManager = XATransactionManagerLoader.getInstance().getTransactionManager();
    
    @Before
    public void setUp() {
        ReflectiveUtil.setProperty(xaShardingTransactionManager, "xaTransactionManager", xaTransactionManager);
        Collection<ResourceDataSource> resourceDataSources = createResourceDataSources(DatabaseTypes.getActualDatabaseType("H2"));
        xaShardingTransactionManager.init(DatabaseTypes.getActualDatabaseType("H2"), resourceDataSources);
        verify(xaTransactionManager).init();
    }
    
    @After
    public void tearDown() throws Exception {
        xaShardingTransactionManager.close();
    }
    
    @Test
    public void assertGetTransactionType() {
        assertThat(xaShardingTransactionManager.getTransactionType(), is(TransactionType.XA));
    }
    
    @Test
    public void assertRegisterXADataSource() {
        Map<String, XATransactionDataSource> cachedXADatasourceMap = getCachedSingleXADataSourceMap();
        assertThat(cachedXADatasourceMap.size(), is(2));
    }
    
    @Test
    public void assertIsInTransaction() {
        assertFalse(xaShardingTransactionManager.isInTransaction());
        xaShardingTransactionManager.begin();
        assertTrue(xaShardingTransactionManager.isInTransaction());
        xaShardingTransactionManager.commit();
    }
    
    @Test
    @Ignore
    public void assertGetConnection() throws SQLException {
        xaShardingTransactionManager.begin();
        Connection actual1 = xaShardingTransactionManager.getConnection("ds1");
        Connection actual2 = xaShardingTransactionManager.getConnection("ds1");
        Connection actual3 = xaShardingTransactionManager.getConnection("ds3");
        assertThat(actual1, instanceOf(Connection.class));
        assertThat(actual2, instanceOf(Connection.class));
        assertThat(actual3, instanceOf(Connection.class));
        verify(xaTransactionManager).enlistResource(any(SingleXAResource.class));
        xaShardingTransactionManager.commit();
    }
    
    @Test
    public void assertGetConnectionWithoutEnlist() throws SQLException {
        xaShardingTransactionManager.begin();
        Connection actual = xaShardingTransactionManager.getConnection("ds1");
        assertThat(actual, instanceOf(Connection.class));
        xaShardingTransactionManager.getConnection("ds1");
        assertThat(actual, instanceOf(Connection.class));
        verify(xaTransactionManager).enlistResource(any(SingleXAResource.class));
        xaShardingTransactionManager.commit();
    }
    
    @Test
    public void assertClose() throws Exception {
        xaShardingTransactionManager.close();
        Map<String, XATransactionDataSource> cachedSingleXADataSourceMap = getCachedSingleXADataSourceMap();
        verify(xaTransactionManager, times(2)).removeRecoveryResource(anyString(), any(XADataSource.class));
        assertThat(cachedSingleXADataSourceMap.size(), is(0));
    }
    
    @Test
    public void assertCommit() {
        xaShardingTransactionManager.begin();
        assertTrue(xaShardingTransactionManager.isInTransaction());
        xaShardingTransactionManager.commit();
        assertFalse(xaShardingTransactionManager.isInTransaction());
    }
    
    @Test
    public void assertRollback() {
        xaShardingTransactionManager.begin();
        assertTrue(xaShardingTransactionManager.isInTransaction());
        xaShardingTransactionManager.rollback();
        assertFalse(xaShardingTransactionManager.isInTransaction());
    }
    
    @SneakyThrows
    @SuppressWarnings("unchecked")
    private Map<String, XATransactionDataSource> getCachedSingleXADataSourceMap() {
        Field field = xaShardingTransactionManager.getClass().getDeclaredField("cachedDataSources");
        field.setAccessible(true);
        return (Map<String, XATransactionDataSource>) field.get(xaShardingTransactionManager);
    }
    
    private Collection<ResourceDataSource> createResourceDataSources(final DatabaseType databaseType) {
        List<ResourceDataSource> result = new LinkedList<>();
        result.add(new ResourceDataSource("ds1", DataSourceUtils.build(HikariDataSource.class, databaseType, "demo_ds_1")));
        result.add(new ResourceDataSource("ds2", DataSourceUtils.build(HikariDataSource.class, databaseType, "demo_ds_2")));
        result.add(new ResourceDataSource("ds3", DataSourceUtils.build(AtomikosDataSourceBean.class, databaseType, "demo_ds_3")));
        return result;
    }
}
