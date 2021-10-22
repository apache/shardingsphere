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

package org.apache.shardingsphere.infra.optimize.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.infra.optimize.converter.statement.SelectStatementConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Optional;

/**
 * Subquery expression converter.
 */
public final class SubqueryExpressionConverter implements SQLSegmentConverter<SubqueryExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final SubqueryExpressionSegment expression) {
        if (null == expression) {
            return Optional.empty();
        }
        return Optional.of(new SelectStatementConverter().convertToSQLNode(expression.getSubquery().getSelect()));
    }
    
    @Override
    public Optional<SubqueryExpressionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (null == sqlNode) {
            return Optional.empty();
        }
        SelectStatement selectStatement = new SelectStatementConverter().convertToSQLStatement(sqlNode);
        // FIXME subquery projection position returned by the CalCite parser does not contain two brackets
        int startIndex = getStartIndex(sqlNode) - 1;
        int stopIndex = getStopIndex(sqlNode) + 1;
        return Optional.of(new SubqueryExpressionSegment(new SubquerySegment(startIndex, stopIndex, selectStatement)));
    }
}
