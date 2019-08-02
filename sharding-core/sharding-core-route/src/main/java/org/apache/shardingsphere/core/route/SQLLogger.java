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

package org.apache.shardingsphere.core.route;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * SQL logger.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j(topic = "ShardingSphere-SQL")
public final class SQLLogger {
    
    /**
     * Print SQL log for master slave rule.
     *
     * @param logicSQL logic SQL
     * @param dataSourceNames data source names
     */
    public static void logSQL(final String logicSQL, final Collection<String> dataSourceNames) {
        log("Rule Type: master-slave");
        log("SQL: {} ::: DataSources: {}", logicSQL, Joiner.on(",").join(dataSourceNames));
    }
    
    /**
     * Print SQL log for encrypt rule.
     * 
     * @param encryptSQL encrypt SQL
     */
    public static void logSQL(final String encryptSQL) { 
        log("Rule Type: encrypt");
        log("SQL: {}", encryptSQL);
    }
    
    /**
     * Print SQL log for sharding rule.
     * 
     * @param logicSQL logic SQL
     * @param showSimple whether show SQL in simple style
     * @param optimizedStatement optimized statement
     * @param routeUnits route units
     */
    public static void logSQL(final String logicSQL, final boolean showSimple, final OptimizedStatement optimizedStatement, final Collection<RouteUnit> routeUnits) {
        log("Rule Type: sharding");
        log("Logic SQL: {}", logicSQL);
        log("SQLStatement: {}", optimizedStatement);
        if (showSimple) {
            logSimpleMode(routeUnits);
        } else {
            logNormalMode(routeUnits);
        }
    }
    
    private static void logSimpleMode(final Collection<RouteUnit> routeUnits) {
        Set<String> dataSourceNames = new HashSet<>(routeUnits.size());
        for (RouteUnit each : routeUnits) {
            dataSourceNames.add(each.getDataSourceName());
        }
        log("Actual SQL(simple): {} ::: {}", dataSourceNames, routeUnits.size());
    }
    
    private static void logNormalMode(final Collection<RouteUnit> routeUnits) {
        for (RouteUnit each : routeUnits) {
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
