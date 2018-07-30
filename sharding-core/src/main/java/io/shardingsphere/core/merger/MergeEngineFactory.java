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

package io.shardingsphere.core.merger;

import io.shardingsphere.core.merger.dal.DALMergeEngine;
import io.shardingsphere.core.merger.dql.DQLMergeEngine;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dal.DALStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

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
     * @param shardingRule sharding rule
     * @param queryResults query results
     * @param sqlStatement SQL statement
     * @param shardingTableMetaData sharding table meta Data
     * @return merge engine instance
     * @throws SQLException SQL exception
     */
    public static MergeEngine newInstance(final ShardingRule shardingRule, final List<QueryResult> queryResults,
                                          final SQLStatement sqlStatement, final ShardingTableMetaData shardingTableMetaData) throws SQLException {
        if (sqlStatement instanceof SelectStatement) {
            return new DQLMergeEngine(queryResults, (SelectStatement) sqlStatement);
        } 
        if (sqlStatement instanceof DALStatement) {
            return new DALMergeEngine(shardingRule, queryResults, (DALStatement) sqlStatement, shardingTableMetaData);
        }
        throw new UnsupportedOperationException(String.format("Cannot support type '%s'", sqlStatement.getType()));
    }
}
