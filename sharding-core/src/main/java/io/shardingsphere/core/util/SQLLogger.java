/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.util;

import com.google.common.base.Joiner;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.routing.RouteUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * SQL logger.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@Slf4j(topic = "Sharding-Sphere-SQL")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLLogger {
    
    /**
     * Print SQL log for sharding rule.
     * 
     * @param logicSQL logic SQL
     * @param sqlStatement SQL statement
     * @param routeUnits route units
     */
    public static void logSQL(final String logicSQL, final SQLStatement sqlStatement, final Collection<RouteUnit> routeUnits) {
        log("Rule Type: sharding");
        log("Logic SQL: {}", logicSQL);
        log("SQLStatement: {}", sqlStatement);
        for (RouteUnit each : routeUnits) {
            if (each.getSqlUnit().getParameterSets().get(0).isEmpty()) {
                log("Actual SQL: {} ::: {}", each.getDataSourceName(), each.getSqlUnit().getSql());
            } else {
                log("Actual SQL: {} ::: {} ::: {}", each.getDataSourceName(), each.getSqlUnit().getSql(), each.getSqlUnit().getParameterSets());
            }
        }
    }
    
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
    
    private static void log(final String pattern, final Object... arguments) {
        log.info(pattern, arguments);
    }
}
