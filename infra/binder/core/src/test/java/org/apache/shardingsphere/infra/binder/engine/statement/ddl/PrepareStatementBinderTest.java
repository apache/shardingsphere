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

package org.apache.shardingsphere.infra.binder.engine.statement.ddl;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.engine.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.PrepareStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrepareStatementBinderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private ShardingSphereMetaData metaData;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private ShardingSphereSchema schema;
    
    @Mock
    private ShardingSphereTable table;
    
    @Test
    void assertBindWithSelect() {
        PrepareStatement prepareStatement = new PrepareStatement(databaseType);
        SelectStatement selectStatement = createSelectStatement();
        prepareStatement.setSelect(selectStatement);
        when(metaData.containsDatabase("foo_db_1")).thenReturn(true);
        when(metaData.getDatabase("foo_db_1")).thenReturn(database);
        when(database.containsSchema("foo_db_1")).thenReturn(true);
        when(database.getSchema("foo_db_1")).thenReturn(schema);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(schema.getTable("t_order")).thenReturn(table);
        when(table.getAllColumns()).thenReturn(Collections.emptyList());
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, "foo_db_1", hintValueContext, prepareStatement);
        PrepareStatement actual = new PrepareStatementBinder().bind(prepareStatement, binderContext);
        assertThat(actual.getDatabaseType(), is(databaseType));
        assertTrue(actual.getSelect().isPresent());
        assertFalse(actual.getInsert().isPresent());
        assertFalse(actual.getUpdate().isPresent());
        assertFalse(actual.getDelete().isPresent());
    }
    
    @Test
    void assertBindWithoutInnerStatements() {
        PrepareStatement prepareStatement = new PrepareStatement(databaseType);
        HintValueContext hintValueContext = new HintValueContext();
        hintValueContext.setSkipMetadataValidate(true);
        SQLStatementBinderContext binderContext = new SQLStatementBinderContext(metaData, "foo_db_1", hintValueContext, prepareStatement);
        PrepareStatement actual = new PrepareStatementBinder().bind(prepareStatement, binderContext);
        assertThat(actual.getDatabaseType(), is(databaseType));
        assertFalse(actual.getSelect().isPresent());
        assertFalse(actual.getInsert().isPresent());
        assertFalse(actual.getUpdate().isPresent());
        assertFalse(actual.getDelete().isPresent());
    }
    
    private SelectStatement createSelectStatement() {
        SelectStatement result = new SelectStatement(databaseType);
        result.setProjections(createProjectionsSegment());
        SimpleTableSegment tableSegment = new SimpleTableSegment(new TableNameSegment(10, 15, new IdentifierValue("t_order")));
        result.setFrom(tableSegment);
        return result;
    }
    
    private ProjectionsSegment createProjectionsSegment() {
        ProjectionsSegment result = new ProjectionsSegment(7, 7);
        result.getProjections().add(new ShorthandProjectionSegment(7, 7));
        return result;
    }
}
