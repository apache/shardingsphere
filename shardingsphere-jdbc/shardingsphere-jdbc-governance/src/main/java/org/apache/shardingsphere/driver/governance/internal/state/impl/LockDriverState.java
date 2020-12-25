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

package org.apache.shardingsphere.driver.governance.internal.state.impl;

import org.apache.shardingsphere.driver.governance.internal.circuit.datasource.CircuitBreakerDataSource;
import org.apache.shardingsphere.driver.governance.internal.state.DriverState;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.state.StateContext;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.TransactionTypeHolder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;

/**
 * Lock driver state.
 */
public final class LockDriverState implements DriverState {
    
    @Override
    public Connection getConnection(final Map<String, DataSource> dataSourceMap, 
                                    final MetaDataContexts metaDataContexts, final TransactionContexts transactionContexts, final TransactionType transactionType) {
        block(metaDataContexts.getProps().<Long>getValue(ConfigurationPropertyKey.LOCK_WAIT_TIMEOUT_MILLISECONDS));
        if (StateContext.getCurrentState() == StateType.OK) {
            return new ShardingSphereConnection(dataSourceMap, metaDataContexts, transactionContexts, TransactionTypeHolder.get());
        } else if (StateContext.getCurrentState() == StateType.CIRCUIT_BREAK) {
            return new CircuitBreakerDataSource().getConnection();
        }
        throw new UnsupportedOperationException(String.format("Unknown driver state type: %s", StateContext.getCurrentState().name()));
    }
    
    private void block(final long lockTimeoutMilliseconds) {
        if (!LockContext.await(lockTimeoutMilliseconds)) {
            throw new ShardingSphereException("Service lock wait timeout of %s ms exceeded", lockTimeoutMilliseconds);
        }
    }
}
