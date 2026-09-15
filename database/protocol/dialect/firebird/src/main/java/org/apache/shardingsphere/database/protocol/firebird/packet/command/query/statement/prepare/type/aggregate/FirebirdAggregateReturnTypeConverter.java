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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.aggregate;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.FirebirdFunctionType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.util.Collection;
import java.util.Map;

/**
 * Firebird Aggregate functions return type converter.
 */
@RequiredArgsConstructor
public final class FirebirdAggregateReturnTypeConverter {
    
    /**
     * Convert to given return types based on parameter.
     *
     * @param schema used schema
     * @param parameters function parameters
     * @param parameterMarkerType returned type when given  is marker
     * @param columnTypeMap column types bindings map
     * @return return type of function
     */
    public static FirebirdBinaryColumnType convert(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters, final FirebirdBinaryColumnType parameterMarkerType,
                                                   final Map<Integer, FirebirdBinaryColumnType> columnTypeMap) {
        ExpressionSegment parameter = parameters.iterator().next();
        if (parameter instanceof ColumnSegment) {
            ColumnSegmentBoundInfo columnBoundInfo = ((ColumnSegment) parameter).getColumnBoundInfo();
            ShardingSphereColumn column = schema.getTable(columnBoundInfo.getOriginalTable()).getColumn(columnBoundInfo.getOriginalColumn());
            return columnTypeMap.get(column.getDataType());
        } else if (parameter instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) parameter).getLiterals();
            return FirebirdBinaryColumnType.valueOfJavaType(literals);
        } else if (parameter instanceof ParameterMarkerExpressionSegment) {
            return parameterMarkerType;
        } else if (parameter instanceof FunctionSegment) {
            FunctionSegment functionSegment = (FunctionSegment) parameter;
            return FirebirdFunctionType.getReturnType(functionSegment.getFunctionName(), schema, functionSegment.getParameters());
        }
        return FirebirdBinaryColumnType.LONG;
    }
}
