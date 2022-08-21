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
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlDynamicParam;
import org.apache.calcite.sql.SqlFunctionCategory;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlUnresolvedFunction;
import org.apache.calcite.sql.SqlUserDefinedTypeNameSpec;
import org.apache.calcite.sql.fun.SqlCastFunction;
import org.apache.calcite.sql.fun.SqlPositionFunction;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DataTypeSegment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Function converter.
 */
public final class FunctionConverter implements SQLSegmentConverter<FunctionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final FunctionSegment segment) {
        if ("POSITION".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlPositionFunction(), getSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        if ("CAST".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlCastFunction(), getSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        if ("CONCAT".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlUnresolvedFunction(new SqlIdentifier(segment.getFunctionName(), SqlParserPos.ZERO),
                    null, null, null, null, SqlFunctionCategory.USER_DEFINED_FUNCTION), getSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        if ("DATABASE".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlBasicCall(new SqlUnresolvedFunction(new SqlIdentifier(segment.getFunctionName(), SqlParserPos.ZERO),
                    null, null, null, null, SqlFunctionCategory.USER_DEFINED_FUNCTION), getSqlNodes(segment.getParameters()), SqlParserPos.ZERO));
        }
        if ("CURRENT_USER".equalsIgnoreCase(segment.getFunctionName())) {
            return Optional.of(new SqlIdentifier(segment.getFunctionName(), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
    
    private List<SqlNode> getSqlNodes(final Collection<ExpressionSegment> sqlSegments) {
        List<SqlNode> sqlNodes = new ArrayList<>();
        sqlSegments.forEach(each -> {
            if (each instanceof LiteralExpressionSegment) {
                sqlNodes.add(SqlLiteral.createCharString(((LiteralExpressionSegment) each).getLiterals().toString(), SqlParserPos.ZERO));
            }
            if (each instanceof DataTypeSegment) {
                sqlNodes.add(new SqlDataTypeSpec(new SqlUserDefinedTypeNameSpec(((DataTypeSegment) each).getDataTypeName(), SqlParserPos.ZERO), SqlParserPos.ZERO));
            }
            if (each instanceof ParameterMarkerExpressionSegment) {
                sqlNodes.add(new SqlDynamicParam(((ParameterMarkerExpressionSegment) each).getParameterMarkerIndex(), SqlParserPos.ZERO));
            }
        });
        return new ArrayList<>(sqlNodes);
    }
}
