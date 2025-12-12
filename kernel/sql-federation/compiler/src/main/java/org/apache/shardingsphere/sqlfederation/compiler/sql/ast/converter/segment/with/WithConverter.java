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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.with;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.SqlWithItem;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type.SelectStatementConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * With converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithConverter {
    
    /**
     * Convert with segment to SQL node list.
     *
     * @param withSegment with segment
     * @param sqlNode SQL node
     * @return SQL node list
     */
    public static Optional<SqlNode> convert(final WithSegment withSegment, final SqlNode sqlNode) {
        return Optional.of(new SqlWith(SqlParserPos.ZERO, convertWithItem(withSegment.getCommonTableExpressions()), sqlNode));
    }
    
    private static SqlNodeList convertWithItem(final Collection<CommonTableExpressionSegment> commonTableExpressionSegments) {
        SqlNodeList result = new SqlNodeList(SqlParserPos.ZERO);
        for (CommonTableExpressionSegment each : commonTableExpressionSegments) {
            SqlIdentifier name = new SqlIdentifier(each.getAliasName().orElse(""), SqlParserPos.ZERO);
            SqlNodeList columns = each.getColumns().isEmpty() ? null : convertColumns(each.getColumns());
            result.add(new SqlWithItem(SqlParserPos.ZERO, name, columns, new SelectStatementConverter().convert(each.getSubquery().getSelect())));
        }
        return result;
    }
    
    private static SqlNodeList convertColumns(final Collection<ColumnSegment> columnSegments) {
        return new SqlNodeList(columnSegments.stream().map(ColumnConverter::convert).collect(Collectors.toList()), SqlParserPos.ZERO);
    }
}
