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

package org.apache.shardingsphere.infra.federation.optimizer.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlUserDefinedTypeNameSpec;
import org.apache.calcite.sql.fun.SqlCastFunction;
import org.apache.calcite.sql.fun.SqlPositionFunction;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Function converter.
 */
public final class FunctionConverter implements SQLSegmentConverter<FunctionSegment, SqlBasicCall> {
    
    @Override
    public Optional<SqlBasicCall> convertToSQLNode(final FunctionSegment segment) {
        if ("POSITION".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlPositionFunction(), getSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        if ("CAST".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlCastFunction(), getSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<FunctionSegment> convertToSQLSegment(final SqlBasicCall sqlBasicCall) {
        if (null == sqlBasicCall) {
            return Optional.empty();
        }
        FunctionSegment functionSegment = new FunctionSegment(getStartIndex(sqlBasicCall), getStopIndex(sqlBasicCall), sqlBasicCall.getOperator().getName(), getFunctionText(sqlBasicCall));
        functionSegment.getParameters().addAll(getParameters(sqlBasicCall));
        return Optional.of(functionSegment);
    }
    
    private String getFunctionText(final SqlBasicCall sqlBasicCall) {
        if (null != sqlBasicCall.getOperator() && sqlBasicCall.getOperator() instanceof SqlCastFunction) {
            return sqlBasicCall.toString().replace("`", "");
        }
        return sqlBasicCall.toString();
    }
    
    private List<ExpressionSegment> getParameters(final SqlBasicCall sqlBasicCall) {
        List<ExpressionSegment> result = new ArrayList<>();
        sqlBasicCall.getOperandList().forEach(operand -> {
            if (operand instanceof SqlDataTypeSpec) {
                DataTypeSegment dataTypeSegment = new DataTypeSegment();
                dataTypeSegment.setStartIndex(getStartIndex(operand));
                dataTypeSegment.setStopIndex(getStopIndex(operand));
                dataTypeSegment.setDataTypeName(operand.toString().replace("`", ""));
                result.add(dataTypeSegment);
            } else if (operand instanceof SqlCharStringLiteral) {
                result.add(new LiteralExpressionSegment(getStartIndex(operand), getStopIndex(operand), operand.toString().replace("'", "")));
            }
        });
        return result;
    }
    
    private SqlNode[] getSqlNodes(final Collection<ExpressionSegment> sqlSegments) {
        List<SqlNode> sqlNodes = new ArrayList<>();
        sqlSegments.forEach(sqlSegment -> {
            if (sqlSegment instanceof LiteralExpressionSegment) {
                sqlNodes.add(SqlLiteral.createCharString(((LiteralExpressionSegment) sqlSegment).getLiterals().toString(), SqlParserPos.ZERO));
            }
            if (sqlSegment instanceof DataTypeSegment) {
                sqlNodes.add(new SqlDataTypeSpec(new SqlUserDefinedTypeNameSpec(((DataTypeSegment) sqlSegment).getDataTypeName(), SqlParserPos.ZERO), SqlParserPos.ZERO));
            }
        });
        return sqlNodes.toArray(new SqlNode[0]);
    }
}
