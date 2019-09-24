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

package org.apache.shardingsphere.core.optimize.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.statement.CommonOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.InsertOptimizedStatement;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.optimize.sharding.engnie.ShardingSelectOptimizeEngine;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.List;

/**
 * Optimized statement factory.
 *
 * @author zhangliang
 * @author maxiaoguang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OptimizedStatementFactory {
    
    /**
     * Create optimized statement.
     *
     * @param tableMetas table meta data
     * @param sql SQL
     * @param parameters SQL parameters
     * @param sqlStatement SQL statement
     * @return optimized statement
     */
    public static OptimizedStatement newInstance(final TableMetas tableMetas, final String sql, final List<Object> parameters, final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return new ShardingSelectOptimizeEngine().optimize(tableMetas, sql, parameters, (SelectStatement) sqlStatement);
        }
        if (sqlStatement instanceof InsertStatement) {
            return new InsertOptimizedStatement(tableMetas, parameters, (InsertStatement) sqlStatement);
        }
        return new CommonOptimizedStatement(sqlStatement);
    }
}
