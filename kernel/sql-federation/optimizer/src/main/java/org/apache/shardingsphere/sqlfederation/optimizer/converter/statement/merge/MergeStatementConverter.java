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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.merge;

import org.apache.calcite.sql.SqlMerge;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlUpdate;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.SQLStatementConverter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Merge statement converter.
 */
public final class MergeStatementConverter implements SQLStatementConverter<MergeStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final MergeStatement mergeStatement) {
        SqlNode targetTable = TableConverter.convert(mergeStatement.getTarget()).orElseThrow(IllegalStateException::new);
        SqlNode condition = ExpressionConverter.convert(mergeStatement.getExpression().getExpr()).orElseThrow(IllegalStateException::new);
        SqlNode sourceTable = TableConverter.convert(mergeStatement.getSource()).orElseThrow(IllegalStateException::new);
        SqlUpdate sqlUpdate = mergeStatement.getUpdate().map(this::convertUpdate).orElse(null);
        return new SqlMerge(SqlParserPos.ZERO, targetTable, condition, sourceTable, sqlUpdate, null, null, null);
    }
    
    private SqlUpdate convertUpdate(final UpdateStatement updateStatement) {
        SqlNode table = TableConverter.convert(updateStatement.getTable()).orElse(SqlNodeList.EMPTY);
        SqlNode condition = updateStatement.getWhere().flatMap(WhereConverter::convert).orElse(null);
        SqlNodeList columns = new SqlNodeList(SqlParserPos.ZERO);
        SqlNodeList expressions = new SqlNodeList(SqlParserPos.ZERO);
        for (ColumnAssignmentSegment each : updateStatement.getAssignmentSegment().orElseThrow(IllegalStateException::new).getAssignments()) {
            columns.addAll(convertColumn(each.getColumns()));
            expressions.add(convertExpression(each.getValue()));
        }
        return new SqlUpdate(SqlParserPos.ZERO, table, columns, expressions, condition, null, null);
    }
    
    private List<SqlNode> convertColumn(final List<ColumnSegment> columnSegments) {
        return columnSegments.stream().map(each -> ColumnConverter.convert(each).orElseThrow(IllegalStateException::new)).collect(Collectors.toList());
    }
    
    private SqlNode convertExpression(final ExpressionSegment expressionSegment) {
        return ExpressionConverter.convert(expressionSegment).orElseThrow(IllegalStateException::new);
    }
}
