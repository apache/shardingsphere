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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ShorthandProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTableStatementBinderTest {
    
    private final DatabaseType hiveDatabaseType = TypedSPILoader.getService(DatabaseType.class, "Hive");
    
    @Test
    void assertBindCreateTableAsSelectWithSelectSchema() {
        CreateTableStatement createTableStatement = createCreateTableAsSelectStatement();
        CreateTableStatement actual = new CreateTableStatementBinder().bind(createTableStatement, new SQLStatementBinderContext(
                createHiveMetaData(), "sharding_db", new HintValueContext(), createTableStatement, Collections.emptyList()));
        assertTrue(actual.getTable().getTableName().getTableBoundInfo().isPresent());
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("ds_sharding_db"));
    }
    
    @Test
    void assertBindCreateTableAsSelectWithNullCurrentSchema() {
        CreateTableStatement createTableStatement = createCreateTableAsSelectStatement();
        CreateTableStatement actual = new CreateTableStatementBinder().bind(createTableStatement, new SQLStatementBinderContext(
                createHiveMetaData(), "sharding_db", new HintValueContext(), createTableStatement, null));
        assertTrue(actual.getTable().getTableName().getTableBoundInfo().isPresent());
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("ds_sharding_db"));
    }
    
    @Test
    void assertBindCrossDatabaseCreateTableAsSelectWithCurrentSchema() {
        CreateTableStatement createTableStatement = createCrossDatabaseCreateTableAsSelectStatement();
        CreateTableStatement actual = new CreateTableStatementBinder().bind(createTableStatement, new SQLStatementBinderContext(
                createCrossDatabaseHiveMetaData(), "sharding_db", new HintValueContext(), createTableStatement, Collections.emptyList()));
        assertTrue(actual.getTable().getTableName().getTableBoundInfo().isPresent());
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalDatabase().getValue(), is("sharding_db"));
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("ds_sharding_db"));
    }
    
    @Test
    void assertBindSchemaQualifiedCreateTableAsSelectWithCurrentSchema() {
        CreateTableStatement createTableStatement = createSchemaQualifiedCreateTableAsSelectStatement();
        CreateTableStatement actual = new CreateTableStatementBinder().bind(createTableStatement, new SQLStatementBinderContext(
                createSchemaQualifiedHiveMetaData(), "sharding_db", new HintValueContext(), createTableStatement, Collections.emptyList()));
        assertTrue(actual.getTable().getTableName().getTableBoundInfo().isPresent());
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalDatabase().getValue(), is("sharding_db"));
        assertThat(actual.getTable().getTableName().getTableBoundInfo().get().getOriginalSchema().getValue(), is("ds_sharding_db"));
    }
    
    private CreateTableStatement createCreateTableAsSelectStatement() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_ctas")));
        SimpleTableSegment sourceTable = new SimpleTableSegment(new TableNameSegment(24, 30, new IdentifierValue("t_order")));
        ProjectionsSegment projections = new ProjectionsSegment(20, 20);
        projections.getProjections().add(new ShorthandProjectionSegment(20, 20));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(hiveDatabaseType).from(sourceTable).projections(projections).build();
        return CreateTableStatement.builder().databaseType(hiveDatabaseType).table(targetTable).selectStatement(selectStatement).build();
    }
    
    private CreateTableStatement createCrossDatabaseCreateTableAsSelectStatement() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_ctas")));
        SimpleTableSegment sourceTable = new SimpleTableSegment(new TableNameSegment(42, 48, new IdentifierValue("t_order")));
        sourceTable.setOwner(new OwnerSegment(25, 40, new IdentifierValue("sharding_product")));
        ProjectionsSegment projections = new ProjectionsSegment(20, 20);
        projections.getProjections().add(new ShorthandProjectionSegment(20, 20));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(hiveDatabaseType).from(sourceTable).projections(projections).build();
        return CreateTableStatement.builder().databaseType(hiveDatabaseType).table(targetTable).selectStatement(selectStatement).build();
    }
    
    private CreateTableStatement createSchemaQualifiedCreateTableAsSelectStatement() {
        SimpleTableSegment targetTable = new SimpleTableSegment(new TableNameSegment(0, 6, new IdentifierValue("t_ctas")));
        SimpleTableSegment sourceTable = new SimpleTableSegment(new TableNameSegment(35, 41, new IdentifierValue("t_order")));
        sourceTable.setOwner(new OwnerSegment(25, 34, new IdentifierValue("ds_product")));
        ProjectionsSegment projections = new ProjectionsSegment(20, 20);
        projections.getProjections().add(new ShorthandProjectionSegment(20, 20));
        SelectStatement selectStatement = SelectStatement.builder().databaseType(hiveDatabaseType).from(sourceTable).projections(projections).build();
        return CreateTableStatement.builder().databaseType(hiveDatabaseType).table(targetTable).selectStatement(selectStatement).build();
    }
    
    private ShardingSphereMetaData createHiveMetaData() {
        IdentifierValue databaseName = new IdentifierValue("sharding_db");
        IdentifierValue sourceSchemaName = new IdentifierValue("ds_sharding_db");
        IdentifierValue targetTableName = new IdentifierValue("t_ctas");
        IdentifierValue sourceTableName = new IdentifierValue("t_order");
        ShardingSphereSchema sourceSchema = mock(ShardingSphereSchema.class);
        ShardingSphereTable sourceTable = mock(ShardingSphereTable.class);
        when(sourceSchema.getName()).thenReturn(sourceSchemaName.getValue());
        when(sourceSchema.containsTable(sourceTableName)).thenReturn(true);
        when(sourceSchema.containsTable(targetTableName)).thenReturn(false);
        when(sourceSchema.getTable(sourceTableName)).thenReturn(sourceTable);
        when(sourceTable.getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false)));
        ShardingSphereSchema anotherSchema = mock(ShardingSphereSchema.class);
        when(anotherSchema.getName()).thenReturn("analytics");
        when(anotherSchema.containsTable(sourceTableName)).thenReturn(false);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsSchema(sourceSchemaName)).thenReturn(true);
        when(database.getSchema(sourceSchemaName)).thenReturn(sourceSchema);
        when(database.getAllSchemas()).thenReturn(Arrays.asList(sourceSchema, anotherSchema));
        when(database.getDefaultSchemaName()).thenReturn(sourceSchemaName.getValue());
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.containsDatabase(databaseName)).thenReturn(true);
        when(result.getDatabase(databaseName)).thenReturn(database);
        when(result.getDatabase(databaseName.getValue())).thenReturn(database);
        return result;
    }
    
    private ShardingSphereMetaData createCrossDatabaseHiveMetaData() {
        IdentifierValue currentDatabaseName = new IdentifierValue("sharding_db");
        IdentifierValue sourceDatabaseName = new IdentifierValue("sharding_product");
        IdentifierValue currentSchemaName = new IdentifierValue("ds_sharding_db");
        IdentifierValue sourceSchemaName = new IdentifierValue("ds_product");
        IdentifierValue targetTableName = new IdentifierValue("t_ctas");
        IdentifierValue sourceTableName = new IdentifierValue("t_order");
        ShardingSphereSchema currentSchema = mock(ShardingSphereSchema.class);
        when(currentSchema.getName()).thenReturn(currentSchemaName.getValue());
        when(currentSchema.containsTable(targetTableName)).thenReturn(false);
        ShardingSphereSchema sourceSchema = mock(ShardingSphereSchema.class);
        ShardingSphereTable sourceTable = mock(ShardingSphereTable.class);
        when(sourceSchema.getName()).thenReturn(sourceSchemaName.getValue());
        when(sourceSchema.containsTable(sourceTableName)).thenReturn(true);
        when(sourceSchema.getTable(sourceTableName)).thenReturn(sourceTable);
        when(sourceTable.getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false)));
        ShardingSphereDatabase currentDatabase = mock(ShardingSphereDatabase.class);
        when(currentDatabase.containsSchema(currentSchemaName)).thenReturn(true);
        when(currentDatabase.containsSchema(sourceDatabaseName)).thenReturn(false);
        when(currentDatabase.getSchema(currentSchemaName)).thenReturn(currentSchema);
        when(currentDatabase.getAllSchemas()).thenReturn(Collections.singleton(currentSchema));
        when(currentDatabase.getDefaultSchemaName()).thenReturn(currentSchemaName.getValue());
        ShardingSphereDatabase sourceDatabase = mock(ShardingSphereDatabase.class);
        when(sourceDatabase.containsSchema(sourceSchemaName)).thenReturn(true);
        when(sourceDatabase.getSchema(sourceSchemaName)).thenReturn(sourceSchema);
        when(sourceDatabase.getAllSchemas()).thenReturn(Collections.singleton(sourceSchema));
        when(sourceDatabase.getDefaultSchemaName()).thenReturn(sourceSchemaName.getValue());
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.containsDatabase(currentDatabaseName)).thenReturn(true);
        when(result.containsDatabase(sourceDatabaseName)).thenReturn(true);
        when(result.getDatabase(currentDatabaseName)).thenReturn(currentDatabase);
        when(result.getDatabase(currentDatabaseName.getValue())).thenReturn(currentDatabase);
        when(result.getDatabase(sourceDatabaseName)).thenReturn(sourceDatabase);
        when(result.getDatabase(sourceDatabaseName.getValue())).thenReturn(sourceDatabase);
        return result;
    }
    
    private ShardingSphereMetaData createSchemaQualifiedHiveMetaData() {
        IdentifierValue databaseName = new IdentifierValue("sharding_db");
        IdentifierValue currentSchemaName = new IdentifierValue("ds_sharding_db");
        IdentifierValue sourceSchemaName = new IdentifierValue("ds_product");
        IdentifierValue targetTableName = new IdentifierValue("t_ctas");
        IdentifierValue sourceTableName = new IdentifierValue("t_order");
        ShardingSphereSchema currentSchema = mock(ShardingSphereSchema.class);
        when(currentSchema.getName()).thenReturn(currentSchemaName.getValue());
        when(currentSchema.containsTable(targetTableName)).thenReturn(false);
        ShardingSphereSchema sourceSchema = mock(ShardingSphereSchema.class);
        ShardingSphereTable sourceTable = mock(ShardingSphereTable.class);
        when(sourceSchema.getName()).thenReturn(sourceSchemaName.getValue());
        when(sourceSchema.containsTable(sourceTableName)).thenReturn(true);
        when(sourceSchema.getTable(sourceTableName)).thenReturn(sourceTable);
        when(sourceTable.getAllColumns()).thenReturn(Collections.singletonList(
                new ShardingSphereColumn("order_id", Types.INTEGER, true, false, false, true, false, false)));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.containsSchema(currentSchemaName)).thenReturn(true);
        when(database.containsSchema(sourceSchemaName)).thenReturn(true);
        when(database.getSchema(currentSchemaName)).thenReturn(currentSchema);
        when(database.getSchema(sourceSchemaName)).thenReturn(sourceSchema);
        when(database.getAllSchemas()).thenReturn(Arrays.asList(currentSchema, sourceSchema));
        when(database.getDefaultSchemaName()).thenReturn(currentSchemaName.getValue());
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class);
        when(result.containsDatabase(databaseName)).thenReturn(true);
        when(result.getDatabase(databaseName)).thenReturn(database);
        when(result.getDatabase(databaseName.getValue())).thenReturn(database);
        return result;
    }
}
