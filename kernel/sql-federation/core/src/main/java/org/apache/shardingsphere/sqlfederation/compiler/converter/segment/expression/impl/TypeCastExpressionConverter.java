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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBasicTypeNameSpec;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlTypeNameSpec;
import org.apache.calcite.sql.fun.SqlCastFunction;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.SQLSegmentConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;

import java.util.Arrays;
import java.util.Optional;

/**
 * Type cast expression converter.
 */
public final class TypeCastExpressionConverter implements SQLSegmentConverter<TypeCastExpression, SqlNode> {
    
    @Override
    public Optional<SqlNode> convert(final TypeCastExpression segment) {
        Optional<SqlNode> expression = new ExpressionConverter().convert(segment.getExpression());
        if (!expression.isPresent()) {
            return Optional.empty();
        }
        SqlTypeNameSpec sqlTypeName = new SqlBasicTypeNameSpec(SqlTypeName.valueOf(segment.getDataType().toUpperCase()), SqlParserPos.ZERO);
        return Optional.of(new SqlBasicCall(new SqlCastFunction(), Arrays.asList(expression.get(), new SqlDataTypeSpec(sqlTypeName, SqlParserPos.ZERO)), SqlParserPos.ZERO));
    }
}
