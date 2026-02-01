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

import org.apache.calcite.sql.SqlExplain;
import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.DCLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExplainStatementConverterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertSelectStatement() {
        ExplainStatement explainStatement = new ExplainStatement(databaseType, createSelectStatement());
        SqlNode actual = new ExplainStatementConverter().convert(explainStatement);
        assertThat(((SqlExplain) actual).getExplicandum(), isA(SqlNode.class));
    }
    
    @Test
    void assertConvertDeleteStatement() {
        ExplainStatement explainStatement = new ExplainStatement(databaseType, createDeleteStatement());
        SqlNode actual = new ExplainStatementConverter().convert(explainStatement);
        assertThat(((SqlExplain) actual).getExplicandum(), isA(SqlNode.class));
    }
    
    @Test
    void assertConvertUpdateStatement() {
        ExplainStatement explainStatement = new ExplainStatement(databaseType, createUpdateStatement());
        SqlNode actual = new ExplainStatementConverter().convert(explainStatement);
        assertThat(((SqlExplain) actual).getExplicandum(), isA(SqlNode.class));
    }
    
    @Test
    void assertConvertInsertStatement() {
        ExplainStatement explainStatement = new ExplainStatement(databaseType, createInsertStatement());
        SqlNode actual = new ExplainStatementConverter().convert(explainStatement);
        assertThat(((SqlExplain) actual).getExplicandum(), isA(SqlNode.class));
    }
    
    @Test
    void assertConvertUnsupportedSQLStatementThrowsException() {
        DCLStatement mockStatement = Mockito.mock(DCLStatement.class, Mockito.withSettings().defaultAnswer(Mockito.CALLS_REAL_METHODS));
        Mockito.when(mockStatement.getDatabaseType()).thenReturn(databaseType);
        ExplainStatement explainStatement = new ExplainStatement(databaseType, mockStatement);
        assertThrows(IllegalStateException.class, () -> new ExplainStatementConverter().convert(explainStatement));
    }
    
    private SelectStatement createSelectStatement() {
        SelectStatement result = new SelectStatement(databaseType);
        result.setProjections(createProjectionsSegment());
        result.setFrom(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_select"))));
        return result;
    }
    
    private DeleteStatement createDeleteStatement() {
        DeleteStatement result = new DeleteStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_delete"))));
        return result;
    }
    
    private UpdateStatement createUpdateStatement() {
        UpdateStatement result = new UpdateStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_update"))));
        result.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singleton(new ColumnAssignmentSegment(0, 0,
                Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))), new ParameterMarkerExpressionSegment(0, 0, 0)))));
        return result;
    }
    
    private InsertStatement createInsertStatement() {
        InsertStatement result = new InsertStatement(databaseType);
        result.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("t_insert"))));
        result.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0))));
        return result;
    }
    
    private ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment result = new ProjectionsSegment(0, 0);
        result.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        return result;
    }
}
