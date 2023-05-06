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

package org.apache.shardingsphere.infra.connection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.connection.datasource.PreferredDataSourceProvider;
import org.apache.shardingsphere.infra.connection.transaction.TransactionConnectionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Connection context.
 */
@RequiredArgsConstructor
@Getter
public final class ConnectionContext implements AutoCloseable {
    
    private final CursorConnectionContext cursorContext = new CursorConnectionContext();
    
    private final TransactionConnectionContext transactionContext = new TransactionConnectionContext();
    
    @Getter(AccessLevel.NONE)
    private final PreferredDataSourceProvider preferredDataSourceProvider;
    
    @Setter
    private String trafficInstanceId;
    
    public ConnectionContext() {
        this(Collections::emptySet);
    }
    
    /**
     * Get preferred data source names.
     *
     * @return preferred data source names
     */
    public Collection<String> getPreferredDataSourceNames() {
        return preferredDataSourceProvider.getPreferredDataSourceNames();
    }
    
    /**
     * Get traffic instance ID.
     *
     * @return traffic instance ID
     */
    public Optional<String> getTrafficInstanceId() {
        return Optional.ofNullable(trafficInstanceId);
    }
    
    /**
     * Clear cursor connection context.
     */
    public void clearCursorConnectionContext() {
        cursorContext.close();
    }
    
    /**
     * Clear transaction connection context.
     */
    public void clearTransactionConnectionContext() {
        transactionContext.close();
    }
    
    @Override
    public void close() {
        trafficInstanceId = null;
        clearCursorConnectionContext();
        clearTransactionConnectionContext();
    }
}
