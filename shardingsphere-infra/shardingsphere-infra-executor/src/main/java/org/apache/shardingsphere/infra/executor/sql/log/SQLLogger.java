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

package org.apache.shardingsphere.infra.executor.sql.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.binder.LogicSQL;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * SQL logger.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-SQL")
public final class SQLLogger {
    
    /**
     * Log SQL.
     * 
     * @param logicSQL logic SQL
     * @param showSimple whether show SQL in simple style
     * @param executionContext Execution context
     */
    public static void logSQL(final LogicSQL logicSQL, final boolean showSimple, final ExecutionContext executionContext) {
        log("Logic SQL: {}", logicSQL.getSql());
        log("SQLStatement: {}", logicSQL.getSqlStatementContext().getSqlStatement());
        if (showSimple) {
            logSimpleMode(executionContext.getExecutionUnits());
        } else {
            logNormalMode(executionContext.getExecutionUnits());
        }
    }
    
    private static void logSimpleMode(final Collection<ExecutionUnit> executionUnits) {
        Set<String> dataSourceNames = new HashSet<>(executionUnits.size());
        for (ExecutionUnit each : executionUnits) {
            dataSourceNames.add(each.getDataSourceName());
        }
        log("Actual SQL(simple): {} ::: {}", dataSourceNames, executionUnits.size());
    }
    
    private static void logNormalMode(final Collection<ExecutionUnit> executionUnits) {
        for (ExecutionUnit each : executionUnits) {
            if (each.getSqlUnit().getParameters().isEmpty()) {
                log("Actual SQL: {} ::: {}", each.getDataSourceName(), each.getSqlUnit().getSql());
            } else {
                log("Actual SQL: {} ::: {} ::: {}", each.getDataSourceName(), each.getSqlUnit().getSql(), each.getSqlUnit().getParameters());
            }
        }
    }
    
    private static void log(final String pattern, final Object... arguments) {
        log.info(pattern, arguments);
    }
}
