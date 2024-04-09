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

package org.apache.shardingsphere.infra.session.connection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.session.connection.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.session.connection.datasource.UsedDataSourceProvider;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
    private final UsedDataSourceProvider usedDataSourceProvider;
    
    private String databaseName;
    
    @Setter
    private String trafficInstanceId;
    
    public ConnectionContext() {
        this(Collections::emptySet);
    }
    
    /**
     * Get used data source names.
     *
     * @return used data source names
     */
    public Collection<String> getUsedDataSourceNames() {
        Collection<String> result = new HashSet<>(usedDataSourceProvider.getNames().size(), 1L);
        for (String each : usedDataSourceProvider.getNames()) {
            result.add(each.contains(".") ? each.split("\\.")[1] : each);
        }
        return result;
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
    public void clearCursorContext() {
        cursorContext.close();
    }
    
    /**
     * Clear transaction connection context.
     */
    public void clearTransactionContext() {
        transactionContext.close();
    }
    
    /**
     * Set current database name.
     *
     * @param databaseName database name
     */
    public void setCurrentDatabase(final String databaseName) {
        if (null != databaseName && !databaseName.equals(this.databaseName)) {
            this.databaseName = databaseName;
        }
    }
    
    /**
     * Get database name.
     *
     * @return database name
     */
    public Optional<String> getDatabaseName() {
        return Optional.ofNullable(databaseName);
    }
    
    @Override
    public void close() {
        trafficInstanceId = null;
        clearCursorContext();
        clearTransactionContext();
    }
}
