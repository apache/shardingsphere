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

package org.apache.shardingsphere.infra.binder.segment.insert.keygen.engine;

import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.column.PhysicalColumnMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.table.PhysicalTableMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class GeneratedKeyContextEngineTest {
    
    private PhysicalSchemaMetaData schemaMetaData;
    
    @Before
    public void setUp() {
        PhysicalTableMetaData tableMetaData = new PhysicalTableMetaData(Collections.singletonList(new PhysicalColumnMetaData("id", Types.INTEGER, "INT", true, true, false)), Collections.emptyList());
        Map<String, PhysicalTableMetaData> tableMetaDataMap = new HashMap<>(1, 1);
        tableMetaDataMap.put("tbl", tableMetaData);
        schemaMetaData = new PhysicalSchemaMetaData(tableMetaDataMap);
    }
    
    @Test
    public void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfigurationForMySQL() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new MySQLInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfigurationForOracle() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new OracleInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfigurationForPostgreSQL() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfigurationForSQL92() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new SQL92InsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfigurationForSQLServer() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new SQLServerInsertStatement());
    }
    
    private void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl1")));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        assertFalse(new GeneratedKeyContextEngine(insertStatement, schemaMetaData).createGenerateKeyContext(Collections.emptyList(), 
                Collections.emptyList(), Collections.singletonList(1)).isPresent());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfigurationForMySQL() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new MySQLInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfigurationForOracle() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new OracleInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfigurationForPostgreSQL() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfigurationForSQL92() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new SQL92InsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfigurationForSQLServer() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new SQLServerInsertStatement());
    }
    
    private void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        List<ExpressionSegment> expressionSegments = Collections.singletonList(new LiteralExpressionSegment(0, 0, 1));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, expressionSegments));
        Optional<GeneratedKeyContext> actual = new GeneratedKeyContextEngine(insertStatement, schemaMetaData)
                .createGenerateKeyContext(Collections.singletonList("id"), Collections.singletonList(expressionSegments), Collections.singletonList(1));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(1));
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenFindForMySQL() {
        assertCreateGenerateKeyContextWhenFind(new MySQLInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenFindForOracle() {
        assertCreateGenerateKeyContextWhenFind(new OracleInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenFindForPostgreSQL() {
        assertCreateGenerateKeyContextWhenFind(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenFindForSQL92() {
        assertCreateGenerateKeyContextWhenFind(new SQL92InsertStatement());
    }
    
    @Test
    public void assertCreateGenerateKeyContextWhenFindForSQLServer() {
        assertCreateGenerateKeyContextWhenFind(new SQLServerInsertStatement());
    }
    
    private void assertCreateGenerateKeyContextWhenFind(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new ParameterMarkerExpressionSegment(1, 2, 0))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(1, 2, 100))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(1, 2, "value"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new CommonExpressionSegment(1, 2, "ignored value"))));
        List<List<ExpressionSegment>> valueExpressions = insertStatement.getValues().stream().map(InsertValuesSegment::getValues).collect(Collectors.toList());
        Optional<GeneratedKeyContext> actual = new GeneratedKeyContextEngine(insertStatement, schemaMetaData)
                .createGenerateKeyContext(Collections.singletonList("id"), valueExpressions, Collections.singletonList(1));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(3));
        Iterator<Comparable<?>> generatedValuesIterator = actual.get().getGeneratedValues().iterator();
        assertThat(generatedValuesIterator.next(), is((Comparable) 1));
        assertThat(generatedValuesIterator.next(), is((Comparable) 100));
        assertThat(generatedValuesIterator.next(), is((Comparable) "value"));
        assertTrue(new GeneratedKeyContextEngine(insertStatement, schemaMetaData).createGenerateKeyContext(Collections.emptyList(), 
                Collections.emptyList(), Collections.singletonList(1)).isPresent());
    }
}
