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

package io.shardingsphere.core.parsing.antlr.filler.impl;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.LimitSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.SQLSegment;
import io.shardingsphere.core.parsing.parser.context.limit.Limit;
import io.shardingsphere.core.parsing.parser.context.limit.LimitValue;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.OffsetToken;
import io.shardingsphere.core.parsing.parser.token.RowCountToken;
import io.shardingsphere.core.rule.ShardingRule;

/**
 * Limit filler.
 *
 * @author duhongjun
 */
public final class LimitFiller implements SQLStatementFiller {
    
    @Override
    public void fill(final SQLSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        LimitSegment limitSegment = (LimitSegment) sqlSegment;
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        Limit limit = new Limit(limitSegment.getDatabaseType());
        selectStatement.setLimit(limit);
        if (limitSegment.getOffset().isPresent()) {
            limit.setOffset(new LimitValue(limitSegment.getOffset().get().getValue(), limitSegment.getOffset().get().getIndex(), false));
            if (-1 == limitSegment.getOffset().get().getIndex()) {
                selectStatement.getSQLTokens().add(new OffsetToken(limitSegment.getOffset().get().getBeginPosition(), limitSegment.getOffset().get().getValue()));
            }
        }
        limit.setRowCount(new LimitValue(limitSegment.getRowCount().get().getValue(), limitSegment.getRowCount().get().getIndex(), false));
        if (-1 == limitSegment.getRowCount().get().getIndex()) {
            selectStatement.getSQLTokens().add(new RowCountToken(limitSegment.getRowCount().get().getBeginPosition(), limitSegment.getRowCount().get().getValue()));
        }
    }
}
