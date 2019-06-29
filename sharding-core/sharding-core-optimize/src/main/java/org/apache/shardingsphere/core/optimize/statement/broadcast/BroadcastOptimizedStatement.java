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

package org.apache.shardingsphere.core.optimize.statement.broadcast;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.optimize.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;

/**
 * Optimized statement for broadcast.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class BroadcastOptimizedStatement implements OptimizedStatement {
    
    private final SQLStatement sqlStatement;
    
    @Setter
    private String logicTableNameForDropIndex;
    
    @Override
    public SQLStatement getSQLStatement() {
        return sqlStatement;
    }
    
    /**
     * Get logic table name for drop index.
     *
     * @return logic table name for drop index
     */
    public Optional<String> getLogicTableNameForDropIndex() {
        return Optional.fromNullable(logicTableNameForDropIndex);
    }
}
