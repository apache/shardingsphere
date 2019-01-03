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

package io.shardingsphere.core.parsing.antlr.filler.impl.dql;

import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.parsing.antlr.filler.SQLStatementFiller;
import io.shardingsphere.core.parsing.antlr.sql.segment.limit.LimitSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.limit.LimitValueSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.limit.LiteralLimitValueSegment;
import io.shardingsphere.core.parsing.antlr.sql.segment.limit.PlaceholderLimitValueSegment;
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
public final class LimitFiller implements SQLStatementFiller<LimitSegment> {
    
    @Override
    public void fill(final LimitSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingRule shardingRule, final ShardingTableMetaData shardingTableMetaData) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        selectStatement.setLimit(new Limit());
        if (sqlSegment.getOffset().isPresent()) {
            setOffset(sqlSegment.getOffset().get(), selectStatement);
        }
        setRowCount(sqlSegment.getRowCount(), selectStatement);
    }
    
    private void setOffset(final LimitValueSegment offsetSegment, final SelectStatement selectStatement) {
        if (offsetSegment instanceof LiteralLimitValueSegment) {
            int value = ((LiteralLimitValueSegment) offsetSegment).getValue();
            selectStatement.getLimit().setOffset(new LimitValue(value, -1, false));
            selectStatement.getSQLTokens().add(new OffsetToken(offsetSegment.getBeginPosition(), value));
        } else {
            selectStatement.getLimit().setOffset(new LimitValue(-1, ((PlaceholderLimitValueSegment) offsetSegment).getParameterIndex(), false));
        }
    }
    
    private void setRowCount(final LimitValueSegment rowCountSegment, final SelectStatement selectStatement) {
        if (rowCountSegment instanceof LiteralLimitValueSegment) {
            int value = ((LiteralLimitValueSegment) rowCountSegment).getValue();
            selectStatement.getLimit().setRowCount(new LimitValue(value, -1, false));
            selectStatement.getSQLTokens().add(new RowCountToken(rowCountSegment.getBeginPosition(), value));
        } else {
            selectStatement.getLimit().setRowCount(new LimitValue(-1, ((PlaceholderLimitValueSegment) rowCountSegment).getParameterIndex(), false));
        }
    }
}
