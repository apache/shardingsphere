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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.bitwise;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.FirebirdBinaryColumnType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.prepare.type.FirebirdFunctionReturnTypeHandler;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;

import java.math.BigInteger;
import java.util.Collection;

/**
 * Firebird SHL and SHR functions return type handler.
 */
@RequiredArgsConstructor
public final class FirebirdShiftReturnTypeHandler implements FirebirdFunctionReturnTypeHandler {
    
    @Override
    public FirebirdBinaryColumnType getReturnType(final ShardingSphereSchema schema, final Collection<ExpressionSegment> parameters) {
        ExpressionSegment parameter = parameters.iterator().next();
        // TODO add support for INT128 column type
        if (parameter instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) parameter).getLiterals();
            if (literals instanceof BigInteger) {
                return FirebirdBinaryColumnType.INT128;
            }
            return FirebirdBinaryColumnType.INT64;
        }
        return FirebirdBinaryColumnType.INT64;
    }
}
