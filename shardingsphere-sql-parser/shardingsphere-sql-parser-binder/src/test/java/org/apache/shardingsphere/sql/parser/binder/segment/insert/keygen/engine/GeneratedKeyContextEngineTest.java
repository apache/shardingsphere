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

package org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.engine;

import org.apache.shardingsphere.sql.parser.binder.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.metadata.table.TableMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.InsertColumnsSegment;
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
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class GeneratedKeyContextEngineTest {
    
    private SchemaMetaData schemaMetaData;
    
    @Before
    public void setUp() {
        TableMetaData tableMetaData = new TableMetaData(Collections.singletonList(new ColumnMetaData("id", Types.INTEGER, "INT", true, true, false)), Collections.emptyList());
        Map<String, TableMetaData> tableMetaDataMap = new HashMap<>(1, 1);
        tableMetaDataMap.put("tbl", tableMetaData);
        schemaMetaData = new SchemaMetaData(tableMetaDataMap);
    }
    
    @Test
    public void assertMySQLCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new MySQLInsertStatement());
    }

    @Test
    public void assertOracleCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new OracleInsertStatement());
    }

    @Test
    public void assertPostgreSQLCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new PostgreSQLInsertStatement());
    }

    @Test
    public void assertSQL92CreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new SQL92InsertStatement());
    }

    @Test
    public void assertSQLServerCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(new SQLServerInsertStatement());
    }
    
    private void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl1")));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        assertFalse(new GeneratedKeyContextEngine(schemaMetaData).createGenerateKeyContext(Collections.singletonList(1), insertStatement).isPresent());
    }
    
    @Test
    public void assertMySQLCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new MySQLInsertStatement());
    }

    @Test
    public void assertOracleCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new OracleInsertStatement());
    }

    @Test
    public void assertPostgreSQLCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new PostgreSQLInsertStatement());
    }

    @Test
    public void assertSQL92CreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new SQL92InsertStatement());
    }

    @Test
    public void assertSQLServerCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration() {
        assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(new SQLServerInsertStatement());
    }

    private void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(0, 0, 1))));
        Optional<GeneratedKeyContext> actual = new GeneratedKeyContextEngine(schemaMetaData).createGenerateKeyContext(Collections.singletonList(1), insertStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(1));
    }
    
    @Test
    public void assertMySQLCreateGenerateKeyContextWhenFind() {
        assertCreateGenerateKeyContextWhenFind(new MySQLInsertStatement());
    }

    @Test
    public void assertOracleCreateGenerateKeyContextWhenFind() {
        assertCreateGenerateKeyContextWhenFind(new OracleInsertStatement());
    }

    @Test
    public void assertPostgreSQLCreateGenerateKeyContextWhenFind() {
        assertCreateGenerateKeyContextWhenFind(new PostgreSQLInsertStatement());
    }

    @Test
    public void assertSQL92CreateGenerateKeyContextWhenFind() {
        assertCreateGenerateKeyContextWhenFind(new SQL92InsertStatement());
    }

    @Test
    public void assertSQLServerCreateGenerateKeyContextWhenFind() {
        assertCreateGenerateKeyContextWhenFind(new SQLServerInsertStatement());
    }
    
    private void assertCreateGenerateKeyContextWhenFind(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(0, 0, new IdentifierValue("tbl")));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new ParameterMarkerExpressionSegment(1, 2, 0))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(1, 2, 100))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(1, 2, "value"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new CommonExpressionSegment(1, 2, "ignored value"))));
        Optional<GeneratedKeyContext> actual = new GeneratedKeyContextEngine(schemaMetaData).createGenerateKeyContext(Collections.singletonList(1), insertStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(3));
        assertThat(actual.get().getGeneratedValues().get(0), is((Comparable) 1));
        assertThat(actual.get().getGeneratedValues().get(1), is((Comparable) 100));
        assertThat(actual.get().getGeneratedValues().get(2), is((Comparable) "value"));
        assertTrue(new GeneratedKeyContextEngine(schemaMetaData).createGenerateKeyContext(Collections.singletonList(1), insertStatement).isPresent());
    }
}
