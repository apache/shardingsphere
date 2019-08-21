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

package org.apache.shardingsphere.core.merge;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.execute.sql.execute.result.QueryResult;
import org.apache.shardingsphere.core.merge.dal.DALMergeEngine;
import org.apache.shardingsphere.core.merge.dql.DQLMergeEngine;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dal.DALStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.spi.database.DatabaseType;

import java.sql.SQLException;
import java.util.List;

/**
 * Result merge engine factory.
 *
 * @author zhangliang
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergeEngineFactory {
    
    /**
     * Create merge engine instance.
     *
     * @param databaseType database type
     * @param shardingRule sharding rule
     * @param routeResult SQL route result
     * @param tableMetas sharding table meta Data
     * @param queryResults query results
     * @return merge engine instance
     * @throws SQLException SQL exception
     */
    public static MergeEngine newInstance(final DatabaseType databaseType, final ShardingRule shardingRule,
                                          final SQLRouteResult routeResult, final TableMetas tableMetas, final List<QueryResult> queryResults) throws SQLException {
        if (routeResult.getShardingStatement() instanceof ShardingSelectOptimizedStatement) {
            return new DQLMergeEngine(databaseType, routeResult, queryResults);
        } 
        if (routeResult.getShardingStatement().getSQLStatement() instanceof DALStatement) {
            return new DALMergeEngine(shardingRule, queryResults, (DALStatement) routeResult.getShardingStatement().getSQLStatement(), tableMetas);
        }
        return new TransparentMergeEngine(queryResults);
    }
}
