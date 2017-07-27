/*
 * Copyright 1999-2015 dangdang.com.
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

package com.dangdang.ddframe.rdb.sharding.util;

import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.routing.SQLExecutionUnit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.List;

/**
 * SQL日志对象.
 * 
 * @author zhangliang 
 */
@Slf4j(topic = "Sharding-JDBC-SQL")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SQLLogger {
    
    /**
     * 打印SQL日志.
     * 
     * @param logicSQL 逻辑SQL
     * @param sqlStatement SQL语句解析对象
     * @param sqlExecutionUnits SQL最小执行单元集合
     * @param parameters SQL参数集合
     */
    public static void logSQL(final String logicSQL, final SQLStatement sqlStatement, final Collection<SQLExecutionUnit> sqlExecutionUnits, final List<Object> parameters) {
        log("Logic SQL: {}", logicSQL);
        log("SQLStatement: {}", sqlStatement);
        for (SQLExecutionUnit each : sqlExecutionUnits) {
            if (parameters.isEmpty()) {
                log("Actual SQL: {} ::: {}", each.getDataSource(), each.getSql());
            } else {
                log("Actual SQL: {} ::: {} ::: {}", each.getDataSource(), each.getSql(), parameters);
            }
        }
    }
    
    private static void log(final String pattern, final Object... arguments) {
        log.info(pattern, arguments);
    }
}
