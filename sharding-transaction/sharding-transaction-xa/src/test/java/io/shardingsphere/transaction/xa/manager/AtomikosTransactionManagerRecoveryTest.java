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
import org.junit.Test;

import javax.sql.DataSource;
import javax.transaction.Transaction;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class AtomikosTransactionManagerRecoveryTest {
    
    private AtomikosTransactionManager atomikosTransactionManager = (AtomikosTransactionManager) XATransactionManagerSPILoader.getInstance().getTransactionManager();
    
    @Test
    @SneakyThrows
    public void assertAtomikosDataSourceBeanRecovery() {
        Map<String, DataSource> xaDataSourceMap = createXADataSourceMap();
        atomikosTransactionManager.begin(new XATransactionEvent(TransactionOperationType.BEGIN));
        try (Connection connection = xaDataSourceMap.get("ds1").getConnection()) {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE t_order (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))");
            statement.execute("INSERT INTO t_order VALUES(1000, 10, 'init');");
        }
        UserTransactionManager transactionManager = (UserTransactionManager) atomikosTransactionManager.getUnderlyingTransactionManager();
        Transaction transaction = transactionManager.getTransaction();
        CompositeTransaction compositeTransaction = (CompositeTransaction) ReflectiveUtil.getProperty(transaction, "compositeTransaction");
        CoordinatorImp coordinator = (CoordinatorImp) compositeTransaction.getCompositeCoordinator();
        coordinator.prepare();
        
        ReflectiveUtil.methodInvoke(transactionManager, "shutdownTransactionService");
        transactionManager.close();
        atomikosTransactionManager = new AtomikosTransactionManager();
        xaDataSourceMap = createXADataSourceMap();
        Connection connection = xaDataSourceMap.get("ds1").getConnection();
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
