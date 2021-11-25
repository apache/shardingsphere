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

import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.infra.federation.optimizer.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Optional;

/**
 * Literal expression converter.
 */
public final class LiteralExpressionConverter implements SQLSegmentConverter<LiteralExpressionSegment, SqlNode> {
    
    @Override
    public Optional<SqlNode> convertToSQLNode(final LiteralExpressionSegment segment) {
        if (Integer.class == segment.getLiterals().getClass()) {
            return Optional.of(SqlLiteral.createExactNumeric(String.valueOf(segment.getLiterals()), SqlParserPos.ZERO));
        }
        if (String.class == segment.getLiterals().getClass()) {
            return Optional.of(SqlLiteral.createCharString((String) segment.getLiterals(), SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<LiteralExpressionSegment> convertToSQLSegment(final SqlNode sqlNode) {
        if (sqlNode instanceof SqlLiteral) {
            SqlLiteral sqlLiteral = (SqlLiteral) sqlNode;
            return Optional.of(new LiteralExpressionSegment(getStartIndex(sqlLiteral), getStopIndex(sqlLiteral), SQLUtil.getExactlyValue(sqlLiteral.toValue())));
        }
        return Optional.empty();
    }
}
