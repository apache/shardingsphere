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

package org.apache.shardingsphere.core.shard.log;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.executor.context.ExecutionUnit;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * SQL logger for sharding.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-SQL")
public final class ShardingSQLLogger {
    
    /**
     * Print SQL log for sharding rule.
     * 
     * @param logicSQL logic SQL
     * @param showSimple whether show SQL in simple style
     * @param sqlStatementContext SQL statement context
     * @param executionUnits execution units
     */
    public static void logSQL(final String logicSQL, final boolean showSimple, final SQLStatementContext sqlStatementContext, final Collection<ExecutionUnit> executionUnits) {
        log("Rule Type: sharding");
        log("Logic SQL: {}", logicSQL);
        log("SQLStatement: {}", sqlStatementContext);
        if (showSimple) {
            logSimpleMode(executionUnits);
        } else {
            logNormalMode(executionUnits);
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
