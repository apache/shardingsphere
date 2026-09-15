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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;

import java.sql.Types;
import java.util.Collection;

/**
 * Firebird Length functions return type handler.
 */
@RequiredArgsConstructor
public final class FirebirdLengthReturnTypeHandler implements FirebirdFunctionReturnTypeHandler {
    
    @Override
    public FirebirdBinaryColumnType getReturnType(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        ExpressionSegment parameter = parameters.iterator().next();
        if (parameter instanceof ColumnSegment) {
            ColumnSegmentBoundInfo columnBoundInfo = ((ColumnSegment) parameter).getColumnBoundInfo();
            ShardingSphereColumn column = schema.getTable(columnBoundInfo.getOriginalTable()).getColumn(columnBoundInfo.getOriginalColumn());
            if (column.getDataType() == Types.LONGVARCHAR
                    || column.getDataType() == Types.CLOB) {
                return FirebirdBinaryColumnType.INT64;
            }
        }
        return FirebirdBinaryColumnType.LONG;
    }
}
