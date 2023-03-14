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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.projection.impl;

import com.google.common.base.Preconditions;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelectKeyword;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Aggregation projection converter. 
 */
public final class AggregationProjectionConverter implements SQLSegmentConverter<AggregationProjectionSegment, SqlNode> {
    
    private static final Map<String, SqlAggFunction> REGISTRY = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        register(SqlStdOperatorTable.MAX);
        register(SqlStdOperatorTable.MIN);
        register(SqlStdOperatorTable.SUM);
        register(SqlStdOperatorTable.COUNT);
        register(SqlStdOperatorTable.AVG);
        register(SqlStdOperatorTable.BIT_XOR);
    }
    
    private static void register(final SqlAggFunction sqlAggFunction) {
        REGISTRY.put(sqlAggFunction.getName(), sqlAggFunction);
    }
    
    @Override
    public Optional<SqlNode> convert(final AggregationProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        SqlLiteral functionQuantifier = segment instanceof AggregationDistinctProjectionSegment ? SqlLiteral.createSymbol(SqlSelectKeyword.DISTINCT, SqlParserPos.ZERO) : null;
        SqlAggFunction operator = convertOperator(segment.getType().name());
        List<SqlNode> params = convertParameters(segment.getParameters(), segment.getInnerExpression());
        SqlBasicCall sqlBasicCall = new SqlBasicCall(operator, params, SqlParserPos.ZERO, functionQuantifier);
        if (segment.getAlias().isPresent()) {
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, Arrays.asList(sqlBasicCall,
                    SqlIdentifier.star(Collections.singletonList(segment.getAlias().get()), SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))), SqlParserPos.ZERO));
        }
        return Optional.of(sqlBasicCall);
    }
    
    private SqlAggFunction convertOperator(final String operator) {
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
    
    private List<SqlNode> convertParameters(final Collection<ExpressionSegment> params, final String innerExpression) {
        if (innerExpression.contains("*")) {
            return Collections.singletonList(SqlIdentifier.star(SqlParserPos.ZERO));
        }
        List<SqlNode> result = new LinkedList<>();
        ExpressionConverter expressionConverter = new ExpressionConverter();
        for (ExpressionSegment each : params) {
            expressionConverter.convert(each).ifPresent(result::add);
        }
        return result;
    }
}
