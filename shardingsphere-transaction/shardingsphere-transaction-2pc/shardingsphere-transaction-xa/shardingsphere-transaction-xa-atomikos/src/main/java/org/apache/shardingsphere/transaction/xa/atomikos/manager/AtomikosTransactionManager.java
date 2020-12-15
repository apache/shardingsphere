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

package org.apache.shardingsphere.transaction.xa.atomikos.manager;

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.core.XATransactionManagerType;
import org.apache.shardingsphere.transaction.xa.spi.SingleXAResource;
import org.apache.shardingsphere.transaction.xa.spi.XATransactionManager;

import javax.sql.XADataSource;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * Atomikos XA transaction manager.
 */
public final class AtomikosTransactionManager implements XATransactionManager {
    
    private final UserTransactionManager transactionManager = new UserTransactionManager();
    
    private final UserTransactionService userTransactionService = new UserTransactionServiceImp();
    
    @Override
    public void init() {
        userTransactionService.init();
    }
    
    @Override
    public void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        userTransactionService.registerResource(new AtomikosXARecoverableResource(dataSourceName, xaDataSource));
    }
    
    @Override
    public void removeRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
        userTransactionService.removeResource(new AtomikosXARecoverableResource(dataSourceName, xaDataSource));
    }
    
    @SneakyThrows({SystemException.class, RollbackException.class})
    @Override
    public void enlistResource(final SingleXAResource xaResource) {
        transactionManager.getTransaction().enlistResource(xaResource);
    }
    
    @Override
    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
    
    @Override
    public void close() {
        userTransactionService.shutdown(true);
    }
    
    @Override
    public String getType() {
        return XATransactionManagerType.ATOMIKOS.getType();
    }
}
