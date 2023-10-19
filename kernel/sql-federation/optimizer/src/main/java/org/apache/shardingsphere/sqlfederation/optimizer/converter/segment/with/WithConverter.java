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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.with;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.SqlWithItem;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.select.SelectStatementConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * With converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WithConverter {
    
    /**
     * Convert with segment to sql node list.
     *
     * @param withSegment with segment
     * @param sqlNode sql node
     * @return sql node list
     */
    public static Optional<SqlNode> convert(final WithSegment withSegment, final SqlNode sqlNode) {
        SqlIdentifier name = new SqlIdentifier(withSegment.getCommonTableExpressions().iterator().next().getIdentifier().getValue(), SqlParserPos.ZERO);
        SqlNode selectSubquery = new SelectStatementConverter().convert(withSegment.getCommonTableExpressions().iterator().next().getSubquery().getSelect());
        Collection<ColumnSegment> collectionColumns = withSegment.getCommonTableExpressions().iterator().next().getColumns();
        Collection<SqlNode> convertedColumns;
        SqlNodeList columns = null;
        if (!collectionColumns.isEmpty()) {
            convertedColumns = collectionColumns.stream().map(ExpressionConverter::convert).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            columns = new SqlNodeList(convertedColumns, SqlParserPos.ZERO);
        }
        SqlWithItem sqlWithItem = new SqlWithItem(SqlParserPos.ZERO, name, columns, selectSubquery);
        SqlNodeList sqlWithItems = new SqlNodeList(SqlParserPos.ZERO);
        sqlWithItems.add(sqlWithItem);
        SqlWith sqlWith = new SqlWith(SqlParserPos.ZERO, sqlWithItems, sqlNode);
        SqlNodeList result = new SqlNodeList(SqlParserPos.ZERO);
        result.add(sqlWith);
        return Optional.of(result);
    }
}
