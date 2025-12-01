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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.update;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlOrderBy;
import org.apache.calcite.sql.SqlUpdate;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.limit.PaginationValueSQLConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.orderby.OrderByConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.SQLStatementConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Update statement converter.
 */
public final class UpdateStatementConverter implements SQLStatementConverter<UpdateStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final UpdateStatement updateStatement) {
        SqlUpdate sqlUpdate = convertUpdate(updateStatement);
        SqlNodeList orderBy = updateStatement.getOrderBy().flatMap(OrderByConverter::convert).orElse(SqlNodeList.EMPTY);
        Optional<LimitSegment> limit = updateStatement.getLimit();
        if (limit.isPresent()) {
            SqlNode offset = limit.get().getOffset().flatMap(PaginationValueSQLConverter::convert).orElse(null);
            SqlNode rowCount = limit.get().getRowCount().flatMap(PaginationValueSQLConverter::convert).orElse(null);
            return new SqlOrderBy(SqlParserPos.ZERO, sqlUpdate, orderBy, offset, rowCount);
        }
        return orderBy.isEmpty() ? sqlUpdate : new SqlOrderBy(SqlParserPos.ZERO, sqlUpdate, orderBy, null, null);
    }
    
    private SqlUpdate convertUpdate(final UpdateStatement updateStatement) {
        SqlNode table = TableConverter.convert(updateStatement.getTable()).orElseThrow(IllegalStateException::new);
        SqlIdentifier alias = convertTableAlias(updateStatement);
        SqlNode condition = updateStatement.getWhere().flatMap(WhereConverter::convert).orElse(null);
        SqlNodeList columns = new SqlNodeList(SqlParserPos.ZERO);
        SqlNodeList expressions = new SqlNodeList(SqlParserPos.ZERO);
        for (ColumnAssignmentSegment each : updateStatement.getAssignment().orElseThrow(IllegalStateException::new).getAssignments()) {
            columns.addAll(convertColumn(each.getColumns()));
            expressions.add(convertExpression(each.getValue()));
        }
        return new SqlUpdate(SqlParserPos.ZERO, getTargetTableName(table), columns, expressions, condition, null, alias);
    }
    
    private SqlIdentifier convertTableAlias(final UpdateStatement updateStatement) {
        if (updateStatement.getTable().getAlias().isPresent()) {
            IdentifierValue aliasIdentifier = updateStatement.getTable().getAlias().get();
            return new SqlIdentifier(aliasIdentifier.getValue(), SqlParserPos.ZERO);
        }
        return null;
    }
    
    private List<SqlNode> convertColumn(final List<ColumnSegment> columnSegments) {
        return columnSegments.stream().map(each -> ColumnConverter.convert(each).orElseThrow(IllegalStateException::new)).collect(Collectors.toList());
    }
    
    private SqlNode convertExpression(final ExpressionSegment expressionSegment) {
        return ExpressionConverter.convert(expressionSegment).orElseThrow(IllegalStateException::new);
    }
    
    private SqlNode getTargetTableName(final SqlNode deleteTable) {
        return deleteTable instanceof SqlBasicCall ? ((SqlBasicCall) deleteTable).getOperandList().iterator().next() : deleteTable;
    }
}
