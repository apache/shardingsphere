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

package org.apache.shardingsphere.sqlfederation.compiler.converter.statement.insert;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlInsert;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.SqlValuesOperator;
import org.apache.calcite.sql.fun.SqlRowOperator;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.handler.dml.SelectStatementHandler;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.groupby.GroupByConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.groupby.HavingConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.projection.DistinctConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.projection.ProjectionsConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.where.WhereConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.segment.window.WindowConverter;
import org.apache.shardingsphere.sqlfederation.compiler.converter.statement.SQLStatementConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Insert statement converter.
 */
public final class InsertStatementConverter implements SQLStatementConverter<InsertStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final InsertStatement insertStatement) {
        return convertInsert(insertStatement);
    }
    
    private SqlInsert convertInsert(final InsertStatement insertStatement) {
        SqlNode table = new TableConverter().convert(insertStatement.getTable()).orElseThrow(IllegalStateException::new);
        SqlParserPos position = SqlParserPos.ZERO;
        SqlNodeList keywords = new SqlNodeList(position);
        SqlNode source;
        if (insertStatement.getInsertSelect().isPresent()) {
            source = convertSelect(insertStatement.getInsertSelect().get());
        } else {
            source = convertValues(insertStatement.getValues());
        }
        SqlNodeList columnList = convertColumn(insertStatement.getColumns());
        return new SqlInsert(SqlParserPos.ZERO, keywords, table, source, columnList);
    }
    
    private SqlNode convertSelect(final SubquerySegment subquerySegment) {
        SelectStatement selectStatement = subquerySegment.getSelect();
        SqlNodeList distinct = new DistinctConverter().convert(selectStatement.getProjections()).orElse(null);
        SqlNodeList projection = new ProjectionsConverter().convert(selectStatement.getProjections()).orElseThrow(IllegalStateException::new);
        SqlNode from = new TableConverter().convert(selectStatement.getFrom()).orElse(null);
        SqlNode where = selectStatement.getWhere().flatMap(optional -> new WhereConverter().convert(optional)).orElse(null);
        SqlNodeList groupBy = selectStatement.getGroupBy().flatMap(optional -> new GroupByConverter().convert(optional)).orElse(null);
        SqlNode having = selectStatement.getHaving().flatMap(optional -> new HavingConverter().convert(optional)).orElse(null);
        SqlNodeList window = SelectStatementHandler.getWindowSegment(selectStatement).flatMap(new WindowConverter()::convert).orElse(SqlNodeList.EMPTY);
        return new SqlSelect(SqlParserPos.ZERO, distinct, projection, from, where, groupBy, having, window, null, null, null, null, SqlNodeList.EMPTY);
    }
    
    private SqlNode convertValues(final Collection<InsertValuesSegment> insertValuesSegments) {
        List<SqlNode> values = new ArrayList<>();
        for (InsertValuesSegment each : insertValuesSegments) {
            values.add(convertExpression(each.getValues().get(0)));
        }
        List<SqlNode> operands = new ArrayList<>();
        operands.add(new SqlBasicCall(new SqlRowOperator("ROW"), values, SqlParserPos.ZERO));
        return new SqlBasicCall(new SqlValuesOperator(), operands, SqlParserPos.ZERO);
    }
    
    private SqlNodeList convertColumn(final Collection<ColumnSegment> columnSegments) {
        List<SqlNode> columns = columnSegments.stream().map(each -> new ColumnConverter().convert(each).orElseThrow(IllegalStateException::new)).collect(Collectors.toList());
        if (columns.isEmpty()) {
            return null;
        }
        return new SqlNodeList(columns, SqlParserPos.ZERO);
    }
    
    private SqlNode convertExpression(final ExpressionSegment expressionSegment) {
        return new ExpressionConverter().convert(expressionSegment).orElseThrow(IllegalStateException::new);
    }
}
