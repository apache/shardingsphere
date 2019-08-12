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
import lombok.SneakyThrows;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;
import org.apache.shardingsphere.transaction.xa.jta.connection.SingleXAConnection;
import org.apache.shardingsphere.transaction.xa.jta.datasource.SingleXADataSource;
import org.apache.shardingsphere.transaction.xa.manager.XATransactionManagerLoader;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import javax.sql.DataSource;
import javax.transaction.Status;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;

/**
 * Sharding transaction manager for XA.
 *
 * @author zhaojun
 */
public final class XAShardingTransactionManager implements ShardingTransactionManager {
    
    private final Map<String, SingleXADataSource> singleXADataSourceMap = new HashMap<>();
    
    private final XATransactionManager xaTransactionManager = XATransactionManagerLoader.getInstance().getTransactionManager();

	// private ThreadLocal<List<String>> enlistedXAResource = new ThreadLocal<List<String>>() {
	// @Override
	// public List<String> initialValue() {
	// return new LinkedList<>();
	// }
	// };

    @Override
    public void init(final DatabaseType databaseType, final Collection<ResourceDataSource> resourceDataSources) {
        for (ResourceDataSource each : resourceDataSources) {
            DataSource dataSource = each.getDataSource();
            if (dataSource instanceof AtomikosDataSourceBean) {
                continue;
            }
            SingleXADataSource singleXADataSource = new SingleXADataSource(databaseType, each.getUniqueResourceName(), dataSource);
            singleXADataSourceMap.put(each.getOriginalName(), singleXADataSource);
            xaTransactionManager.registerRecoveryResource(each.getUniqueResourceName(), singleXADataSource.getXaDataSource());
        }
        xaTransactionManager.init();
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @SneakyThrows
    @Override
    public boolean isInTransaction() {
        return Status.STATUS_NO_TRANSACTION != xaTransactionManager.getTransactionManager().getStatus();
    }
    
    @SneakyThrows
    @Override
    public Connection getConnection(final String dataSourceName) {
		// XAShardingTransactionManager should not maintain the enlisted resources, because it cannot control the
		// association/desociation of transactions. Consider the following scenario( A{REQUIRED} -> B{REQUIRES_NEW} ): <br />
		// 0. TM begins a xa transaction(txA) for A;
		// 1. A performs an insert operation in DataSource demo_ds;
		// 2. A invokes service B, the propagation of B is REQUIRES_NEW, so TM begins a new xa transaction(txB) for B;
		// 3. B performs an insert operation in the same DataSource demo_ds too;
		// in this scenario, the XAResource should be enlisted to both txA and txB. In the existing implementation, the
		// XAResource is only enlisted to txA, because when B get the connection, there is a dataSourceName associated with
		// current thread.
		//
		// Since the XAShardingTransactionManager does not participate in the thread control of transaction, it should always
		// dispatch the enlistment to the TM, the TM will decide if and how to enlist these xa resources.
    	TransactionManager transactionManager = this.xaTransactionManager.getTransactionManager();
    	Transaction transaction = transactionManager.getTransaction();
		if (transaction == null) {
			throw new IllegalStateException("XAShardingTransactionManager requires an XA transaction."); // should never happen
		}
        SingleXAConnection singleXAConnection = singleXADataSourceMap.get(dataSourceName).getXAConnection();
        transaction.enlistResource(singleXAConnection.getXAResource());
        return singleXAConnection.getConnection();
    }
    
    @SneakyThrows
    @Override
    public void begin() {
        xaTransactionManager.getTransactionManager().begin();
    }
    
    @SneakyThrows
    @Override
	public void commit() {
		xaTransactionManager.getTransactionManager().commit();
	}
    
    @SneakyThrows
    @Override
	public void rollback() {
		xaTransactionManager.getTransactionManager().rollback();
	}
    
    @Override
    public void close() throws Exception {
        for (SingleXADataSource each : singleXADataSourceMap.values()) {
            xaTransactionManager.removeRecoveryResource(each.getResourceName(), each.getXaDataSource());
        }
        singleXADataSourceMap.clear();
        xaTransactionManager.close();
    }
}
