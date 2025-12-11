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
import org.apache.calcite.sql.SqlBasicTypeNameSpec;
import org.apache.calcite.sql.SqlDataTypeSpec;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.DataTypeSegment;

import java.util.Objects;
import java.util.Optional;

/**
 * Data type expression converter. 
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataTypeExpressionConverter {
    
    /**
     * Convert data type segment to SQL node.
     *
     * @param segment data type segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final DataTypeSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        return Optional.of(new SqlDataTypeSpec(getSqlBasicTypeNameSpec(segment), SqlParserPos.ZERO));
    }
    
    private static SqlBasicTypeNameSpec getSqlBasicTypeNameSpec(final DataTypeSegment segment) {
        SqlTypeName sqlTypeName = Objects.requireNonNull(SqlTypeName.get(segment.getDataTypeName().toUpperCase()));
        return segment.getDataTypeLength().isPresent()
                ? new SqlBasicTypeNameSpec(sqlTypeName, segment.getDataLength().getPrecision(), SqlParserPos.ZERO)
                : new SqlBasicTypeNameSpec(sqlTypeName, SqlParserPos.ZERO);
    }
}
