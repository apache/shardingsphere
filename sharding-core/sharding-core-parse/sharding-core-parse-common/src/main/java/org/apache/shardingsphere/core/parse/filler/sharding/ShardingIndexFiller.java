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

package org.apache.shardingsphere.core.parse.filler.sharding;

import com.google.common.base.Optional;
import lombok.Setter;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.filler.api.ShardingTableMetaDataAwareFiller;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.IndexToken;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

/**
 * Index filler for sharding.
 *
 * @author zhangliang
 */
@Setter
public final class ShardingIndexFiller implements SQLSegmentFiller<IndexSegment>, ShardingRuleAwareFiller, ShardingTableMetaDataAwareFiller {
    
    private ShardingRule shardingRule;
    
    private ShardingTableMetaData shardingTableMetaData;
    
    @Override
    public void fill(final IndexSegment sqlSegment, final SQLStatement sqlStatement) {
        Optional<String> tableName = getTableNameOfIndex(sqlSegment.getIndexName(), sqlStatement);
        if (tableName.isPresent()) {
            sqlStatement.getSQLTokens().add(new IndexToken(sqlSegment.getStartIndex(), sqlSegment.getStopIndex(), sqlSegment.getIndexName(), sqlSegment.getQuoteCharacter(), tableName.get()));
        }
    }
    
    private Optional<String> getTableNameOfIndex(final String indexName, final SQLStatement sqlStatement) {
        if (sqlStatement.getTables().isSingleTable()) {
            return Optional.of(sqlStatement.getTables().getSingleTableName());
        }
        for (String each : sqlStatement.getTables().getTableNames()) {
            if (containsIndex(indexName, each)) {
                return Optional.of(each);
            }
        }
        for (String each : shardingTableMetaData.getTables().keySet()) {
            if (containsIndex(indexName, each)) {
                return Optional.of(each);
            }
        }
        return Optional.absent();
    }
    
    private boolean containsIndex(final String indexName, final String tableName) {
        Optional<TableRule> tableRule = shardingRule.findTableRule(tableName);
        return tableRule.isPresent() && indexName.equalsIgnoreCase(tableRule.get().getLogicIndex());
    }
}
