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

package org.apache.shardingsphere.core.parse.antlr.filler.common.dql;

import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.LiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.limit.PlaceholderLimitValueSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OffsetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.RowCountToken;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.Limit;
import org.apache.shardingsphere.core.parse.old.parser.context.limit.LimitValue;

/**
 * Limit filler.
 *
 * @author duhongjun
 */
public final class LimitFiller implements SQLSegmentFiller<LimitSegment> {
    
    @Override
    public void fill(final LimitSegment sqlSegment, final SQLStatement sqlStatement) {
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
