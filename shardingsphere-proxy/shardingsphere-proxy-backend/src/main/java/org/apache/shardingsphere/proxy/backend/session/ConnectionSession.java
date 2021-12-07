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

package org.apache.shardingsphere.proxy.backend.session;

import io.netty.util.AttributeMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.proxy.backend.communication.SQLStatementSchemaHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Connection session.
 */
@Getter
@Setter
public abstract class ConnectionSession {
    
    @Setter(AccessLevel.NONE)
    private volatile String schemaName;
    
    private volatile int connectionId;
    
    private volatile Grantee grantee;
    
    private final TransactionStatus transactionStatus;
    
    private final AttributeMap attributeMap;
    
    private final AtomicBoolean autoCommit = new AtomicBoolean(true);
    
    public ConnectionSession(final TransactionType initialTransactionType, final AttributeMap attributeMap) {
        transactionStatus = new TransactionStatus(initialTransactionType);
        this.attributeMap = attributeMap;
    }
    
    /**
     * Change schema of current channel.
     *
     * @param schemaName schema name
     */
    public void setCurrentSchema(final String schemaName) {
        if (null != schemaName && schemaName.equals(this.schemaName)) {
            return;
        }
        if (transactionStatus.isInTransaction()) {
            throw new ShardingSphereException("Failed to switch schema, please terminate current transaction.");
        }
        this.schemaName = schemaName;
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public String getSchemaName() {
        return null == SQLStatementSchemaHolder.get() ? schemaName : SQLStatementSchemaHolder.get();
    }
    
    /**
     * Get default schema name.
     *
     * @return default schema name
     */
    public String getDefaultSchemaName() {
        return schemaName;
    }
    
    /**
     * Is autocommit.
     *
     * @return is autocommit
     */
    public boolean isAutoCommit() {
        return autoCommit.get();
    }
    
    /**
     * Set autocommit.
     *
     * @param autoCommit autocommit
     */
    public void setAutoCommit(final boolean autoCommit) {
        this.autoCommit.set(autoCommit);
    }
}
