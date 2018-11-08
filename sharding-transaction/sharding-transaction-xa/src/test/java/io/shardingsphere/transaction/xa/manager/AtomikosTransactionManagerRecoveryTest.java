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
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.PoolType;
import io.shardingsphere.core.constant.transaction.TransactionOperationType;
import io.shardingsphere.core.event.transaction.xa.XATransactionEvent;
import io.shardingsphere.transaction.xa.convert.dialect.XADataSourceFactory;
import io.shardingsphere.transaction.xa.convert.extractor.DataSourceParameterFactory;
import io.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import io.shardingsphere.transaction.xa.fixture.ReflectiveUtil;
import lombok.SneakyThrows;
import org.h2.tools.RunScript;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import javax.transaction.Transaction;
import java.io.StringReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AtomikosTransactionManagerRecoveryTest {
    
    private AtomikosTransactionManager atomikosTransactionManager = (AtomikosTransactionManager) XATransactionManagerSPILoader.getInstance().getTransactionManager();
    
    private Map<String, DataSource> xaDataSourceMap = createXADataSourceMap();
    
    @Before
    @SneakyThrows
    public void setup() {
        createTable();
    }
    
    @SneakyThrows
    private void createTable() {
        executeSQL("ds1", "CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
        executeSQL("ds2", "CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
    }
    
    @Test
    @SneakyThrows
    public void assertOnlyExecutePrepareThenShutdown() {
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        executeSQL("ds1", "INSERT INTO t_order VALUES(1000, 10, 'init')");
        executeSQL("ds2", "INSERT INTO t_order VALUES(1000, 10, 'init')");
        assertTrue(executeSQL("ds1", "SELECT count(1) from t_order"));
        mockAtomikosOnlyExecutePreparePhase();
        atomikosTransactionManager.destroy();
        assertFalse(executeSQL("ds1", "SELECT count(1) from t_order"));
        closeDataSource();
        atomikosTransactionManager = new AtomikosTransactionManager();
        xaDataSourceMap = createXADataSourceMap();
        Connection connection = xaDataSourceMap.get("ds1").getConnection();
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
    private boolean executeSQL(final String dsName, final String sql) {
        try (Connection connection = xaDataSourceMap.get(dsName).getConnection()) {
            return RunScript.execute(connection, new StringReader(sql)).next();
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
