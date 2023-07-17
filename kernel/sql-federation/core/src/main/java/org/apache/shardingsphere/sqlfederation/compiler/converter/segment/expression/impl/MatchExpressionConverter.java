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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.MatchAgainstExpression;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Match expression converter.
 */
public final class MatchExpressionConverter implements SQLSegmentConverter<MatchAgainstExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final MatchAgainstExpression segment) {
        List<SqlNode> column = new LinkedList<>();
        column.add(new SqlIdentifier(segment.getColumnName().getIdentifier().getValue(), SqlParserPos.ZERO));
        SqlNode against = new ExpressionConverter().convert(segment.getExpr()).get();
        String matchSearchModifier = matchSearchModifierExtract(segment.getMatchSearchModifier());
        SqlNode searchModifier = SqlLiteral.createCharString(matchSearchModifier, SqlParserPos.ZERO);
        int sqlNodeSize = column.size() + 2;
        SqlNode[] sqlNodes = new SqlNode[sqlNodeSize];
        for (int i = 0; i < column.size(); i++) {
            sqlNodes[i] = column.get(i);
        }
        sqlNodes[sqlNodeSize - 2] = against;
        sqlNodes[sqlNodeSize - 1] = searchModifier;
        SqlOperator operator = SQLExtensionOperatorTable.MATCH_AGAINST;
        return Optional.of(new SqlBasicCall(operator, sqlNodes, SqlParserPos.ZERO));
    }
    
    private String matchSearchModifierExtract(final String matchSearchModifier) {
        if ("INNATURALLANGUAGEMODE".equalsIgnoreCase(matchSearchModifier)) {
            return "IN NATURAL LANGUAGE MODE";
        }
        if ("INNATURALLANGUAGEMODEWITHQUERYEXPANSION".equalsIgnoreCase(matchSearchModifier)) {
            return "IN NATURAL LANGUAGE MODE WITH QUERY EXPANSION";
        }
        if ("WITHQUERYEXPANSION".equalsIgnoreCase(matchSearchModifier)) {
            return "WITH QUERY EXPANSION";
        }
        return null;
    }
}
