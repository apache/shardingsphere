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

package io.shardingsphere.core.parsing.antlr.filler;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * SQL statement filler.
 * 
 * @author duhongjun
 * @author zhangliang
 * 
 * @param <T> type of sql segment
 */
public interface SQLStatementFiller<T extends SQLSegment> {
    
    /**
     * Fill SQL segment to SQL statement.
     *
     * @param sqlSegment SQL segment
     * @param sqlStatement SQL statement
     * @param sql SQL
     * @param shardingRule  databases and tables sharding rule
     * @param shardingTableMetaData sharding table meta data
     */
    void fill(T sqlSegment, SQLStatement sqlStatement, String sql, ShardingRule shardingRule, ShardingTableMetaData shardingTableMetaData);
}
