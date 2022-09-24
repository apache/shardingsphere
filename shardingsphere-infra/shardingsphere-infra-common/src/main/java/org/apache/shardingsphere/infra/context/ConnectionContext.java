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

package org.apache.shardingsphere.infra.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.infra.context.cursor.CursorConnectionContext;
import org.apache.shardingsphere.infra.context.datasource.PreferredDataSourceProvider;
import org.apache.shardingsphere.infra.context.transaction.TransactionConnectionContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Connection context.
 */
@RequiredArgsConstructor
@Getter
public final class ConnectionContext implements AutoCloseable {
    
    private final CursorConnectionContext cursorConnectionContext = new CursorConnectionContext();
    
    private final TransactionConnectionContext transactionConnectionContext = new TransactionConnectionContext();
    
    @Getter(AccessLevel.NONE)
    private final PreferredDataSourceProvider preferredDataSourceProvider;
    
    @Setter
    private String trafficInstanceId;
    
    public ConnectionContext() {
        preferredDataSourceProvider = Collections::emptySet;
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
     * Get traffic instance id.
     *
     * @return traffic instance id
     */
    public Optional<String> getTrafficInstanceId() {
        return Optional.ofNullable(trafficInstanceId);
    }
    
    @Override
    public void close() {
        clearTrafficInstance();
        clearCursorConnectionContext();
        clearTransactionConnectionContext();
    }
    
    /**
     * Clear traffic instance.
     */
    public void clearTrafficInstance() {
        trafficInstanceId = null;
    }
    
    /**
     * Clear cursor connection context.
     */
    public void clearCursorConnectionContext() {
        cursorConnectionContext.close();
    }
    
    /**
     * Clear transaction connection context.
     */
    public void clearTransactionConnectionContext() {
        transactionConnectionContext.close();
    }
}
