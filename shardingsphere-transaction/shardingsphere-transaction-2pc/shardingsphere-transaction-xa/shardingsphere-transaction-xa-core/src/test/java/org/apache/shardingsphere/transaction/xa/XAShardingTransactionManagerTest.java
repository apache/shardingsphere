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
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.transaction.Transaction;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionManagerType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XATransactionDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class XAShardingTransactionManagerTest {
    
    private final XAShardingTransactionManager xaShardingTransactionManager = new XAShardingTransactionManager();
    
    @Before
    public void setUp() {
        Collection<ResourceDataSource> resourceDataSources = createResourceDataSources(DatabaseTypeRegistry.getActualDatabaseType("H2"));
        xaShardingTransactionManager.init(DatabaseTypeRegistry.getActualDatabaseType("H2"), resourceDataSources, TransactionManagerType.ATOMIKOS.getType());
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
        Map<String, XATransactionDataSource> cachedXADatasourceMap = getCachedDataSources();
        assertThat(cachedXADatasourceMap.size(), is(3));
    }
    
    @Test
    public void assertIsInTransaction() {
        assertFalse(xaShardingTransactionManager.isInTransaction());
        xaShardingTransactionManager.begin();
        assertTrue(xaShardingTransactionManager.isInTransaction());
        xaShardingTransactionManager.commit();
    }
    
    @Test
    public void assertGetConnection() throws SQLException {
        xaShardingTransactionManager.begin();
        Connection actual1 = xaShardingTransactionManager.getConnection("ds1");
        Connection actual2 = xaShardingTransactionManager.getConnection("ds2");
        Connection actual3 = xaShardingTransactionManager.getConnection("ds3");
        assertThat(actual1, instanceOf(Connection.class));
        assertThat(actual2, instanceOf(Connection.class));
        assertThat(actual3, instanceOf(Connection.class));
        xaShardingTransactionManager.commit();
    }
    
    @Test
    public void assertGetConnectionOfNestedTransaction() throws SQLException {
        ThreadLocal<Set<Transaction>> transactions = getEnlistedTransactions(getCachedDataSources().get("ds1"));
        xaShardingTransactionManager.begin();
        assertThat(transactions.get().size(), is(0));
        xaShardingTransactionManager.getConnection("ds1");
        assertThat(transactions.get().size(), is(1));
        executeNestedTransaction(transactions);
        assertThat(transactions.get().size(), is(1));
        xaShardingTransactionManager.commit();
        assertThat(transactions.get().size(), is(0));
    }
    
    private void executeNestedTransaction(final ThreadLocal<Set<Transaction>> transactions) throws SQLException {
        xaShardingTransactionManager.begin();
        xaShardingTransactionManager.getConnection("ds1");
        assertThat(transactions.get().size(), is(2));
        xaShardingTransactionManager.commit();
        assertThat(transactions.get().size(), is(1));
    }
    
    @Test
    public void assertClose() throws Exception {
        xaShardingTransactionManager.close();
        Map<String, XATransactionDataSource> cachedSingleXADataSourceMap = getCachedDataSources();
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
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private Map<String, XATransactionDataSource> getCachedDataSources() {
        Field field = xaShardingTransactionManager.getClass().getDeclaredField("cachedDataSources");
        field.setAccessible(true);
        return (Map<String, XATransactionDataSource>) field.get(xaShardingTransactionManager);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    @SuppressWarnings("unchecked")
    private ThreadLocal<Set<Transaction>> getEnlistedTransactions(final XATransactionDataSource transactionDataSource) {
        Field field = transactionDataSource.getClass().getDeclaredField("enlistedTransactions");
        field.setAccessible(true);
        return (ThreadLocal<Set<Transaction>>) field.get(transactionDataSource);
    }
    
    private Collection<ResourceDataSource> createResourceDataSources(final DatabaseType databaseType) {
        List<ResourceDataSource> result = new LinkedList<>();
        result.add(new ResourceDataSource("ds1", DataSourceUtils.build(HikariDataSource.class, databaseType, "demo_ds_1")));
        result.add(new ResourceDataSource("ds2", DataSourceUtils.build(HikariDataSource.class, databaseType, "demo_ds_2")));
        result.add(new ResourceDataSource("ds3", DataSourceUtils.build(AtomikosDataSourceBean.class, databaseType, "demo_ds_3")));
        return result;
    }
}
