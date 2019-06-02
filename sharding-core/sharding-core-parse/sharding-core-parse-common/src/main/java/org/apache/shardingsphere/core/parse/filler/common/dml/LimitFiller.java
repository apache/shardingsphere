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

package org.apache.shardingsphere.core.parse.filler.common.dml;

import org.apache.shardingsphere.core.parse.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.sql.context.limit.Limit;
import org.apache.shardingsphere.core.parse.sql.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.LimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.NumberLiteralLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.limit.ParameterMarkerLimitValueSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.sql.token.impl.RowCountToken;

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
        if (sqlSegment.getRowCount().isPresent()) {
            fillRowCount(sqlSegment.getRowCount().get(), selectStatement);
        }
        if (sqlSegment.getOffset().isPresent()) {
            fillOffset(sqlSegment.getOffset().get(), selectStatement);
        }
    }
    
    private void fillRowCount(final LimitValueSegment rowCountSegment, final SelectStatement selectStatement) {
        if (rowCountSegment instanceof NumberLiteralLimitValueSegment) {
            int value = ((NumberLiteralLimitValueSegment) rowCountSegment).getValue();
            selectStatement.getLimit().setRowCount(new LimitValue(value, -1, rowCountSegment, false));
            selectStatement.getSQLTokens().add(new RowCountToken(rowCountSegment.getStartIndex(), rowCountSegment.getStopIndex(), value));
        } else {
            selectStatement.getLimit().setRowCount(new LimitValue(-1, ((ParameterMarkerLimitValueSegment) rowCountSegment).getParameterIndex(), rowCountSegment, false));
        }
    }
    
    private void fillOffset(final LimitValueSegment offsetSegment, final SelectStatement selectStatement) {
        if (offsetSegment instanceof NumberLiteralLimitValueSegment) {
            int value = ((NumberLiteralLimitValueSegment) offsetSegment).getValue();
            selectStatement.getLimit().setOffset(new LimitValue(value, -1, offsetSegment, false));
        } else {
            selectStatement.getLimit().setOffset(new LimitValue(-1, ((ParameterMarkerLimitValueSegment) offsetSegment).getParameterIndex(), offsetSegment, false));
        }
    }
}
