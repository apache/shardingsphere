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

import com.atomikos.icatch.config.UserTransactionService;
import com.atomikos.icatch.config.UserTransactionServiceImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import io.shardingsphere.transaction.xa.spi.XATransactionManager;
import lombok.SneakyThrows;

import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.xa.XAResource;

/**
 * Atomikos XA transaction manager.
 *
 * @author zhaojun
 */
public final class AtomikosTransactionManager implements XATransactionManager {
    
    private final UserTransactionManager underlyingTransactionManager = new UserTransactionManager();
    
    private final UserTransactionService userTransactionService = new UserTransactionServiceImp();
    
    @Override
    public void startup() {
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
    
    @Override
    @SneakyThrows
    public void enlistResource(final XAResource xaResource) {
        underlyingTransactionManager.getTransaction().enlistResource(xaResource);
    }
    
    @Override
    @SneakyThrows
    public void begin() {
        underlyingTransactionManager.begin();
    }
    
    @Override
    @SneakyThrows
    public void commit() {
        underlyingTransactionManager.commit();
    }
    
    @Override
    @SneakyThrows
    public void rollback() {
        // TODO mybatis may call rollback twice, need investigate reason here 
        if (Status.STATUS_NO_TRANSACTION != underlyingTransactionManager.getStatus()) {
            underlyingTransactionManager.rollback();
        }
    }
    
    @Override
    @SneakyThrows
    public int getStatus() {
        return underlyingTransactionManager.getStatus();
    }
    
    @Override
    public void destroy() {
        userTransactionService.shutdown(true);
    }
}
