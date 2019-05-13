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

package org.apache.shardingsphere.core.parse.filler.dal;

import lombok.Setter;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.filler.api.ShardingRuleAwareFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.impl.TableToken;
import org.apache.shardingsphere.core.parse.sql.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.core.rule.ShardingRule;

/**
 * Show like filler for MySQL.
 *
 * @author zhangliang
 */
@Setter
public final class MySQLShowLikeFiller implements SQLSegmentFiller<ShowLikeSegment>, ShardingRuleAwareFiller {
    
    private ShardingRule shardingRule;
    
    @Override
    public void fill(final ShowLikeSegment sqlSegment, final SQLStatement sqlStatement) {
        String pattern = sqlSegment.getPattern();
        if (shardingRule.findTableRule(pattern).isPresent() || shardingRule.isBroadcastTable(pattern)) {
            sqlStatement.addSQLToken(new TableToken(sqlSegment.getStartIndex(), sqlSegment.getStopIndex(), pattern, QuoteCharacter.NONE));
            sqlStatement.getTables().add(new Table(pattern, null));
        }
    }
}
