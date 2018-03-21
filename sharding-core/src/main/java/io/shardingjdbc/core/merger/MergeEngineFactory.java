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

package io.shardingjdbc.core.merger;

import io.shardingjdbc.core.merger.dal.DALMergeEngine;
import io.shardingjdbc.core.merger.dql.DQLMergeEngine;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dal.DALStatement;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.SQLException;
import java.util.List;

/**
 * Result merge engine factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MergeEngineFactory {
    
    /**
     * Create merge engine instance.
     *
     * @param shardingRule sharding rule
     * @param queryResults query results
     * @param sqlStatement SQL statement
     * @return merge engine instance
     * @throws SQLException SQL exception
     */
    public static MergeEngine newInstance(final ShardingRule shardingRule, final List<QueryResult> queryResults, final SQLStatement sqlStatement) throws SQLException {
        if (sqlStatement instanceof SelectStatement) {
            return new DQLMergeEngine(queryResults, (SelectStatement) sqlStatement);
        } 
        if (sqlStatement instanceof DALStatement) {
            return new DALMergeEngine(shardingRule, queryResults, (DALStatement) sqlStatement);
        }
        throw new UnsupportedOperationException(String.format("Cannot support type '%s'", sqlStatement.getType()));
    }
}
