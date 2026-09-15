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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.string;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.FirebirdFunctionReturnTypeHandler;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.FirebirdFunctionType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Firebird Upper and Lower functions return type handler.
 */
@RequiredArgsConstructor
public final class FirebirdUpperLowerReturnTypeHandler implements FirebirdFunctionReturnTypeHandler {
    
    private static final Map<Integer, FirebirdBinaryColumnType> COLUMN_TYPE_MAP = new HashMap<>(0, 1F);
    
    static {
        COLUMN_TYPE_MAP.put(Types.VARCHAR, FirebirdBinaryColumnType.VARYING);
        COLUMN_TYPE_MAP.put(Types.LONGVARCHAR, FirebirdBinaryColumnType.BLOB_SUBTYPE_TEXT);
        COLUMN_TYPE_MAP.put(Types.CLOB, FirebirdBinaryColumnType.BLOB_SUBTYPE_TEXT);
        COLUMN_TYPE_MAP.put(Types.CHAR, FirebirdBinaryColumnType.VARYING);
        // TODO add proper support for (VAR)CHAR CHARACTER SET OCTETS
        COLUMN_TYPE_MAP.put(Types.BINARY, FirebirdBinaryColumnType.VARYING);
        COLUMN_TYPE_MAP.put(Types.VARBINARY, FirebirdBinaryColumnType.VARYING);
    }
    
    @Override
    public FirebirdBinaryColumnType getReturnType(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        ExpressionSegment parameter = parameters.iterator().next();
        if (parameter instanceof ColumnSegment) {
            ColumnSegmentBoundInfo columnBoundInfo = ((ColumnSegment) parameter).getColumnBoundInfo();
            ShardingSphereColumn column = schema.getTable(columnBoundInfo.getOriginalTable()).getColumn(columnBoundInfo.getOriginalColumn());
            return COLUMN_TYPE_MAP.get(column.getDataType());
        } else if (parameter instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) parameter).getLiterals();
            if (literals instanceof String) {
                return FirebirdBinaryColumnType.VARYING;
            }
        } else if (parameter instanceof FunctionSegment) {
            FunctionSegment functionSegment = (FunctionSegment) parameter;
            return FirebirdFunctionType.getReturnType(functionSegment.getFunctionName(), schema, functionSegment.getParameters());
        }
        // TODO replace with TEXT when CHAR type is fixed
        return FirebirdBinaryColumnType.VARYING;
    }
}
