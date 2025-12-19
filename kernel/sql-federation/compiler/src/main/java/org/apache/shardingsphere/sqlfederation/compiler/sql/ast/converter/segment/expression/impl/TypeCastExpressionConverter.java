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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBasicTypeNameSpec;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlTypeNameSpec;
import org.apache.calcite.sql.fun.SqlCastFunction;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.type.SqlTypeNameConverter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Type cast expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TypeCastExpressionConverter {
    
    /**
     * Convert type cast expression to SQL node.
     *
     * @param segment type cast expression
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final TypeCastExpression segment) {
        Optional<SqlNode> expression = ExpressionConverter.convert(segment.getExpression());
        if (!expression.isPresent()) {
            return Optional.empty();
        }
        SqlTypeNameSpec sqlTypeName = new SqlBasicTypeNameSpec(SqlTypeNameConverter.convert(segment.getDataType().toUpperCase()), SqlParserPos.ZERO);
        return Optional.of(new SqlBasicCall(new SqlCastFunction(), Arrays.asList(expression.get(), new SqlDataTypeSpec(sqlTypeName, SqlParserPos.ZERO)), SqlParserPos.ZERO));
    }
}
