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

package org.apache.shardingsphere.infra.binder.statement.dml;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.OnDuplicateKeyColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InsertStatementContextTest {
    
    @Test
    void assertMySQLInsertStatementContextWithColumnNames() {
        assertInsertStatementContextWithColumnNames(new MySQLInsertStatement());
    }
    
    @Test
    void assertOracleInsertStatementContextWithColumnNames() {
        assertInsertStatementContextWithColumnNames(new OracleInsertStatement());
    }
    
    @Test
    void assertPostgreSQLInsertStatementContextWithColumnNames() {
        assertInsertStatementContextWithColumnNames(new PostgreSQLInsertStatement());
    }
    
    @Test
    void assertSQL92InsertStatementContextWithColumnNames() {
        assertInsertStatementContextWithColumnNames(new SQL92InsertStatement());
    }
    
    @Test
    void assertSQLServerInsertStatementContextWithColumnNames() {
        assertInsertStatementContextWithColumnNames(new SQLServerInsertStatement());
    }
    
    private void assertInsertStatementContextWithColumnNames(final InsertStatement insertStatement) {
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl")));
        tableSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue(DefaultDatabase.LOGIC_NAME.toUpperCase())));
        insertStatement.setTable(tableSegment);
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Arrays.asList(
                new ColumnSegment(0, 0, new IdentifierValue("id")), new ColumnSegment(0, 0, new IdentifierValue("name")), new ColumnSegment(0, 0, new IdentifierValue("status"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(Arrays.asList(1, "Tom", 2, "Jerry"), insertStatement);
        actual.setUpParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        assertInsertStatementContext(actual);
    }
    
    private InsertStatementContext createInsertStatementContext(final List<Object> params, final InsertStatement insertStatement) {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        String defaultSchemaName = insertStatement instanceof PostgreSQLStatement || insertStatement instanceof OpenGaussStatement ? "public" : DefaultDatabase.LOGIC_NAME;
        when(database.getSchema(defaultSchemaName)).thenReturn(schema);
        when(schema.getVisibleColumnNames("tbl")).thenReturn(Arrays.asList("id", "name", "status"));
        return new InsertStatementContext(createShardingSphereMetaData(database), params, insertStatement, DefaultDatabase.LOGIC_NAME);
    }
    
    private ShardingSphereMetaData createShardingSphereMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singletonMap(DefaultDatabase.LOGIC_NAME, database), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
    
    @Test
    void assertInsertStatementContextWithoutColumnNames() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(Arrays.asList(1, "Tom", 2, "Jerry"), insertStatement);
        actual.setUpParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        assertInsertStatementContext(actual);
    }
    
    @Test
    void assertGetGroupedParametersWithoutOnDuplicateParameter() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        setUpInsertValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(Arrays.asList(1, "Tom", 2, "Jerry"), insertStatement);
        actual.setUpParameters(Arrays.asList(1, "Tom", 2, "Jerry"));
        assertThat(actual.getGroupedParameters().size(), is(2));
        assertNull(actual.getOnDuplicateKeyUpdateValueContext());
        assertTrue(actual.getOnDuplicateKeyUpdateParameters().isEmpty());
    }
    
    @Test
    void assertGetGroupedParametersWithOnDuplicateParameters() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        setUpInsertValues(insertStatement);
        setUpOnDuplicateValues(insertStatement);
        InsertStatementContext actual = createInsertStatementContext(Arrays.asList(1, "Tom", 2, "Jerry", "onDuplicateKeyUpdateColumnValue"), insertStatement);
        actual.setUpParameters(Arrays.asList(1, "Tom", 2, "Jerry", "onDuplicateKeyUpdateColumnValue"));
        assertThat(actual.getGroupedParameters().size(), is(2));
        assertThat(actual.getOnDuplicateKeyUpdateValueContext().getColumns().size(), is(2));
        assertThat(actual.getOnDuplicateKeyUpdateParameters().size(), is(1));
    }
    
    @Test
    void assertInsertSelect() {
        InsertStatement insertStatement = new MySQLInsertStatement();
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubquerySegment insertSelect = new SubquerySegment(0, 0, selectStatement);
        insertStatement.setInsertSelect(insertSelect);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        InsertStatementContext actual = createInsertStatementContext(Collections.singletonList("param"), insertStatement);
        actual.setUpParameters(Collections.singletonList("param"));
        assertThat(actual.getInsertSelectContext().getParameterCount(), is(0));
        assertThat(actual.getGroupedParameters().size(), is(1));
        assertThat(actual.getGroupedParameters().iterator().next(), is(Collections.emptyList()));
    }
    
    private void setUpInsertValues(final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 1), new ParameterMarkerExpressionSegment(0, 0, 2), new LiteralExpressionSegment(0, 0, "init"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Arrays.asList(
                new ParameterMarkerExpressionSegment(0, 0, 3), new ParameterMarkerExpressionSegment(0, 0, 4), new LiteralExpressionSegment(0, 0, "init"))));
    }
    
    private void setUpOnDuplicateValues(final MySQLInsertStatement insertStatement) {
        List<ColumnSegment> parameterMarkerExpressionAssignmentColumns = new LinkedList<>();
        parameterMarkerExpressionAssignmentColumns.add(new ColumnSegment(0, 0, new IdentifierValue("on_duplicate_key_update_column_1")));
        AssignmentSegment parameterMarkerExpressionAssignment = new ColumnAssignmentSegment(0, 0, parameterMarkerExpressionAssignmentColumns,
                new ParameterMarkerExpressionSegment(0, 0, 4));
        List<ColumnSegment> literalExpressionAssignmentColumns = new LinkedList<>();
        literalExpressionAssignmentColumns.add(new ColumnSegment(0, 0, new IdentifierValue("on_duplicate_key_update_column_2")));
        AssignmentSegment literalExpressionAssignment = new ColumnAssignmentSegment(0, 0, literalExpressionAssignmentColumns,
                new LiteralExpressionSegment(0, 0, 5));
        OnDuplicateKeyColumnsSegment onDuplicateKeyColumnsSegment = new OnDuplicateKeyColumnsSegment(0, 0, Arrays.asList(parameterMarkerExpressionAssignment, literalExpressionAssignment));
        insertStatement.setOnDuplicateKeyColumns(onDuplicateKeyColumnsSegment);
    }
    
    private void assertInsertStatementContext(final InsertStatementContext actual) {
        assertThat(actual.getTablesContext().getTableNames(), is(new HashSet<>(Collections.singleton("tbl"))));
        assertThat(actual.getAllTables().size(), is(1));
        SimpleTableSegment simpleTableSegment = actual.getAllTables().iterator().next();
        assertThat(simpleTableSegment.getTableName().getStartIndex(), is(0));
        assertThat(simpleTableSegment.getTableName().getStopIndex(), is(0));
        assertThat(simpleTableSegment.getTableName().getIdentifier().getValue(), is("tbl"));
        List<String> columnNames = new ArrayList<>(3);
        actual.getDescendingColumnNames().forEachRemaining(columnNames::add);
        assertThat(columnNames, is(Arrays.asList("status", "name", "id")));
        assertThat(actual.getGeneratedKeyContext(), is(Optional.empty()));
        assertThat(actual.getColumnNames(), is(Arrays.asList("id", "name", "status")));
        assertThat(actual.getInsertValueContexts().size(), is(2));
        assertTrue(actual.getInsertValueContexts().get(0).getLiteralValue(0).isPresent());
        assertTrue(actual.getInsertValueContexts().get(0).getLiteralValue(1).isPresent());
        assertTrue(actual.getInsertValueContexts().get(0).getLiteralValue(2).isPresent());
        assertTrue(actual.getInsertValueContexts().get(1).getLiteralValue(0).isPresent());
        assertTrue(actual.getInsertValueContexts().get(1).getLiteralValue(1).isPresent());
        assertTrue(actual.getInsertValueContexts().get(1).getLiteralValue(2).isPresent());
        assertThat(actual.getInsertValueContexts().get(0).getLiteralValue(0).get(), is(1));
        assertThat(actual.getInsertValueContexts().get(0).getLiteralValue(1).get(), is("Tom"));
        assertThat(actual.getInsertValueContexts().get(0).getLiteralValue(2).get(), is("init"));
        assertThat(actual.getInsertValueContexts().get(1).getLiteralValue(0).get(), is(2));
        assertThat(actual.getInsertValueContexts().get(1).getLiteralValue(1).get(), is("Jerry"));
        assertThat(actual.getInsertValueContexts().get(1).getLiteralValue(2).get(), is("init"));
    }
    
    @Test
    void assertUseDefaultColumnsForMySQL() {
        assertContainsInsertColumns(new MySQLInsertStatement());
    }
    
    @Test
    void assertUseDefaultColumnsForOracle() {
        assertContainsInsertColumns(new OracleInsertStatement());
    }
    
    @Test
    void assertUseDefaultColumnsForPostgreSQL() {
        assertContainsInsertColumns(new PostgreSQLInsertStatement());
    }
    
    @Test
    void assertUseDefaultColumnsForSQL92() {
        assertContainsInsertColumns(new SQL92InsertStatement());
    }
    
    @Test
    void assertUseDefaultColumnsForSQLServer() {
        assertContainsInsertColumns(new SQLServerInsertStatement());
    }
    
    private void assertNotContainsInsertColumns(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        assertFalse(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertNotUseDefaultColumnsWithColumnsForMySQL() {
        assertNotContainsInsertColumns(new MySQLInsertStatement());
    }
    
    @Test
    void assertNotUseDefaultColumnsWithColumnsForOracle() {
        assertNotContainsInsertColumns(new OracleInsertStatement());
    }
    
    @Test
    void assertNotUseDefaultColumnsWithColumnsForPostgreSQL() {
        assertNotContainsInsertColumns(new PostgreSQLInsertStatement());
    }
    
    @Test
    void assertNotUseDefaultColumnsWithColumnsForSQL92() {
        assertNotContainsInsertColumns(new SQL92InsertStatement());
    }
    
    @Test
    void assertNotUseDefaultColumnsWithColumnsForSQLServer() {
        assertNotContainsInsertColumns(new SQLServerInsertStatement());
    }
    
    private void assertContainsInsertColumns(final InsertStatement insertStatement) {
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        assertTrue(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertContainsInsertColumnsWithSetAssignmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.emptyList()));
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        assertTrue(insertStatementContext.containsInsertColumns());
    }
    
    @Test
    void assertGetValueListCountWithValuesForMySQL() {
        assertGetValueListCountWithValues(new MySQLInsertStatement());
    }
    
    @Test
    void assertGetValueListCountWithValuesForOracle() {
        assertGetValueListCountWithValues(new OracleInsertStatement());
    }
    
    @Test
    void assertGetValueListCountWithValuesForPostgreSQL() {
        assertGetValueListCountWithValues(new PostgreSQLInsertStatement());
    }
    
    @Test
    void assertGetValueListCountWithValuesForSQL92() {
        assertGetValueListCountWithValues(new SQL92InsertStatement());
    }
    
    @Test
    void assertGetValueListCountWithValuesForSQLServer() {
        assertGetValueListCountWithValues(new SQLServerInsertStatement());
    }
    
    private void assertGetValueListCountWithValues(final InsertStatement insertStatement) {
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 2))));
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        assertThat(insertStatementContext.getValueListCount(), is(2));
    }
    
    @Test
    void assertGetValueListCountWithSetAssignmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("col")));
        AssignmentSegment insertStatementAssignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(insertStatementAssignment)));
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        assertThat(insertStatementContext.getValueListCount(), is(1));
    }
    
    @Test
    void assertGetInsertColumnNamesForInsertColumnsForMySQL() {
        assertGetInsertColumnNamesForInsertColumns(new MySQLInsertStatement());
    }
    
    @Test
    void assertGetInsertColumnNamesForInsertColumnsForOracle() {
        assertGetInsertColumnNamesForInsertColumns(new OracleInsertStatement());
    }
    
    @Test
    void assertGetInsertColumnNamesForInsertColumnsForPostgreSQL() {
        assertGetInsertColumnNamesForInsertColumns(new PostgreSQLInsertStatement());
    }
    
    @Test
    void assertGetInsertColumnNamesForInsertColumnsForSQL92() {
        assertGetInsertColumnNamesForInsertColumns(new SQL92InsertStatement());
    }
    
    @Test
    void assertGetInsertColumnNamesForInsertColumnsForSQLServer() {
        assertGetInsertColumnNamesForInsertColumns(new SQLServerInsertStatement());
    }
    
    private void assertGetInsertColumnNamesForInsertColumns(final InsertStatement insertStatement) {
        InsertColumnsSegment insertColumnsSegment = new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("col"))));
        insertStatement.setInsertColumns(insertColumnsSegment);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        List<String> columnNames = insertStatementContext.getInsertColumnNames();
        assertThat(columnNames.size(), is(1));
        assertThat(columnNames.iterator().next(), is("col"));
    }
    
    @Test
    void assertGetInsertColumnNamesForSetAssignmentForMySQL() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        List<ColumnSegment> columns = new LinkedList<>();
        columns.add(new ColumnSegment(0, 0, new IdentifierValue("col")));
        AssignmentSegment insertStatementAssignment = new ColumnAssignmentSegment(0, 0, columns, new LiteralExpressionSegment(0, 0, 1));
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0, Collections.singletonList(insertStatementAssignment)));
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue(""))));
        InsertStatementContext insertStatementContext = createInsertStatementContext(Collections.emptyList(), insertStatement);
        List<String> columnNames = insertStatementContext.getInsertColumnNames();
        assertThat(columnNames.size(), is(1));
        assertThat(columnNames.iterator().next(), is("col"));
    }
}
