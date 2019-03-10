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

package org.apache.shardingsphere.core.parsing.antlr.filler.common.segment.impl.dql;

import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parsing.antlr.filler.common.SQLSegmentCommonFiller;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.LimitSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.LiteralLimitValueSegment;
import org.apache.shardingsphere.core.parsing.antlr.sql.segment.limit.PlaceholderLimitValueSegment;
import org.apache.shardingsphere.core.parsing.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parsing.parser.context.limit.LimitValue;
import org.apache.shardingsphere.core.parsing.parser.sql.SQLStatement;
import org.apache.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parsing.parser.token.OffsetToken;
import org.apache.shardingsphere.core.parsing.parser.token.RowCountToken;

/**
 * Limit filler.
 *
 * @author duhongjun
 */
public final class LimitFiller implements SQLSegmentCommonFiller<LimitSegment> {
    
    @Override
    public void fill(final LimitSegment sqlSegment, final SQLStatement sqlStatement, final String sql, final ShardingTableMetaData shardingTableMetaData) {
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
            selectStatement.getSQLTokens().add(new OffsetToken(offsetSegment.getStartIndex(), value));
        } else {
            selectStatement.getLimit().setOffset(new LimitValue(-1, ((PlaceholderLimitValueSegment) offsetSegment).getParameterIndex(), false));
        }
    }
    
    private void setRowCount(final LimitValueSegment rowCountSegment, final SelectStatement selectStatement) {
        if (rowCountSegment instanceof LiteralLimitValueSegment) {
            int value = ((LiteralLimitValueSegment) rowCountSegment).getValue();
            selectStatement.getLimit().setRowCount(new LimitValue(value, -1, false));
            selectStatement.getSQLTokens().add(new RowCountToken(rowCountSegment.getStartIndex(), value));
        } else {
            selectStatement.getLimit().setRowCount(new LimitValue(-1, ((PlaceholderLimitValueSegment) rowCountSegment).getParameterIndex(), false));
        }
    }
}
