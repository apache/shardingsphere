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
import com.atomikos.icatch.config.Configuration;
import com.atomikos.icatch.imp.CoordinatorImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.h2.engine.Session;
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

@Slf4j
@Getter
public abstract class TransactionManagerRecoveryTest {
    
    private static final String INSERT_INTO_T_ORDER = "INSERT INTO t_order VALUES(1000, 10, 'init')";
    
    private static final String SELECT_COUNT_T_ORDER = "SELECT count(1) from t_order";
    
    private AtomikosTransactionManager atomikosTransactionManager = new AtomikosTransactionManager();
    
    private Map<String, DataSource> xaDataSourceMap = createXADataSourceMap();
    
    private XATransactionEvent beginEvent = new XATransactionEvent(TransactionOperationType.BEGIN);
    
    private XATransactionEvent commitEvent = new XATransactionEvent(TransactionOperationType.COMMIT);
    
    private XATransactionEvent rollbackEvent = new XATransactionEvent(TransactionOperationType.ROLLBACK);
    
    @Before
    public void setup() {
        executeSQL("ds1", "DROP TABLE IF EXISTS t_order");
        executeSQL("ds1", "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
        executeSQL("ds2", "DROP TABLE IF EXISTS t_order");
        executeSQL("ds2", "CREATE TABLE IF NOT EXISTS t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
    }
    
    @After
    public void teardown() {
        Configuration.shutdown(true);
        closeDataSource();
    }
    
    @Test
    public void assertAtomikos2PC() {
        atomikosTransactionManager.begin(beginEvent);
        insertOrder("ds1");
        insertOrder("ds2");
        atomikosTransactionManager.commit(commitEvent);
        assertOrderCount("ds1", 1L);
        assertOrderCount("ds2", 1L);
    }
    
    @Test
    public void assertShutdownRecoveryAfterPrepared() {
        atomikosTransactionManager.begin(beginEvent);
        insertOrder("ds1");
        assertOrderCount("ds1", 1L);
        coordinateOnlyExecutePrepare();
        Configuration.shutdown(true);
        assertOrderCount("ds1", 0L);
    }
    
//    @Test(expected = IllegalStateException.class)
//    public void assertAccessFailedAfterPrepared() {
//        atomikosTransactionManager.begin(beginEvent);
//        insertOrder("ds1");
//        coordinateOnlyExecutePrepare();
//        try {
//            assertOrderCount("ds1", 1L);
//            // CHECKSTYLE:OFF
//        } catch (Exception ex) {
//            // CHECKSTYLE:ON
//            assertTrue(ex.getMessage().contains("no longer active but in state IN_DOUBT"));
//            throw ex;
//        }
//    }
    
    @Test
    @SneakyThrows
    public void assertAccessSucceedAfterPrepared() {
        atomikosTransactionManager.begin(beginEvent);
        insertOrder("ds1");
        coordinateOnlyExecutePrepare();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                atomikosTransactionManager.begin(beginEvent);
                assertOrderCount("ds1", 0L);
            }
        });
        thread.start();
        thread.join();
    }
    
    @Test
    @SneakyThrows
    public void assertLockResourceAfterPrepared() {
        atomikosTransactionManager.begin(beginEvent);
        insertOrder("ds1");
        coordinateOnlyExecutePrepare();
        Thread thread = new Thread(new LockResourceTask());
        thread.start();
        thread.join();
    }
    
    @Test
    @SneakyThrows
    public void assertLockResourceAfterStartTransaction() {
        atomikosTransactionManager.begin(beginEvent);
        insertOrder("ds1");
        Thread thread = new Thread(new LockResourceTask());
        thread.start();
        thread.join();
    }
    
    @Test
    @SneakyThrows
    public void assertSucceedInAfterXAResourceReleased() {
        closeDataSource();
        xaDataSourceMap = createXADataSourceMap();
        xaDataSourceMap.get("ds1").getConnection();
    }
    
    protected abstract Session getH2Session(String dsName);

    final void insertOrder(final String ds) {
        executeSQL(ds, INSERT_INTO_T_ORDER);
    }
    
    final void assertOrderCount(final String ds, final long expectedCount) {
        assertEquals(expectedCount, executeSQL(ds, SELECT_COUNT_T_ORDER));
    }
    
    private void closeDataSource() {
        for (DataSource each : xaDataSourceMap.values()) {
            ReflectiveUtil.methodInvoke(each, "close");
        }
    }
    
    @SneakyThrows
    private void coordinateOnlyExecutePrepare() {
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
    
    final Map<String, DataSource> createXADataSourceMap() {
        Map<String, DataSource> result = new HashMap<>();
        result.put("ds1", createXADataSource("ds1"));
        result.put("ds2", createXADataSource("ds2"));
        return result;
    }
    
    protected abstract DataSource createXADataSource(String dsName);
    
    private final class LockResourceTask implements Runnable {
        
        @Override
        public void run() {
            try {
                atomikosTransactionManager.begin(beginEvent);
                insertOrder("ds1");
                // CHECKSTYLE:OFF
            } catch (Exception ex) {
                // CHECKSTYLE:ON
                assertTrue(ex.getMessage().contains("Timeout trying to lock table ; SQL statement:"));
                throw ex;
            }
        }
    }
}
