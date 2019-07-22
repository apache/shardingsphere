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

package org.apache.shardingsphere.core.optimize.api.engine;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.api.statement.OptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rule.BaseRule;

import java.util.List;

/**
 * Optimize engine.
 *
 * @author maxiaoguang
 * @author panjuan
 * 
 * @param <R> type of rule
 * @param <T> type of SQL statement
 */
public interface OptimizeEngine<R extends BaseRule, T extends SQLStatement> {
    
    /**
     * Optimize.
     * 
     * @param rule rule
     * @param shardingTableMetaData table meta data
     * @param sql SQL
     * @param parameters SQL parameters
     * @param sqlStatement SQL statement
     * @return optimized statement
     */
    OptimizedStatement optimize(R rule, ShardingTableMetaData shardingTableMetaData, String sql, List<Object> parameters, T sqlStatement);
}
