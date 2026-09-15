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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.cast;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.FirebirdFunctionReturnTypeHandler;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Firebird Cast function return type handler.
 */
@RequiredArgsConstructor
public final class FirebirdCastReturnTypeHandler implements FirebirdFunctionReturnTypeHandler {
    
    private static final Map<String, FirebirdBinaryColumnType> COLUMN_TYPE_MAP = new HashMap<>(0, 1F);
    
    static {
        COLUMN_TYPE_MAP.put("SMALLINT", FirebirdBinaryColumnType.SHORT);
        COLUMN_TYPE_MAP.put("INTEGER", FirebirdBinaryColumnType.LONG);
        COLUMN_TYPE_MAP.put("BIGINT", FirebirdBinaryColumnType.INT64);
        COLUMN_TYPE_MAP.put("INT64", FirebirdBinaryColumnType.INT64);
        COLUMN_TYPE_MAP.put("INT128", FirebirdBinaryColumnType.INT128);
        COLUMN_TYPE_MAP.put("NUMERIC", FirebirdBinaryColumnType.NUMERIC);
        COLUMN_TYPE_MAP.put("DECIMAL", FirebirdBinaryColumnType.DECIMAL);
        COLUMN_TYPE_MAP.put("FLOAT", FirebirdBinaryColumnType.FLOAT);
        COLUMN_TYPE_MAP.put("DOUBLE PRECISION", FirebirdBinaryColumnType.DOUBLE);
        COLUMN_TYPE_MAP.put("BOOLEAN", FirebirdBinaryColumnType.BOOLEAN);
        COLUMN_TYPE_MAP.put("DECFLOAT", FirebirdBinaryColumnType.DEC34);
        COLUMN_TYPE_MAP.put("DECFLOAT(16)", FirebirdBinaryColumnType.DEC16);
        COLUMN_TYPE_MAP.put("DECFLOAT(34)", FirebirdBinaryColumnType.DEC34);
        COLUMN_TYPE_MAP.put("DATE", FirebirdBinaryColumnType.DATE);
        COLUMN_TYPE_MAP.put("TIME", FirebirdBinaryColumnType.TIME);
        COLUMN_TYPE_MAP.put("TIMESTAMP", FirebirdBinaryColumnType.TIMESTAMP);
        COLUMN_TYPE_MAP.put("TIME WITH TIME ZONE", FirebirdBinaryColumnType.TIME_TZ);
        COLUMN_TYPE_MAP.put("TIMESTAMP WITH TIME ZONE", FirebirdBinaryColumnType.TIMESTAMP_TZ);
        // replace with TEXT when CHAR is fixed
        COLUMN_TYPE_MAP.put("CHAR", FirebirdBinaryColumnType.VARYING);
        COLUMN_TYPE_MAP.put("VARCHAR", FirebirdBinaryColumnType.VARYING);
        COLUMN_TYPE_MAP.put("BLOB", FirebirdBinaryColumnType.BLOB);
        COLUMN_TYPE_MAP.put("BLOB SUB_TYPE BINARY", FirebirdBinaryColumnType.BLOB);
        COLUMN_TYPE_MAP.put("BLOB SUB_TYPE TEXT", FirebirdBinaryColumnType.BLOB_SUBTYPE_TEXT);
    }
    
    @Override
    public FirebirdBinaryColumnType getReturnType(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        Iterator<ExpressionSegment> iterator = parameters.iterator();
        ExpressionSegment parameter = iterator.next();
        try {
            parameter = iterator.next();
        } catch (final NoSuchElementException ignored) {
        }
        // TODO add support for custom domains
        if (parameter instanceof DataTypeSegment) {
            String typeName = ((DataTypeSegment) parameter).getDataTypeName();
            return COLUMN_TYPE_MAP.get(typeName);
        }
        return FirebirdBinaryColumnType.LONG;
    }
}
