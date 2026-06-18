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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.projection.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * Expression projection converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionProjectionConverter {
    
    /**
     * Convert expression projection segment to SQL node.
     *
     * @param segment expression projection segment
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final ExpressionProjectionSegment segment) {
        if (null == segment) {
            return Optional.empty();
        }
        Optional<SqlNode> result = ExpressionConverter.convert(segment.getExpr());
        if (result.isPresent() && segment.getAliasName().isPresent()) {
            return Optional.of(new SqlBasicCall(SqlStdOperatorTable.AS, Arrays.asList(result.get(),
                    SqlIdentifier.star(Collections.singletonList(segment.getAliasName().get()), SqlParserPos.ZERO, Collections.singletonList(SqlParserPos.ZERO))), SqlParserPos.ZERO));
        }
        return result;
    }
}
