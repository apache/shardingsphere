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

package org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.engine;

import com.cedarsoftware.util.CaseInsensitiveMap;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratedKeyContextEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private ShardingSphereSchema schema;
    
    @BeforeEach
    void setUp() {
        ShardingSphereTable fooTable = new ShardingSphereTable(
                "foo_tbl", Collections.singletonList(new ShardingSphereColumn("id", Types.INTEGER, true, true,"int", false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable barTable = new ShardingSphereTable(
                "bar_tbl", Collections.singletonList(new ShardingSphereColumn("ID", Types.INTEGER, true, true,"int", false, true, false, false)), Collections.emptyList(), Collections.emptyList());
        schema = new ShardingSphereSchema("foo_db", Arrays.asList(fooTable, barTable), Collections.emptyList());
    }
    
    @Test
    void assertCreateGenerateKeyContextWithoutGenerateKeyColumnConfiguration() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl1"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        assertFalse(new GeneratedKeyContextEngine(insertStatement, schema).createGenerateKeyContext(Collections.emptyMap(),
                Collections.emptyList(), Collections.singletonList(1)).isPresent());
    }
    
    @Test
    void assertCreateGenerateKeyContextWhenCreateWithGenerateKeyColumnConfiguration() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        List<ExpressionSegment> expressionSegments = Collections.singletonList(new LiteralExpressionSegment(0, 0, 1));
        InsertValueContext insertValueContext = new InsertValueContext(expressionSegments, Collections.emptyList(), 0);
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, expressionSegments));
        Optional<GeneratedKeyContext> actual = new GeneratedKeyContextEngine(insertStatement, schema)
                .createGenerateKeyContext(new CaseInsensitiveMap<>(Collections.singletonMap("id", 0)), Collections.singletonList(insertValueContext), Collections.singletonList(1));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(1));
    }
    
    @Test
    void assertCreateGenerateKeyContextWhenFind() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_tbl"))));
        insertStatement.setInsertColumns(new InsertColumnsSegment(0, 0, Collections.singletonList(new ColumnSegment(0, 0, new IdentifierValue("id")))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new ParameterMarkerExpressionSegment(1, 2, 0))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(1, 2, 100))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new LiteralExpressionSegment(1, 2, "value"))));
        insertStatement.getValues().add(new InsertValuesSegment(0, 0, Collections.singletonList(new CommonExpressionSegment(1, 2, "ignored value"))));
        List<InsertValueContext> insertValueContexts = insertStatement.getValues().stream()
                .map(each -> new InsertValueContext(each.getValues(), Collections.emptyList(), 0)).collect(Collectors.toList());
        Optional<GeneratedKeyContext> actual = new GeneratedKeyContextEngine(insertStatement, schema)
                .createGenerateKeyContext(Collections.singletonMap("id", 0), insertValueContexts, Collections.singletonList(1));
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedValues().size(), is(3));
        Iterator<Comparable<?>> generatedValuesIterator = actual.get().getGeneratedValues().iterator();
        assertThat(generatedValuesIterator.next(), is(1));
        assertThat(generatedValuesIterator.next(), is(100));
        assertThat(generatedValuesIterator.next(), is("value"));
        assertTrue(new GeneratedKeyContextEngine(insertStatement, schema).createGenerateKeyContext(Collections.emptyMap(), Collections.emptyList(), Collections.singletonList(1)).isPresent());
    }
}
