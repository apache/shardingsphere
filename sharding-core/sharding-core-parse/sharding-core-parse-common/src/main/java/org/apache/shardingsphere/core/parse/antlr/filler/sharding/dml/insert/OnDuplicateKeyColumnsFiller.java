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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml.insert;

import lombok.Setter;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.old.lexer.token.Literals;
import org.apache.shardingsphere.core.parse.old.parser.exception.SQLParsingException;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * On duplicate key columns filler.
 *
 * @author zhangliang
 */
@Setter
public final class OnDuplicateKeyColumnsFiller implements SQLSegmentFiller<OnDuplicateKeyColumnsSegment>, ShardingRuleAwareFiller {
    
    private ShardingRule shardingRule;
    
    @Override
    public void fill(final OnDuplicateKeyColumnsSegment sqlSegment, final SQLStatement sqlStatement) {
        String tableName = sqlStatement.getTables().getSingleTableName();
        for (ColumnSegment each : sqlSegment.getColumns()) {
            if (shardingRule.isShardingColumn(each.getName(), tableName)) {
                throw new SQLParsingException("INSERT INTO .... ON DUPLICATE KEY UPDATE can not support on sharding column, token is '%s', literals is '%s'.", Literals.IDENTIFIER, each);
            }
        }
    }
}
