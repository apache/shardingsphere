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

package org.apache.shardingsphere.infra.optimize.converter.segment.projection.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.apache.calcite.sql.SqlAggFunction;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlSelectKeyword;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.optimize.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.constant.QuoteCharacter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    public Optional<SqlBasicCall> convertToSQLNode(final AggregationProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        SqlLiteral functionQuantifier = null;
        List<String> parameters = Splitter.on(",").trimResults().splitToList(SQLUtil.getExpressionWithoutOutsideParentheses(segment.getInnerExpression()));
        if (segment instanceof AggregationDistinctProjectionSegment) {
            parameters = Collections.singletonList(((AggregationDistinctProjectionSegment) segment).getDistinctExpression());
            functionQuantifier = SqlLiteral.createSymbol(SqlSelectKeyword.DISTINCT, SqlParserPos.ZERO);
        }
        if (segment.getAlias().isPresent()) {
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, new SqlNode[]{new SqlBasicCall(convertOperator(segment.getType().name()), 
                    new SqlNode[]{SqlIdentifier.star(parameters, SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))}, SqlParserPos.ZERO, false, functionQuantifier),
                    SqlIdentifier.star(Collections.singletonList(segment.getAlias().get()), SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))}, SqlParserPos.ZERO));
        }
        return Optional.of(new SqlBasicCall(convertOperator(segment.getType().name()), 
                new SqlNode[]{SqlIdentifier.star(parameters, SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))}, SqlParserPos.ZERO, false, functionQuantifier));
    }
    
    @Override
    public Optional<AggregationProjectionSegment> convertToSQLSegment(final SqlBasicCall sqlBasicCall) {
        if (null == sqlBasicCall) {
            return Optional.empty();
        }
        if (isAsOperatorAggregationType(sqlBasicCall)) {
            SqlBasicCall subSqlBasicCall = (SqlBasicCall) sqlBasicCall.getOperandList().get(0);
            AggregationType aggregationType = AggregationType.valueOf(subSqlBasicCall.getOperator().getName());
            String innerExpression = getInnerExpression(subSqlBasicCall);
            AliasSegment aliasSegment = new AliasSegment(getStartIndex(sqlBasicCall.getOperandList().get(1)), getStopIndex(sqlBasicCall.getOperandList().get(1)), 
                    new IdentifierValue(((SqlIdentifier) sqlBasicCall.getOperandList().get(1)).names.get(0)));
            if (null != subSqlBasicCall.getFunctionQuantifier() && SqlSelectKeyword.DISTINCT == subSqlBasicCall.getFunctionQuantifier().getValue()) {
                return Optional.of(getAggregationDistinctProjectionSegment(subSqlBasicCall, aggregationType, aliasSegment));
            }
            AggregationProjectionSegment aggregationProjectionSegment = new AggregationProjectionSegment(getStartIndex(subSqlBasicCall), getStopIndex(subSqlBasicCall),
                    aggregationType, innerExpression);
            aggregationProjectionSegment.setAlias(aliasSegment);
            return Optional.of(aggregationProjectionSegment);
        }
        AggregationType aggregationType = AggregationType.valueOf(sqlBasicCall.getOperator().getName());
        if (null != sqlBasicCall.getFunctionQuantifier() && SqlSelectKeyword.DISTINCT == sqlBasicCall.getFunctionQuantifier().getValue()) {
            return Optional.of(getAggregationDistinctProjectionSegment(sqlBasicCall, aggregationType, null));
        }
        String innerExpression = getInnerExpression(sqlBasicCall);
        return Optional.of(new AggregationProjectionSegment(getStartIndex(sqlBasicCall), getStopIndex(sqlBasicCall), aggregationType, innerExpression));
    }
    
    private AggregationDistinctProjectionSegment getAggregationDistinctProjectionSegment(final SqlBasicCall sqlBasicCall, final AggregationType aggregationType, final AliasSegment aliasSegment) {
        String innerExpression = getInnerExpression(sqlBasicCall, SqlSelectKeyword.DISTINCT);
        String distinctParams = sqlBasicCall.getOperandList().stream().map(SqlNode::toString).collect(Collectors.joining(", "));
        AggregationDistinctProjectionSegment aggregationDistinctProjectionSegment = new AggregationDistinctProjectionSegment(getStartIndex(sqlBasicCall), getStopIndex(sqlBasicCall), 
                aggregationType, innerExpression, distinctParams);
        aggregationDistinctProjectionSegment.setAlias(aliasSegment);
        return aggregationDistinctProjectionSegment;
    }
    
    private String getInnerExpression(final SqlBasicCall sqlBasicCall, final SqlSelectKeyword selectKeyword) {
        if (selectKeyword == null) {
            return getInnerExpression(sqlBasicCall);
        }
        String params = sqlBasicCall.getOperandList().stream().map(SqlNode::toString).collect(Collectors.joining(", "));
        return QuoteCharacter.PARENTHESES.wrap(selectKeyword.toString() + " " + params);
    }
    
    private String getInnerExpression(final SqlBasicCall sqlBasicCall) {
        String params = sqlBasicCall.getOperandList().stream().map(SqlNode::toString).collect(Collectors.joining(", "));
        return QuoteCharacter.PARENTHESES.wrap(params);
    }
    
    private SqlAggFunction convertOperator(final String operator) {
        Preconditions.checkState(REGISTRY.containsKey(operator), "Unsupported SQL operator: `%s`", operator);
        return REGISTRY.get(operator);
    }
    
    /**
     * Judge whether sqlBasicCall is as operator aggregation type or not.
     * @param sqlBasicCall sqlBasicCall 
     * @return whether sqlBasicCall is as operator aggregation type or not
     */
    public static boolean isAsOperatorAggregationType(final SqlBasicCall sqlBasicCall) {
        return null != sqlBasicCall.getOperator() && SqlKind.AS == sqlBasicCall.getOperator().getKind()
                && sqlBasicCall.getOperandList().get(0) instanceof SqlBasicCall
                && AggregationType.isAggregationType(((SqlBasicCall) sqlBasicCall.getOperandList().get(0)).getOperator().getName());
    }
}
