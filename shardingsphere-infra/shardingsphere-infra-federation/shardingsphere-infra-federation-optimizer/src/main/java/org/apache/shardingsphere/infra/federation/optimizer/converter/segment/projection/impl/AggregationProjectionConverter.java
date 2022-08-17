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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.projection.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelectKeyword;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Aggregation projection converter. 
 */
public final class AggregationProjectionConverter implements SQLSegmentConverter<AggregationProjectionSegment, SqlBasicCall> {
    
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
    public Optional<SqlBasicCall> convert(final AggregationProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        SqlLiteral functionQuantifier = null;
        List<String> parameters = Splitter.on(",").trimResults().splitToList(
                SQLUtil.getExactlyValue(SQLUtil.getExpressionWithoutOutsideParentheses(segment.getInnerExpression())));
        if (segment instanceof AggregationDistinctProjectionSegment) {
            parameters = Collections.singletonList(((AggregationDistinctProjectionSegment) segment).getDistinctExpression());
            functionQuantifier = SqlLiteral.createSymbol(SqlSelectKeyword.DISTINCT, SqlParserPos.ZERO);
        }
        if (segment.getAlias().isPresent()) {
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, Arrays.asList(new SqlBasicCall(convertOperator(segment.getType().name()),
                    Collections.singletonList(createParametersSqlNode(parameters)), SqlParserPos.ZERO, functionQuantifier).withExpanded(false),
                    SqlIdentifier.star(Collections.singletonList(segment.getAlias().get()), SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))), SqlParserPos.ZERO));
        }
        return Optional.of((SqlBasicCall) new SqlBasicCall(convertOperator(segment.getType().name()),
                Collections.singletonList(createParametersSqlNode(parameters)), SqlParserPos.ZERO, functionQuantifier).withExpanded(false));
    }
    
    private SqlAggFunction convertOperator(final String operator) {
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
    
    private SqlNode createParametersSqlNode(final List<String> parameters) {
        if (1 == parameters.size()) {
            try {
                Long.parseLong(parameters.get(0));
                return SqlLiteral.createExactNumeric(parameters.get(0), SqlParserPos.ZERO);
            } catch (NumberFormatException ignored) {
            }
        }
        return SqlIdentifier.star(parameters, SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO));
    }
}
