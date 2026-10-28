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

package org.apache.shardingsphere.infra.executor.checker;

import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Push down SQL execution check engine.
 */
@HighFrequencyInvocation
public final class PushDownSQLExecutionCheckEngine {
    
    /**
     * Check ordinary push down execution after routing and before backend execution.
     *
     * @param database database
     * @param executionContext execution context outside SQL Federation
     * @throws SQLException SQL exception
     */
    public void check(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        Collection<ExecutionUnit> executionUnits = executionContext.getExecutionUnits();
        if (executionUnits.size() < 2 || !(executionContext.getSqlStatementContext().getSqlStatement() instanceof SelectStatement)) {
            return;
        }
        Map<String, StorageUnit> storageUnits = database.getResourceMetaData().getStorageUnits();
        String storageType = storageUnits.get(executionUnits.iterator().next().getDataSourceName()).getStorageType().getType();
        for (ExecutionUnit each : executionUnits) {
            ShardingSpherePreconditions.checkState(storageType.equals(storageUnits.get(each.getDataSourceName()).getStorageType().getType()),
                    () -> new UnsupportedSQLOperationException("SELECT across different storage database types in push down execution").toSQLException());
        }
    }
}
