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

package io.shardingsphere.transaction.xa.fixture;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.transaction.spi.xa.XATransactionManager;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

public final class FixtureXATransactionManager implements XATransactionManager {
    @Override
    public void startup() {
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    public void begin() {
    }
    
    @Override
    public void commit() {
    }
    
    @Override
    public void rollback() {
    }
    
    @Override
    public int getStatus() {
        return Status.STATUS_NO_TRANSACTION;
    }
    
    @Override
    public DataSource wrapDataSource(final DatabaseType databaseType, final XADataSource xaDataSource, final String dataSourceName, final DataSourceParameter dataSourceParameter) {
        return null;
    }
    
    @Override
    public TransactionManager getUnderlyingTransactionManager() {
        return null;
    }
    
    @Override
    public void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
    
    }
    
    @Override
    public void removeRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
    
    }
    
    @Override
    public XAResource getRecoveryXAResource(final String dataSourceName) {
        return null;
    }
}
