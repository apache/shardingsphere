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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.type;

import org.apache.calcite.sql.SqlMerge;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlUpdate;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.SQLStatementConverter;

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
        for (ColumnAssignmentSegment each : updateStatement.getAssignment().orElseThrow(IllegalStateException::new).getAssignments()) {
            columns.addAll(each.getColumns().stream().map(ColumnConverter::convert).collect(Collectors.toList()));
            expressions.add(ExpressionConverter.convert(each.getValue()).orElseThrow(IllegalStateException::new));
        }
        return new SqlUpdate(SqlParserPos.ZERO, table, columns, expressions, condition, null, null);
    }
}
