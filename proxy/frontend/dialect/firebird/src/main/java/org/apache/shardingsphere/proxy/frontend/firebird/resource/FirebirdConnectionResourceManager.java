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

package org.apache.shardingsphere.proxy.frontend.firebird.resource;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.constant.protocol.FirebirdConnectionProtocolVersion;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.FirebirdBlobBinaryProtocolValue;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.batch.FirebirdBatchStatementManager;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.FirebirdStatementIdGenerator;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.statement.fetch.FirebirdFetchStatementCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.transaction.FirebirdTransactionIdGenerator;

/**
 * Connection resource manager for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class FirebirdConnectionResourceManager {
    
    private static final FirebirdConnectionResourceManager INSTANCE = new FirebirdConnectionResourceManager();
    
    /**
     * Get connection resource manager instance.
     *
     * @return connection resource manager instance
     */
    public static FirebirdConnectionResourceManager getInstance() {
        return INSTANCE;
    }
    
    /**
     * Register connection resources.
     *
     * @param connectionId connection ID
     */
    public void registerConnection(final int connectionId) {
        FirebirdTransactionIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdStatementIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdBlobIdGenerator.getInstance().registerConnection(connectionId);
        FirebirdBlobHandleGenerator.getInstance().registerConnection(connectionId);
        FirebirdBlobWriteCache.getInstance().registerConnection(connectionId);
        FirebirdBlobReadCache.getInstance().registerConnection(connectionId);
        FirebirdFetchStatementCache.getInstance().registerConnection(connectionId);
        FirebirdBatchStatementManager.getInstance().registerConnection(connectionId);
    }
    
    /**
     * Unregister connection resources.
     *
     * @param connectionId connection ID
     */
    public void unregisterConnection(final int connectionId) {
        FirebirdStatementIdGenerator.getInstance().unregisterConnection(connectionId);
        FirebirdTransactionIdGenerator.getInstance().unregisterConnection(connectionId);
        FirebirdBlobIdGenerator.getInstance().unregisterConnection(connectionId);
        FirebirdBlobHandleGenerator.getInstance().unregisterConnection(connectionId);
        FirebirdBlobWriteCache.getInstance().unregisterConnection(connectionId);
        FirebirdBlobReadCache.getInstance().unregisterConnection(connectionId);
        FirebirdConnectionProtocolVersion.getInstance().unsetProtocolVersion(connectionId);
        FirebirdBlobBinaryProtocolValue.unregisterConnection(connectionId);
        FirebirdFetchStatementCache.getInstance().unregisterConnection(connectionId);
        FirebirdBatchStatementManager.getInstance().unregisterConnection(connectionId);
    }
}
