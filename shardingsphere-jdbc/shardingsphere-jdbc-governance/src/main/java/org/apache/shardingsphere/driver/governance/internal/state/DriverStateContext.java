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

package org.apache.shardingsphere.driver.governance.internal.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.governance.internal.state.impl.CircuitBreakDriverState;
import org.apache.shardingsphere.driver.governance.internal.state.impl.LockDriverState;
import org.apache.shardingsphere.driver.governance.internal.state.impl.OKDriverState;
import org.apache.shardingsphere.infra.context.manager.ContextManager;
import org.apache.shardingsphere.infra.state.StateType;
import org.apache.shardingsphere.transaction.core.TransactionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Driver state context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DriverStateContext {
    
    private static final Map<StateType, DriverState> STATES = new ConcurrentHashMap<>(3, 1);
    
    static {
        STATES.put(StateType.OK, new OKDriverState());
        STATES.put(StateType.LOCK, new LockDriverState());
        STATES.put(StateType.CIRCUIT_BREAK, new CircuitBreakDriverState());
    }
    
    /**
     * Get connection.
     *
     * @param schemaName schema name
     * @param dataSourceMap data source map
     * @param contextManager context manager
     * @param transactionType transaction type
     * @return connection
     */
    public static Connection getConnection(final String schemaName, final Map<String, DataSource> dataSourceMap, final ContextManager contextManager, final TransactionType transactionType) {
        return STATES.get(contextManager.getMetaDataContexts().getStateContext().getCurrentState()).getConnection(schemaName, dataSourceMap, contextManager, transactionType);
    }
}
