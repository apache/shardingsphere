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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.insert;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlInsert;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlValuesOperator;
import org.apache.calcite.sql.fun.SqlRowOperator;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.SQLStatementConverter;
import org.apache.shardingsphere.sqlfederation.optimizer.converter.statement.select.SelectStatementConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        SqlNode table = TableConverter.convert(insertStatement.getTable()).orElseThrow(IllegalStateException::new);
        SqlNodeList keywords = new SqlNodeList(SqlParserPos.ZERO);
        SqlNode source = convertSource(insertStatement);
        SqlNodeList columnList = convertColumn(insertStatement.getColumns());
        return new SqlInsert(SqlParserPos.ZERO, keywords, table, source, columnList);
    }
    
    private SqlNode convertSource(final InsertStatement insertStatement) {
        if (insertStatement.getInsertSelect().isPresent()) {
            return new SelectStatementConverter().convert(insertStatement.getInsertSelect().get().getSelect());
        } else {
            return convertValues(insertStatement.getValues());
        }
    }
    
    private SqlNode convertValues(final Collection<InsertValuesSegment> insertValuesSegments) {
        List<SqlNode> values = new ArrayList<>();
        for (InsertValuesSegment each : insertValuesSegments) {
            for (ExpressionSegment value : each.getValues()) {
                values.add(convertExpression(value));
            }
        }
        List<SqlNode> operands = Collections.singletonList(new SqlBasicCall(new SqlRowOperator("ROW"), values, SqlParserPos.ZERO));
        return new SqlBasicCall(new SqlValuesOperator(), operands, SqlParserPos.ZERO);
    }
    
    private SqlNodeList convertColumn(final Collection<ColumnSegment> columnSegments) {
        List<SqlNode> columns = columnSegments.stream().map(each -> ColumnConverter.convert(each).orElseThrow(IllegalStateException::new)).collect(Collectors.toList());
        return columns.isEmpty() ? null : new SqlNodeList(columns, SqlParserPos.ZERO);
    }
    
    private SqlNode convertExpression(final ExpressionSegment expressionSegment) {
        return ExpressionConverter.convert(expressionSegment).orElseThrow(IllegalStateException::new);
    }
}
