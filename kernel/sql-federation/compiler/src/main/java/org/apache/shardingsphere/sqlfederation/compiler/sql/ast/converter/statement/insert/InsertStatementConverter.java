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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.insert;

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlInsert;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.SqlValuesOperator;
import org.apache.calcite.sql.fun.SqlRowOperator;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl.ColumnConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.from.TableConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.SQLStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.select.SelectStatementConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Insert statement converter.
 */
public final class InsertStatementConverter implements SQLStatementConverter<InsertStatement, SqlNode> {
    
    @Override
    public SqlNode convert(final InsertStatement insertStatement) {
        return convertInsert(insertStatement);
    }
    
    private SqlInsert convertInsert(final InsertStatement insertStatement) {
        SqlNode table = insertStatement.getTable().flatMap(TableConverter::convert).orElseThrow(IllegalStateException::new);
        SqlNode source = convertSource(insertStatement);
        SqlNodeList columns = convertColumn(insertStatement);
        return new SqlInsert(SqlParserPos.ZERO, SqlNodeList.EMPTY, table, source, columns);
    }
    
    private SqlNode convertSource(final InsertStatement insertStatement) {
        if (insertStatement.getInsertSelect().isPresent()) {
            return new SelectStatementConverter().convert(insertStatement.getInsertSelect().get().getSelect());
        } else if (insertStatement.getSetAssignment().isPresent()) {
            return convertSetAssignment(insertStatement.getSetAssignment().get());
        } else {
            return convertValues(insertStatement.getValues());
        }
    }
    
    private SqlNode convertSetAssignment(final SetAssignmentSegment setAssignment) {
        List<SqlNode> operands = new ArrayList<>();
        List<SqlNode> values = new ArrayList<>();
        for (ColumnAssignmentSegment each : setAssignment.getAssignments()) {
            values.add(convertExpression(each.getValue()));
        }
        operands.add(new SqlBasicCall(new SqlRowOperator("ROW"), values, SqlParserPos.ZERO));
        return new SqlBasicCall(new SqlValuesOperator(), operands, SqlParserPos.ZERO);
    }
    
    private SqlNode convertValues(final Collection<InsertValuesSegment> insertValuesSegments) {
        List<SqlNode> operands = new ArrayList<>();
        for (InsertValuesSegment each : insertValuesSegments) {
            List<SqlNode> values = new ArrayList<>();
            for (ExpressionSegment value : each.getValues()) {
                values.add(convertExpression(value));
            }
            operands.add(new SqlBasicCall(new SqlRowOperator("ROW"), values, SqlParserPos.ZERO));
        }
        return new SqlBasicCall(new SqlValuesOperator(), operands, SqlParserPos.ZERO);
    }
    
    private SqlNodeList convertColumn(final InsertStatement insertStatement) {
        List<SqlNode> columns = new ArrayList<>();
        insertStatement.getSetAssignment().ifPresent(optional -> columns.addAll(convertSetAssignmentColumns(optional)));
        for (ColumnSegment each : insertStatement.getColumns()) {
            columns.add(ColumnConverter.convert(each).orElseThrow(IllegalStateException::new));
        }
        return columns.isEmpty() ? null : new SqlNodeList(columns, SqlParserPos.ZERO);
    }
    
    private Collection<SqlNode> convertSetAssignmentColumns(final SetAssignmentSegment setAssignment) {
        List<SqlNode> result = new ArrayList<>();
        for (ColumnAssignmentSegment each : setAssignment.getAssignments()) {
            for (ColumnSegment column : each.getColumns()) {
                result.add(ColumnConverter.convert(column).orElseThrow(IllegalStateException::new));
            }
        }
        return result;
    }
    
    private SqlNode convertExpression(final ExpressionSegment expressionSegment) {
        return ExpressionConverter.convert(expressionSegment).orElseThrow(IllegalStateException::new);
    }
}
