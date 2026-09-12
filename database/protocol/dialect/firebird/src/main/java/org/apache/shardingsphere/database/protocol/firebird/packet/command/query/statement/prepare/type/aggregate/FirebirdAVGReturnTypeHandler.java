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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.FirebirdFunctionReturnTypeHandler;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;

import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Firebird AVG function return type handler.
 */
@RequiredArgsConstructor
public final class FirebirdAVGReturnTypeHandler implements FirebirdFunctionReturnTypeHandler {
    
    private static final Map<Integer, FirebirdBinaryColumnType> COLUMN_TYPE_MAP = new HashMap<>(0, 1F);
    
    static {
        COLUMN_TYPE_MAP.put(Types.BIGINT, FirebirdBinaryColumnType.INT64);
        COLUMN_TYPE_MAP.put(Types.DECIMAL, FirebirdBinaryColumnType.DECIMAL);
        COLUMN_TYPE_MAP.put(Types.NUMERIC, FirebirdBinaryColumnType.NUMERIC);
        COLUMN_TYPE_MAP.put(Types.DOUBLE, FirebirdBinaryColumnType.DOUBLE);
        COLUMN_TYPE_MAP.put(Types.FLOAT, FirebirdBinaryColumnType.DOUBLE);
        COLUMN_TYPE_MAP.put(Types.INTEGER, FirebirdBinaryColumnType.INT64);
        COLUMN_TYPE_MAP.put(Types.SMALLINT, FirebirdBinaryColumnType.INT64);
        COLUMN_TYPE_MAP.put(-6001, FirebirdBinaryColumnType.DEC34);
    }
    
    @Override
    public FirebirdBinaryColumnType getReturnType(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        return FirebirdAggregateReturnTypeConverter.convert(schema, parameters, FirebirdBinaryColumnType.DOUBLE, COLUMN_TYPE_MAP);
    }
}
