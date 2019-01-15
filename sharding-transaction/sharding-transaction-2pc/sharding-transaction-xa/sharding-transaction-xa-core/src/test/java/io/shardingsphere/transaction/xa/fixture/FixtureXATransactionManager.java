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

import io.shardingsphere.transaction.xa.spi.XATransactionManager;

import javax.sql.XADataSource;
import javax.transaction.Status;
import javax.transaction.xa.XAResource;

public final class FixtureXATransactionManager implements XATransactionManager {
    
    @Override
    public void init() {
    }
    
    @Override
    public void registerRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
    }
    
    @Override
    public void removeRecoveryResource(final String dataSourceName, final XADataSource xaDataSource) {
    }
    
    @Override
    public void enlistResource(final XAResource xaResource) {
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
    public void close() {
    }
}
