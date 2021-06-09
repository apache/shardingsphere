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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * PostgreSQL connection context.
 */
@Getter
@Setter
public final class PostgreSQLConnectionContext {
    
    private final Collection<CommandExecutor> pendingExecutors = new LinkedList<>();
    
    private SQLStatement sqlStatement;
    
    private long updateCount;
    
    /**
     * Get describe command executor.
     *
     * @return describe command executor
     */
    public Optional<PostgreSQLComDescribeExecutor> getDescribeExecutor() {
        return pendingExecutors.stream().filter(PostgreSQLComDescribeExecutor.class::isInstance).map(PostgreSQLComDescribeExecutor.class::cast).findFirst();
    }
    
    /**
     * Get SQL statement.
     *
     * @return SQL statement
     */
    public Optional<SQLStatement> getSqlStatement() {
        return Optional.ofNullable(sqlStatement);
    }
    
    /**
     * Clear context.
     */
    public void clearContext() {
        pendingExecutors.clear();
        sqlStatement = null;
        updateCount = 0;
    }
}
