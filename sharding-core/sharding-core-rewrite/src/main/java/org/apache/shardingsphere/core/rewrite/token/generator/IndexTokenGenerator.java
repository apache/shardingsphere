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

package org.apache.shardingsphere.core.rewrite.token.generator;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.sql.segment.SQLSegment;
import org.apache.shardingsphere.core.parse.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Index token generator.
 *
 * @author zhangliang
 */
public final class IndexTokenGenerator implements CollectionSQLTokenGenerator<ShardingRule> {
    
    @Override
    public Collection<IndexToken> generateSQLTokens(final SQLStatement sqlStatement, final ShardingRule shardingRule) {
        Collection<IndexToken> result = new LinkedList<>();
        for (SQLSegment each : sqlStatement.getSqlSegments()) {
            if (each instanceof IndexSegment) {
                Optional<IndexToken> indexToken = createIndexToken(sqlStatement, shardingRule, (IndexSegment) each);
                if (indexToken.isPresent()) {
                    result.add(indexToken.get());
                }
            }
        }
        return result;
    }
    
    private Optional<IndexToken> createIndexToken(final SQLStatement sqlStatement, final ShardingRule shardingRule, final IndexSegment indexSegment) {
        Optional<String> tableName = getTableNameOfIndex(sqlStatement, shardingRule, indexSegment);
        return tableName.isPresent()
                ? Optional.of(new IndexToken(indexSegment.getStartIndex(), indexSegment.getStopIndex(), indexSegment.getIndexName(), indexSegment.getQuoteCharacter(), tableName.get()))
                : Optional.<IndexToken>absent();
    }
    
    private Optional<String> getTableNameOfIndex(final SQLStatement sqlStatement, final ShardingRule shardingRule, final IndexSegment indexSegment) {
        if (sqlStatement.getTables().isSingleTable()) {
            return Optional.of(sqlStatement.getTables().getSingleTableName());
        }
        for (TableRule each : shardingRule.getTableRules()) {
            if (indexSegment.getIndexName().equalsIgnoreCase(each.getLogicIndex())) {
                return Optional.of(each.getLogicTable());
            }
        }
        return Optional.absent();
    }
}
