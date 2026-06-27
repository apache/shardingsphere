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

package org.apache.shardingsphere.data.pipeline.core.metadata.generator;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.index.CreateIndexStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PipelineDDLDecoratorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @Test
    void assertDecorateUsesSchemaSpecificCandidatesForSameNamedTables() {
        String logicIndexName = "very_long_named_index_boundary_case_for_sharding_length_safety_validation";
        String actualIndexName = IndexMetaDataUtils.getActualIndexName(logicIndexName, "t_order_0", databaseType);
        assertTrue(actualIndexName.matches(".*_t[0-9a-z]{8}$"));
        String actualSQL = String.format("CREATE INDEX %s ON t_order_0 (order_id)", actualIndexName);
        String expected = String.format("CREATE INDEX %s ON t_order (order_id)", logicIndexName);
        assertThat(
                new PipelineDDLDecorator(mockMetaData(logicIndexName)).decorate(databaseType, "foo_db", "schema_b", "t_order", createParserEngine(actualSQL, actualIndexName), actualSQL).orElse(""),
                is(expected));
    }
    
    private ShardingSphereMetaData mockMetaData(final String logicIndexName) {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingSphereSchema schemaA = new ShardingSphereSchema("schema_a", databaseType, Collections.singleton(
                new ShardingSphereTable("t_order", Collections.emptyList(),
                        Collections.singleton(new ShardingSphereIndex("another_very_long_named_index_for_schema_a_validation", Collections.emptyList(), false)), Collections.emptyList())),
                Collections.emptyList());
        ShardingSphereSchema schemaB = new ShardingSphereSchema("schema_b", databaseType, Collections.singleton(
                new ShardingSphereTable("t_order", Collections.emptyList(),
                        Collections.singleton(new ShardingSphereIndex(logicIndexName, Collections.emptyList(), false)), Collections.emptyList())),
                Collections.emptyList());
        when(result.getDatabase("foo_db")).thenReturn(database);
        when(database.containsSchema("schema_b")).thenReturn(true);
        when(database.getSchema("schema_b")).thenReturn(schemaB);
        when(database.getAllSchemas()).thenReturn(Arrays.asList(schemaA, schemaB));
        return result;
    }
    
    private SQLParserEngine createParserEngine(final String actualSQL, final String actualIndexName) {
        SQLParserEngine result = mock(SQLParserEngine.class);
        when(result.parse(actualSQL, true)).thenReturn(CreateIndexStatement.builder()
                .databaseType(databaseType)
                .index(new IndexSegment(getIndexStartIndex(), getIndexStopIndex(actualIndexName),
                        new IndexNameSegment(getIndexStartIndex(), getIndexStopIndex(actualIndexName), new IdentifierValue(actualIndexName))))
                .table(new SimpleTableSegment(new TableNameSegment(getTableStartIndex(actualSQL), getTableStopIndex(actualSQL), new IdentifierValue("t_order_0"))))
                .build());
        return result;
    }
    
    private int getIndexStartIndex() {
        return "CREATE INDEX ".length();
    }
    
    private int getIndexStopIndex(final String actualIndexName) {
        return getIndexStartIndex() + actualIndexName.length() - 1;
    }
    
    private int getTableStartIndex(final String actualSQL) {
        return actualSQL.indexOf("t_order_0");
    }
    
    private int getTableStopIndex(final String actualSQL) {
        return getTableStartIndex(actualSQL) + "t_order_0".length() - 1;
    }
}
