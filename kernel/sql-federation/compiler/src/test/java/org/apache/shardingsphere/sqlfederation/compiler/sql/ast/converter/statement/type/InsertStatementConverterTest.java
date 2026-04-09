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

import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlInsert;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;

class InsertStatementConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertWithInsertSelect() {
        SelectStatement selectStatement = SelectStatement.builder().databaseType(databaseType).projections(createProjectionsSegment()).build();
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_insert_select"))))
                .insertSelect(new SubquerySegment(0, 0, selectStatement, "select")).build();
        SqlInsert actual = (SqlInsert) new InsertStatementConverter().convert(insertStatement);
        assertThat(actual.getSource(), isA(SqlNode.class));
    }
    
    @Test
    void assertConvertWithSetAssignmentColumns() {
        SetAssignmentSegment setAssignment = createSetAssignmentSegment();
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_insert_set"))))
                .setAssignment(setAssignment).insertColumns(new InsertColumnsSegment(0, 0, Collections.emptyList())).build();
        SqlInsert actual = (SqlInsert) new InsertStatementConverter().convert(insertStatement);
        assertThat(actual.getTargetTable(), isA(SqlNode.class));
        assertThat(actual.getSource(), isA(SqlBasicCall.class));
        assertThat(actual.getTargetColumnList(), isA(SqlNodeList.class));
    }
    
    @Test
    void assertConvertWithValuesOnly() {
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_insert_values"))))
                .values(Collections.singletonList(new InsertValuesSegment(0, 0, Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0))))).build();
        SqlInsert actual = (SqlInsert) new InsertStatementConverter().convert(insertStatement);
        assertThat(actual.getSource(), isA(SqlBasicCall.class));
        assertNull(actual.getTargetColumnList());
    }
    
    @Test
    void assertConvertWithExplicitColumns() {
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        InsertStatement insertStatement = InsertStatement.builder().databaseType(databaseType)
                .table(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_insert_columns"))))
                .insertColumns(insertColumnsSegment)
                .values(Collections.singletonList(new InsertValuesSegment(0, 0, Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0))))).build();
        SqlInsert actual = (SqlInsert) new InsertStatementConverter().convert(insertStatement);
        assertThat(actual.getTargetColumnList(), isA(SqlNodeList.class));
    }
    
    private ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        return result;
    }
    
    private SetAssignmentSegment createSetAssignmentSegment() {
        ColumnAssignmentSegment columnAssignmentSegment = new ColumnAssignmentSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))),
                new ParameterMarkerExpressionSegment(0, 0, 0));
        return new SetAssignmentSegment(0, 0, Collections.singleton(columnAssignmentSegment));
    }
}
