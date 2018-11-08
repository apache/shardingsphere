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

import com.atomikos.icatch.CompositeTransaction;
import com.atomikos.icatch.imp.CoordinatorImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import com.atomikos.jdbc.AtomikosSQLException;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.transaction.xa.convert.dialect.XADataSourceFactory;
import io.shardingsphere.transaction.xa.convert.extractor.DataSourceParameterFactory;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.SneakyThrows;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AtomikosTransactionManagerRecoveryTest {
    
    private static final String INSERT_INTO_T_ORDER = "INSERT INTO t_order VALUES(1000, 10, 'init')";
    
    private static final String SELECT_COUNT_T_ORDER = "SELECT count(1) from t_order";
    
    private AtomikosTransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
    
    private Map<String, DataSource> xaDataSourceMap = createXADataSourceMap();
    
    @Before
    public void setup() {
        executeSQL("ds1", "DROP TABLE IF EXISTS t_order");
        executeSQL("ds1", "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
        executeSQL("ds2", "DROP TABLE IF EXISTS t_order");
        executeSQL("ds2", "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
    }
    
    @After
    public void teardown() {
        atomikosTransactionManager.destroy();
        closeDataSource();
    }
    
    @Test
    public void assertOnlyExecutePrepareThenRecoveryInShutdown() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", INSERT_INTO_T_ORDER);
        assertEquals(1L, executeSQL("ds1", SELECT_COUNT_T_ORDER));
        mockAtomikosOnlyExecutePreparePhase();
        atomikosTransactionManager.destroy();
        assertEquals(0L, executeSQL("ds1", SELECT_COUNT_T_ORDER));
    }
    
    @Test(expected = AtomikosSQLException.class)
    public void assertCannotRegistryResourceAgainWhenDataSourceIsNotClose() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", INSERT_INTO_T_ORDER);
        atomikosTransactionManager.rollback(new XATransactionEvent(TransactionOperationType.ROLLBACK));
        xaDataSourceMap = createXADataSourceMap();
        assertEquals(0L, executeSQL("ds1", SELECT_COUNT_T_ORDER));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertCurrentThreadCannotDoEnlistInDoubtState() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", INSERT_INTO_T_ORDER);
        executeSQL("ds2", INSERT_INTO_T_ORDER);
        mockAtomikosOnlyExecutePreparePhase();
        executeSQL("ds1", SELECT_COUNT_T_ORDER);
    }
    
    @Test
    @SneakyThrows
    public void assertDoEnlistInAnotherThreadWhenInDoubtState() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", INSERT_INTO_T_ORDER);
        mockAtomikosOnlyExecutePreparePhase();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
                assertEquals(0L, executeSQL("ds1", SELECT_COUNT_T_ORDER));
            }
        });
        thread.start();
        thread.join();
    }
    
    @Test
    public void assertDoPrepareCommitSucceed() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", INSERT_INTO_T_ORDER);
        executeSQL("ds2", INSERT_INTO_T_ORDER);
        assertEquals(1L, executeSQL("ds1", SELECT_COUNT_T_ORDER));
        assertEquals(1L, executeSQL("ds2", SELECT_COUNT_T_ORDER));
        atomikosTransactionManager.commit(new XATransactionEvent(TransactionOperationType.COMMIT));
        atomikosTransactionManager.destroy();
        closeDataSource();
        atomikosTransactionManager = new AtomikosTransactionManager();
        xaDataSourceMap = createXADataSourceMap();
        assertEquals(1L, executeSQL("ds1", SELECT_COUNT_T_ORDER));
        assertEquals(1L, executeSQL("ds2", SELECT_COUNT_T_ORDER));
        atomikosTransactionManager.destroy();
    }
    
    @Test
    @SneakyThrows
    public void assertPrepareAlsoLockedResource() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", INSERT_INTO_T_ORDER);
        mockAtomikosOnlyExecutePreparePhase();
    
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
                    executeSQL("ds1", INSERT_INTO_T_ORDER);
                    // CHECKSTYLE:OFF
                } catch (Exception ex) {
                    // CHECKSTYLE:ON
                    assertTrue(ex.getMessage().contains("Timeout trying to lock table ; SQL statement:"));
                    throw ex;
                }
            }
        });
        thread.start();
        thread.join();
    }
    
    private void closeDataSource() {
        for (DataSource each : xaDataSourceMap.values()) {
            ReflectiveUtil.methodInvoke(each, "close");
        }
    }
    
    @SneakyThrows
    private void mockAtomikosOnlyExecutePreparePhase() {
        UserTransactionManager transactionManager = (UserTransactionManager) atomikosTransactionManager.getUnderlyingTransactionManager();
        Transaction transaction = transactionManager.getTransaction();
        CompositeTransaction compositeTransaction = (CompositeTransaction) ReflectiveUtil.getProperty(transaction, "compositeTransaction");
        CoordinatorImp coordinator = (CoordinatorImp) compositeTransaction.getCompositeCoordinator();
        coordinator.prepare();
    }
    
    @SneakyThrows
    private Object executeSQL(final String dsName, final String sql) {
        Object result = null;
        try (Connection connection = xaDataSourceMap.get(dsName).getConnection()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = null;
            if (statement.execute(sql)) {
                resultSet = statement.getResultSet();
            }
            if (null != resultSet) {
                if (resultSet.next()) {
                    result = resultSet.getObject(1);
                }
            }
            return result;
        }
    }
    
    @Test
    public void assertBasicManagedDataSourceRecovery() {
    
    }
    
    private Map<String, DataSource> createXADataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds1", createXADataSource("ds1"));
        result.put("ds2", createXADataSource("ds2"));
        return result;
    }
    
    private DataSource createXADataSource(final String dsName) {
        DataSource dataSource = DataSourceUtils.build(PoolType.HIKARI, DatabaseType.H2, dsName);
        return atomikosTransactionManager.wrapDataSource(XADataSourceFactory.build(DatabaseType.H2), dsName, DataSourceParameterFactory.build(dataSource));
    }
}
