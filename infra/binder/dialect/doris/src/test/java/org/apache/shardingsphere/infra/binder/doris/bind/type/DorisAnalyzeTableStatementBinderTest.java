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

package org.apache.shardingsphere.infra.binder.doris.bind.type;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.doris.dal.DorisAnalyzeTableStatement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DorisAnalyzeTableStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Test
    void assertBindWithTable() {
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(database);
        when(database.containsSchema("foo_db")).thenReturn(true);
        when(database.getSchema("foo_db")).thenReturn(schema);
        when(schema.containsTable("test_table")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getAllColumns()).thenReturn(Collections.emptyList());
        when(schema.getTable("test_table")).thenReturn(table);
        DorisAnalyzeTableStatement actual = getDorisAnalyzeTableStatement();
        assertNotNull(actual.getTable());
        assertThat(actual.getTable().getTableName().getIdentifier().getValue(), is("test_table"));
        assertTrue(actual.isSync());
        assertThat(actual.getSampleType().orElse(null), is("PERCENT"));
        assertThat(actual.getSampleValue().orElse(null), is(10));
    }
    
    private DorisAnalyzeTableStatement getDorisAnalyzeTableStatement() {
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("test_table")));
        DorisAnalyzeTableStatement original = new DorisAnalyzeTableStatement(databaseType, tableSegment, null, Collections.emptyList(), true, "PERCENT", 10);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, "foo_db", hintValueContext, original);
        return new DorisAnalyzeTableStatementBinder().bind(original, binderContext);
    }
    
    @Test
    void assertBindWithoutTable() {
        DorisAnalyzeTableStatement original = new DorisAnalyzeTableStatement(databaseType, null, null, Collections.emptyList(), false, null, null);
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, "foo_db", hintValueContext, original);
        DorisAnalyzeTableStatement actual = new DorisAnalyzeTableStatementBinder().bind(original, binderContext);
        assertNull(actual.getTable());
    }
}
