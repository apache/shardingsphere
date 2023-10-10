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

package org.apache.shardingsphere.sqlfederation.compiler.converter.segment.with;

import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlWithItem;
import org.apache.calcite.sql.SqlWith;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.WithSegment;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.select.SelectStatementConverter;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * With converter.
 */
public final class WithConverter {
    
    /**
     * Convert the given WithSegment and query into an SqlNodeList.
     *
     * @param withSegment with segment
     * @param query SqlNode
     * @return SqlNodeList
     */
    public Optional<SqlNode> convert(final WithSegment withSegment, final SqlNode query) {
        SqlIdentifier name = new SqlIdentifier(withSegment.getCommonTableExpressions().iterator().next().getIdentifier().getValue(), SqlParserPos.ZERO);
        SqlNode selectSubquery = new SelectStatementConverter().convert(withSegment.getCommonTableExpressions().iterator().next().getSubquery().getSelect());
        ExpressionConverter converter = new ExpressionConverter();
        Collection<ColumnSegment> collectionColumns = withSegment.getCommonTableExpressions().iterator().next().getColumns();
        Collection<SqlNode> convertedColumns;
        SqlNodeList columns = null;
        if (!collectionColumns.isEmpty()) {
            convertedColumns = collectionColumns.stream().map(converter::convert).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
            columns = new SqlNodeList(convertedColumns, SqlParserPos.ZERO);
        }
        SqlWithItem sqlWithItem = new SqlWithItem(SqlParserPos.ZERO, name, columns, selectSubquery);
        SqlNodeList sqlWithItems = new SqlNodeList(SqlParserPos.ZERO);
        sqlWithItems.add(sqlWithItem);
        SqlWith sqlWith = new SqlWith(SqlParserPos.ZERO, sqlWithItems, query);
        SqlNodeList result = new SqlNodeList(SqlParserPos.ZERO);
        result.add(sqlWith);
        return Optional.of(result);
    }
}
